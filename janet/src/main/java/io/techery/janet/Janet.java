package io.techery.janet;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.PublishProcessor;
import io.techery.janet.internal.TypeToken;

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
    private final PublishProcessor<ActionPair> pipeline;

    private Janet(Builder builder) {
        this.services = builder.services;
        this.pipeline = PublishProcessor.create();
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
        return new ActionPipe<A>(new FlowableFactory<A>() {
            @Override public Flowable<ActionState<A>> create(A action) {
                return send(action);
            }
        }, new Provider<Flowable<ActionState<A>>>() {
            @Override public Flowable<ActionState<A>> provide() {
                return pipeline
                        .onBackpressureBuffer()
                        .map(new Function<ActionPair, ActionState>() {
                            @Override public ActionState apply(ActionPair pair) {
                                return pair.state;
                            }
                        })
                        .filter(new Predicate<ActionState>() {
                            @Override public boolean test(ActionState actionState) {
                                return actionClass.isInstance(actionState.action);
                            }
                        }).compose(new CastToState<A>());
            }
        }, new CancelConsumer<A>() {
            @Override public void accept(A a) {
                doCancel(a);
            }
        }, defaultSubscribeOn);
    }

    private <A> Flowable<ActionState<A>> send(final A action) {
        return pipeline
                .filter(new Predicate<ActionPair>() {
                    @Override public boolean test(ActionPair pair) {
                        return pair.holder.isOrigin(action);
                    }
                })
                .map(new Function<ActionPair, ActionState<A>>() {
                    @Override public ActionState apply(ActionPair pair) {
                        return pair.state;
                    }
                })
                .compose(new CastToState<A>())
                .mergeWith(Flowable.<ActionState<A>>empty()
                        .doOnSubscribe(new Consumer<Subscription>() {
                            @Override public void accept(Subscription subscription) throws Exception {
                                doSend(action);
                            }
                        }))
                .takeUntil(new Predicate<ActionState<A>>() {
                    @Override public boolean test(ActionState actionState) {
                        return actionState.status == ActionState.Status.SUCCESS
                                || actionState.status == ActionState.Status.FAIL;
                    }
                });
    }

    private <A> void doSend(A action) {
        ActionService service = findService(action.getClass());
        service.send(ActionHolder.create(action));
    }

    private <A> void doCancel(A action) {
        ActionHolder holder = ActionHolder.create(action);
        pipeline.onNext(new ActionPair(holder, ActionState.fail(action, new CancelException())));
        ActionService service = findService(action.getClass());
        service.cancel(holder);
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

    private final static class CastToState<A> implements FlowableTransformer<ActionState, ActionState<A>> {

        private final Class<ActionState<A>> type;

        @SuppressWarnings("unchecked") public CastToState() {
            type = (Class<ActionState<A>>) new TypeToken<ActionState<A>>() {}.getRawType();
        }

        @Override public Publisher<ActionState<A>> apply(Flowable<ActionState> upstream) {
            return upstream.cast(type);
        }
    }
}
