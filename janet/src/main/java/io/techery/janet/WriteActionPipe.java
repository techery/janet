package io.techery.janet;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

public interface WriteActionPipe<A> {

    /**
     * Send action to {@linkplain Janet}.
     *
     * @param action prepared action for sending
     */
    void send(A action);

    /**
     * Send action to {@linkplain Janet}.
     *
     * @param action      prepared action for sending
     * @param subscribeOn {@linkplain Scheduler} to do {@linkplain Observable#subscribeOn(Scheduler) subcribeOn} of created Observable.
     */
    void send(A action, Scheduler subscribeOn);

    /**
     * Cancel running action.
     * Action cancellation defines in relative service {@linkplain ActionService#cancel(ActionHolder)}
     *
     * @param action prepared action for cancellation
     */
    void cancel(A action);

    /**
     * Create {@linkplain Observable observable} to send action and receive result
     * in the form of action {@linkplain ActionState states} synchronously
     *
     * @param action prepared action to send
     */
    Observable<ActionState<A>> createObservable(A action);

    /**
     * Create {@linkplain Observable observable} to send action and receive action with result synchronously
     * <p>
     * To catch errors use {@linkplain Subscriber#onError(Throwable)}
     *
     * @param action prepared action to send
     */
    Observable<A> createObservableSuccess(A action);
}
