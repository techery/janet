package io.techery.janet;

import java.util.ArrayList;
import java.util.List;

import io.techery.janet.internal.TypeToken;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Action router that can send and receive actions using added {@linkplain ActionService services} that know
 * what do with theirs. Each action must to have an annotation that is defined
 * in {@linkplain ActionService#getSupportedAnnotationType()} and after that Janet will be able to process them.
 * Create instances using {@linkplain Builder the builder} where it's possible to add the necessary services
 * using {@linkplain Builder#addService(ActionService)}
 * <p>
 * For example,
 * <pre>{@code
 * Janet janet = new Janet.Builder()
 *         .addService(new HttpActionService(API_URL, new OkClient(), new GsonConverter(new Gson())))
 *         .build();}
 * </pre>
 */
public final class Janet {

    private final List<ActionService> services;
    private final PublishSubject<ActionPair> pipeline;

    private Janet(Builder builder) {
        this.services = builder.services;
        this.pipeline = PublishSubject.create();
        connectPipeline();
    }

    private void connectPipeline() {
        for (ActionService service : services) {
            service.setCallback(new ActionService.Callback() {
                @Override public void onStart(ActionHolder holder) {
                    pipeline.onNext(new ActionPair(holder, ActionState.start(holder.action())));
                }

                @Override public void onProgress(ActionHolder holder, int progress) {
                    pipeline.onNext(new ActionPair(holder, ActionState.progress(holder.action(), progress)));
                }

                @Override public void onSuccess(ActionHolder holder) {
                    pipeline.onNext(new ActionPair(holder, ActionState.success(holder.action())));
                }

                @Override public void onFail(ActionHolder holder, JanetException e) {
                    pipeline.onNext(new ActionPair(holder, ActionState.fail(holder.action(), e)));
                }
            });
        }
    }

    /**
     * Create an {@linkplain ActionPipe} for working with specific actions
     *
     * @param actionClass type of action
     */
    public <A> ActionPipe<A> createPipe(Class<A> actionClass) {
        return createPipe(actionClass, null);
    }

    /**
     * Create an {@linkplain ActionPipe} for working with specific actions
     *
     * @param actionClass        type of action
     * @param defaultSubscribeOn default {@linkplain Scheduler} to do {@linkplain Observable#subscribeOn(Scheduler) subcribeOn} of created Observable in this ActionPipe
     */
    public <A> ActionPipe<A> createPipe(final Class<A> actionClass, Scheduler defaultSubscribeOn) {
        return new ActionPipe<A>(new Func1<A, Observable<ActionState<A>>>() {
            @Override public Observable<ActionState<A>> call(A action) {
                return send(action);
            }
        }, new Func0<Observable<ActionState<A>>>() {
            @Override public Observable<ActionState<A>> call() {
                return pipeline.asObservable()
                        .map(new Func1<ActionPair, ActionState>() {
                            @Override public ActionState call(ActionPair pair) {
                                return pair.state;
                            }
                        })
                        .filter(new Func1<ActionState, Boolean>() {
                            @Override public Boolean call(ActionState actionState) {
                                return actionClass.isInstance(actionState.action);
                            }
                        }).compose(new CastToState<A>());
            }
        }, new Action1<A>() {
            @Override public void call(A a) {
                doCancel(a);
            }
        }, defaultSubscribeOn);
    }

    private <A> Observable<ActionState<A>> send(final A action) {
        return pipeline.asObservable()
                .filter(new Func1<ActionPair, Boolean>() {
                    @Override public Boolean call(ActionPair pair) {
                        return pair.holder.isOrigin(action);
                    }
                })
                .map(new Func1<ActionPair, ActionState>() {
                    @Override public ActionState call(ActionPair pair) {
                        return pair.state;
                    }
                })
                .compose(new CastToState<A>())
                .mergeWith(Observable.<ActionState<A>>empty()
                        .doOnSubscribe(new Action0() {
                            @Override public void call() {
                                doSend(action);
                            }
                        }));
    }

    private <A> void doSend(A action) {
        ActionService service = findService(action.getClass());
        service.send(ActionHolder.create(action));
    }

    private <A> void doCancel(A action) {
        ActionService service = findService(action.getClass());
        service.cancel(ActionHolder.create(action));
    }

    private ActionService findService(Class actionClass) {
        for (ActionService service : services) {
            if (actionClass.getAnnotation(service.getSupportedAnnotationType()) != null) {
                return service;
            }
        }
        throw new JanetInternalException("Action class should be annotated by any supported annotation or check dependence of any service");
    }

    /**
     * Builds an instance of {@linkplain Janet}.
     */
    public static final class Builder {

        private List<ActionService> services = new ArrayList<ActionService>();

        /**
         * Add an service for action processing
         */
        public Builder addService(ActionService service) {
            if (service == null) {
                throw new IllegalArgumentException("ActionService may not be null.");
            }
            if (service.getSupportedAnnotationType() == null) {
                throw new IllegalArgumentException("the ActionService doesn't support any actions");
            }
            services.add(service);
            return this;
        }

        /**
         * Create the {@linkplain Janet} instance using added services.
         */
        public Janet build() {
            return new Janet(this);
        }
    }

    private final static class ActionPair {
        private final ActionHolder holder;
        private final ActionState state;

        private ActionPair(ActionHolder holder, ActionState state) {
            this.holder = holder;
            this.state = state;
        }
    }

    private final static class CastToState<A> implements Observable.Transformer<ActionState, ActionState<A>> {

        private final Class<ActionState<A>> type;

        @SuppressWarnings("unchecked") public CastToState() {
            type = (Class<ActionState<A>>) new TypeToken<ActionState<A>>() {}.getRawType();
        }

        @Override public Observable<ActionState<A>> call(Observable<ActionState> source) {
            return source.cast(type);
        }
    }
}
