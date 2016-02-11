package io.techery.janet;

import java.util.ArrayList;
import java.util.List;

import io.techery.janet.internal.CastToState;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class Janet {

    private final List<ActionAdapter> adapters;
    private final PublishSubject<ActionState> pipeline;
    private final InterceptorsDecorator interceptors;

    private Janet(Builder builder) {
        this.adapters = builder.adapters;
        this.pipeline = PublishSubject.create();
        this.interceptors = builder.interceptors;
        connectPipeline();
        connectInterceptors();
    }

    private void connectPipeline() {
        for (ActionAdapter adapter : adapters) {
            adapter.setOnResponseCallback(new ActionAdapter.Callback() {
                @Override public void onStart(Object action) {
                    //noinspection unchecked
                    pipeline.onNext(new ActionState(action).status(ActionState.Status.START));
                }

                @Override public void onSuccess(Object action) {
                    //noinspection unchecked
                    pipeline.onNext(new ActionState(action).status(ActionState.Status.SUCCESS));
                }

                @Override public void onServerError(Object action) {
                    //noinspection unchecked
                    pipeline.onNext(new ActionState(action).status(ActionState.Status.SERVER_ERROR));
                }

                @Override public void onFail(Object action, Throwable throwable) {
                    //noinspection unchecked
                    pipeline.onNext(new ActionState(action).throwable(throwable).status(ActionState.Status.FAIL));
                }
            });
        }
    }

    private void connectInterceptors() {
        pipeline.asObservable()
                .subscribe(new Subscriber<ActionState>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {}

                    @Override public void onNext(ActionState actionState) {
                        interceptors.intercept(actionState);
                    }
                });
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

    private ActionAdapter findActionAdapter(Class actionClass) {
        for (ActionAdapter adapter : adapters) {
            if (actionClass.getAnnotation(adapter.getActionAnnotationClass()) != null) {
                return adapter;
            }
        }
        throw new JanetInternalException("Action class should be annotated by any supported annotation or check dependence of any adapter");
    }

    public static class Builder {

        private List<ActionAdapter> adapters = new ArrayList<ActionAdapter>();
        private InterceptorsDecorator interceptors = new InterceptorsDecorator();

        public Builder addAdapter(ActionAdapter adapter) {
            if (adapter == null) {
                throw new IllegalArgumentException("ActionAdapter may not be null.");
            }
            if (adapter.getActionAnnotationClass() == null) {
                throw new IllegalArgumentException("the ActionAdapter doesn't support any actions");
            }
            adapters.add(adapter);
            return this;
        }

        public Builder addInterceptor(ActionState.Status status, Interceptor interceptor) {
            if (status == null) {
                throw new IllegalArgumentException("ActionState.Status may not be null.");
            }
            if (interceptor == null) {
                throw new IllegalArgumentException("Interceptor may not be null.");
            }
            this.interceptors.add(status, interceptor);
            return this;
        }

        public Janet build() {
            return new Janet(this);
        }
    }
}
