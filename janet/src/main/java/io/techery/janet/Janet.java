package io.techery.janet;

import java.util.ArrayList;
import java.util.List;

import io.techery.janet.utils.TypeToken;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class Janet {

    private final List<Interceptor> interceptors;
    private final List<ActionAdapter> adapters;
    private final PublishSubject<ActionState> pipeline;

    private Janet(Builder builder) {
        this.interceptors = builder.interceptors;
        this.adapters = builder.adapters;
        this.pipeline = PublishSubject.create();
        connectPipeline();
    }

    private <A> void sendAction(A action) {
        ActionAdapter adapter = findActionAdapter(action.getClass());
        adapter.send(action);
    }

    private ActionAdapter findActionAdapter(Class actionClass) {
        for (ActionAdapter adapter : adapters) {
            if (actionClass.getAnnotation(adapter.getActionAnnotationClass()) != null) {
                return adapter;
            }
        }
        throw new JanetException("Action class should be annotated by any supported annotation or check dependence of any adapter");
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

    private <A> Observable<ActionState<A>> createObservable(final A action) {
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
                                sendAction(action);
                            }
                        }));
    }

    public <A> JanetPipe<A> createExecutor(final Class<A> actionClass, Scheduler scheduler) {
        return new JanetPipe<A>(new Func1<A, Observable<ActionState<A>>>() {
            @Override
            public Observable<ActionState<A>> call(A action) {
                return createObservable(action);
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

    public <A> JanetPipe<A> createExecutor(Class<A> actionClass) {
        return createExecutor(actionClass, null);
    }


    private static class CastToState<A> implements Observable.Transformer<ActionState, ActionState<A>> {

        private final Class<ActionState<A>> type;


        @SuppressWarnings("unchecked")
        private CastToState() {
            type = (Class<ActionState<A>>) new TypeToken<ActionState<A>>() {}.getRawType();
        }


        @Override public Observable<ActionState<A>> call(Observable<ActionState> source) {
            return source.cast(type);
        }
    }

    public interface Interceptor {
        void intercept(Action action);
    }

    private interface Callback<A> {
        void call(A value) throws Exception;
    }

    public static class Builder {
        private List<ActionAdapter> adapters = new ArrayList<ActionAdapter>();
        private List<Interceptor> interceptors = new ArrayList<Interceptor>();


        public Builder addInterceptor(Interceptor requestInterceptor) {
            if (requestInterceptor == null) {
                throw new IllegalArgumentException("Request interceptor may not be null.");
            }
            this.interceptors.add(requestInterceptor);
            return this;
        }

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

        public Janet build() {
            return new Janet(this);
        }
    }
}
