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

public class Janet {

    private final List<ActionAdapter> adapters;
    private final PublishSubject<ActionState> pipeline;

    private Janet(Builder builder) {
        this.adapters = builder.adapters;
        this.pipeline = PublishSubject.create();
        connectPipeline();
    }

    private void connectPipeline() {
        for (ActionAdapter adapter : adapters) {
            adapter.setCallback(new ActionAdapter.Callback() {
                @Override public void onStart(Object action) {
                    pipeline.onNext(ActionState.start(action));
                }

                @Override public void onProgress(Object action, int progress) {
                    pipeline.onNext(ActionState.progress(action, progress));
                }

                @Override public void onSuccess(Object action) {
                    pipeline.onNext(ActionState.success(action));
                }

                @Override public void onFail(Object action, JanetException e) {
                    pipeline.onNext(ActionState.fail(action, e));
                }
            });
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public API
    ///////////////////////////////////////////////////////////////////////////

    public <A> ActionPipe<A> createPipe(Class<A> actionClass) {
        return createPipe(actionClass, null);
    }

    public <A> ActionPipe<A> createPipe(final Class<A> actionClass, Scheduler scheduler) {
        return new ActionPipe<A>(new Func1<A, Observable<ActionState<A>>>() {
            @Override
            public Observable<ActionState<A>> call(A action) {
                return send(action);
            }
        }, new Func0<Observable<ActionState<A>>>() {
            @Override public Observable<ActionState<A>> call() {
                return pipeline.asObservable()
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

    ///////////////////////////////////////////////////////////////////////////
    // Send Action Flow
    ///////////////////////////////////////////////////////////////////////////

    private <A> Observable<ActionState<A>> send(final A action) {
        return pipeline.asObservable()
                .filter(new Func1<ActionState, Boolean>() {
                    @Override public Boolean call(ActionState actionState) {
                        return actionState.action == action;
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
        adapter.send(action);
    }

    private <A> void doCancel(A action) {
        ActionAdapter adapter = findActionAdapter(action.getClass());
        adapter.cancel(action);
    }

    private ActionAdapter findActionAdapter(Class actionClass) {
        for (ActionAdapter adapter : adapters) {
            if (actionClass.getAnnotation(adapter.getSupportedAnnotationType()) != null) {
                return adapter;
            }
        }
        throw new JanetInternalException("Action class should be annotated by any supported annotation or check dependence of any adapter");
    }

    public static class Builder {

        private List<ActionAdapter> adapters = new ArrayList<ActionAdapter>();

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

        public Janet build() {
            return new Janet(this);
        }
    }
}
