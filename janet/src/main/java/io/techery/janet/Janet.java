package io.techery.janet;

import java.util.ArrayList;
import java.util.List;

import io.techery.janet.internal.CastToState;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Action router that can send and receive actions using added {@link ActionAdapter adapters} that know
 * what do with theirs. Each action must to have an annotation that is defined
 * in {@link ActionAdapter#getSupportedAnnotationType()} and after that Janet will be able to process them.
 * Create instances using {@linkplain Builder the builder} where it's possible to add the necessary adapters
 * using {@link Builder#addAdapter(ActionAdapter)}
 * <p>
 * For example,
 * <pre>{@code
 * Janet janet = new Janet.Builder()
 *         .addAdapter(new HttpActionAdapter(API_URL, new OkClient(), new GsonConverter(new Gson())))
 *         .build();}
 * </pre>
 */
public final class Janet {

    private final List<ActionAdapter> adapters;
    private final PublishSubject<ActionPair> pipeline;

    private Janet(Builder builder) {
        this.adapters = builder.adapters;
        this.pipeline = PublishSubject.create();
        connectPipeline();
    }

    private void connectPipeline() {
        for (ActionAdapter adapter : adapters) {
            adapter.setCallback(new ActionAdapter.Callback() {
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
     * Create an {@link ActionPipe} for working with actions
     *
     * @param actionClass type of action
     */
    public <A> ActionPipe<A> createPipe(Class<A> actionClass) {
        return createPipe(actionClass, null);
    }

    /**
     * Create an {@link ActionPipe} for working with specific actions
     *
     * @param actionClass type of action
     * @param scheduler   add {@link rx.Scheduler} to {@link ActionPipe} using {@link ActionPipe#pimp(Scheduler)}
     */
    public <A> ActionPipe<A> createPipe(final Class<A> actionClass, Scheduler scheduler) {
        return new ActionPipe<A>(new Func1<A, Observable<ActionState<A>>>() {
            @Override
            public Observable<ActionState<A>> call(A action) {
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
        }).pimp(scheduler);
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
        ActionAdapter adapter = findActionAdapter(action.getClass());
        adapter.send(ActionHolder.create(action));
    }

    private <A> void doCancel(A action) {
        ActionAdapter adapter = findActionAdapter(action.getClass());
        adapter.cancel(ActionHolder.create(action));
    }

    private ActionAdapter findActionAdapter(Class actionClass) {
        for (ActionAdapter adapter : adapters) {
            if (actionClass.getAnnotation(adapter.getSupportedAnnotationType()) != null) {
                return adapter;
            }
        }
        throw new JanetInternalException("Action class should be annotated by any supported annotation or check dependence of any adapter");
    }

    /**
     * Builds an instance of {@link Janet}.
     */
    public static final class Builder {

        private List<ActionAdapter> adapters = new ArrayList<ActionAdapter>();

        /**
         * Add an adapter for action processing
         */
        public Builder addAdapter(ActionAdapter adapter) {
            if (adapter == null) {
                throw new IllegalArgumentException("ActionAdapter may not be null.");
            }
            if (adapter.getSupportedAnnotationType() == null) {
                throw new IllegalArgumentException("the ActionAdapter doesn't support any actions");
            }
            adapters.add(adapter);
            return this;
        }

        /**
         * Create the {@link Janet} instance using added adapters.
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
}
