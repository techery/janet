package io.techery.janet;


import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;

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
     * Cancel latest sent or received non-finished action
     */
    void cancelLatest();

    /**
     * Create {@linkplain Observable observable} to send action and receive result
     * in the form of action {@linkplain ActionState states} synchronously
     *
     * @param action prepared action to send
     */
    Flowable<ActionState<A>> createObservable(A action);

    /**
     * Create {@linkplain Observable observable} to send action and receive action with result synchronously
     * <p>
     * To catch errors use {@linkplain SingleObserver#onError(Throwable)}
     *
     * @param action prepared action to send
     */
    Single<A> createObservableResult(A action);
}
