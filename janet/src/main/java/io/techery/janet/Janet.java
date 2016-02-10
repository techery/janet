package io.techery.janet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.techery.janet.utils.TypeToken;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action;
import rx.functions.Action1;
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
    }

    private <A> void sendAction(A action, Action1<A> callback) throws IOException {
        ActionAdapter adapter = findActionAdapter(action.getClass());
        adapter.send(action, callback);
    }

    private <A> Observable<ActionState<A>> bind(Observable<ActionState<A>> observable) {
        return observable.doOnNext(new Action1<ActionState<A>>() {
            @Override public void call(ActionState<A> actionState) {
                pipeline.onNext(actionState);
            }
        }).doOnError(new Action1<Throwable>() {
            @Override public void call(Throwable throwable) {
                pipeline.onError(throwable);
            }
        });
    }

    private ActionAdapter findActionAdapter(Class actionClass) {
        for (ActionAdapter adapter : adapters) {
            if (actionClass.getAnnotation(adapter.getActionAnnotationClass()) != null) {
                return adapter;
            }
        }
        throw new JanetException("Action class should be annotated by any supported annotation or check dependence of any adapter");
    }

    private <A> Observable<ActionState<A>> createObservable(final A action) {
        return Observable
                .create(new CallOnSubscribe<A>(action, new Callback<A, Action1<A>>() {
                    @Override
                    public void call(A action, Action1<A> callback) throws IOException {
                        sendAction(action, callback);
                    }
                }));
    }

    public <A> JanetExecutor<A> createExecutor(final Class<A> actionClass, Scheduler scheduler) {
        final TypeToken<ActionState<A>> type = new TypeToken<ActionState<A>>() {};
        return new JanetExecutor<A>(new Func1<A, Observable<ActionState<A>>>() {
            @Override
            public Observable<ActionState<A>> call(A action) {
                return bind(createObservable(action));
            }
        }, new Func0<Observable<ActionState<A>>>() {
            @Override public Observable<ActionState<A>> call() {
                return pipeline.asObservable().filter(new Func1<ActionState, Boolean>() {
                    @Override public Boolean call(ActionState actionState) {
                        return actionClass.isInstance(actionState.action);
                    }
                }).cast((Class<ActionState<A>>) type.getRawType());
            }
        }).scheduler(scheduler);
    }

    public <A> JanetExecutor<A> createExecutor(Class<A> actionClass) {
        return createExecutor(actionClass, null);
    }

    final private static class CallOnSubscribe<A> implements Observable.OnSubscribe<ActionState<A>> {

        private final A action;
        private final Callback<A, Action1<A>> func;

        CallOnSubscribe(A action, Callback<A, Action1<A>> func) {
            this.action = action;
            this.func = func;
        }

        @Override public void call(final Subscriber<? super ActionState<A>> subscriber) {
            final ActionState<A> state = new ActionState<A>(action);
            subscriber.onNext(state.status(ActionState.Status.START));
            try {
                func.call(action, new Action1<A>() {
                    @Override
                    public void call(A action) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(state.status(ActionState.Status.SUCCESS));
                        }
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
            } catch (final Exception e) {
                Exceptions.throwIfFatal(e);
                if (e instanceof JanetException) {
                    throw (JanetException) e;
                }
                if (e instanceof JanetServerException) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(state.status(ActionState.Status.SERVER_ERROR));
                    }
                } else {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(state.status(ActionState.Status.FAIL).throwable(e));
                    }
                }
            }
        }
    }

    public interface Interceptor {
        void intercept(Action action);
    }

    private interface Callback<A, T> {
        void call(A value, T value2) throws IOException;
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
