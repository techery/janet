package io.techery.janet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action;
import rx.functions.Action1;
import rx.functions.Func1;

public class Janet {

    private final List<Interceptor> interceptors;
    private final List<ActionAdapter> adapters;

    private Janet(Builder builder) {
        this.interceptors = builder.interceptors;
        this.adapters = builder.adapters;
    }

    private <A> void sendAction(A action, Action1<A> callback) throws IOException {
        ActionAdapter adapter = getActionAdapter(action.getClass());
        if (adapter == null) {
            throw new JanetException("Action object should be annotated by any supported annotation or check dependence of any adapter");
        }
        adapter.send(action, callback);
    }

    private ActionAdapter getActionAdapter(Class actionClass) {
        for (ActionAdapter adapter : adapters) {
            if (actionClass.getAnnotation(adapter.getActionAnnotationClass()) != null) {
                return adapter;
            }
        }
        return null;
    }

    public <A> Observable<A> createObservable(final A action) {
        return Observable
                .create(new CallOnSubscribe<A>(new Callback<Action1<A>>() {
                    @Override
                    public void call(Action1<A> callback) throws IOException {
                        sendAction(action, callback);
                    }
                }));
    }

    public <A> JanetExecutor<A> createExecutor(Class<A> actionClass, Scheduler scheduler) {
        return new JanetExecutor<A>(new Func1<A, Observable<A>>() {
            @Override
            public Observable<A> call(A action) {
                return createObservable(action);
            }
        }).scheduler(scheduler);
    }

    public <A> JanetExecutor<A> createExecutor(Class<A> actionClass) {
        return createExecutor(actionClass, null);
    }

    final private static class CallOnSubscribe<A> implements Observable.OnSubscribe<A> {

        private final Callback<Action1<A>> func;

        CallOnSubscribe(Callback<Action1<A>> func) {
            this.func = func;
        }

        @Override
        public void call(final Subscriber<? super A> subscriber) {
            try {
                func.call(new Action1<A>() {
                    @Override
                    public void call(A action) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(action);
                        }
                    }
                });
            } catch (final Exception e) {
                Exceptions.throwIfFatal(e);
                if (e instanceof JanetException) {
                    throw (JanetException) e;
                }
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }
    }

    public interface Interceptor {
        void intercept(Action action);
    }

    private interface Callback<A> {
        void call(A value) throws IOException;
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
