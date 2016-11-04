package io.techery.janet;

import org.junit.Test;

import java.util.concurrent.Executor;

import io.techery.janet.helper.ActionStateSubscriber;
import io.techery.janet.model.TestAction;
import io.techery.janet.util.FakeExecutor;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestPipeOperations extends BaseTest {


    @Test
    public void testBackpressure() {
        TestSubscriber<String> subscriber = new TestSubscriber<String>();
        Observable.range(0, 10)
                .map(new Func1<Integer, Observable<ActionState<TestAction>>>() {
                    @Override
                    public Observable<ActionState<TestAction>> call(Integer integer) {
                        return actionPipe.observe();
                    }
                })
                .flatMap(new Func1<Observable<ActionState<TestAction>>, Observable<ActionState<TestAction>>>() {
                    @Override
                    public Observable<ActionState<TestAction>> call(Observable<ActionState<TestAction>> observable) {
                        return observable.mergeWith(actionPipe.observe());
                    }
                })
                .map(new Func1<ActionState<TestAction>, String>() {
                    @Override
                    public String call(ActionState<TestAction> state) {
                        return String.valueOf(state);
                    }
                })
                .distinct() //reduce pressure for subscriber
                .subscribe(subscriber);
        int magicLoopSize = 100;
        Observable.range(0, magicLoopSize)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        actionPipe.send(new TestAction());
                    }
                });
        Observable.range(0, magicLoopSize)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        actionPipe.send(new TestAction());
                    }
                });
        subscriber.unsubscribe();
        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
    }

    @Test
    public void createObservable() {
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);
    }

    @Test
    public void sendWithObserve() {
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observe().subscribe(subscriber);
        actionPipe.send(new TestAction());
        subscriber.unsubscribe();
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);
    }

    @Test
    public void sendWithObserveWithReplay() {
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.send(new TestAction());
        actionPipe.observeWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);

        subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observeWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
    }

    @Test
    public void createObservableSuccess() {
        TestSubscriber<TestAction> subscriber = new TestSubscriber<TestAction>();
        TestAction action = new TestAction();
        actionPipe.createObservableResult(action).subscribe(subscriber);
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);
    }

    @Test
    public void sendWithObserveSuccess() {
        TestSubscriber<TestAction> subscriber = new TestSubscriber<TestAction>();
        TestAction action = new TestAction();
        actionPipe.observeSuccess().subscribe(subscriber);
        actionPipe.send(action);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);
    }

    @Test
    public void sendWithObserveSuccessWithReplay() {
        TestSubscriber<TestAction> subscriber = new TestSubscriber<TestAction>();
        TestAction action = new TestAction();
        actionPipe.send(action);
        actionPipe.observeSuccessWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);

        subscriber = new TestSubscriber<TestAction>();
        actionPipe.observeSuccessWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);
    }

    @Test
    public void sendWithDefaultScheduler() {
        Executor fakeExecutor = spy(new FakeExecutor());
        //
        ActionPipe<TestAction> actionPipe = providePipe(janet, Schedulers.from(fakeExecutor));
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observe().subscribe(subscriber);
        actionPipe.send(new TestAction());
        subscriber.unsubscribe();
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);
        verify(fakeExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    public void sendWithOverridenScheduler() {
        Executor defaultExecutor = spy(new FakeExecutor());
        Executor newExecutor = spy(new FakeExecutor());
        //
        ActionPipe<TestAction> actionPipe = providePipe(janet, Schedulers.from(defaultExecutor));
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observe().subscribe(subscriber);
        actionPipe.send(new TestAction(), Schedulers.from(newExecutor));
        subscriber.unsubscribe();
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);
        verify(defaultExecutor, never()).execute(any(Runnable.class));
        verify(newExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    public void cancelActionAfterStart() {
        final TestAction action = new TestAction();
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>(
                new ActionStateSubscriber<TestAction>().onStart(new Action1<TestAction>() {
                    @Override public void call(TestAction testAction) {
                        actionPipe.cancel(action);
                    }
                })
        );
        actionPipe.createObservable(action).subscribe(subscriber);
        AssertUtil.assertCanceled(subscriber);
        verify(service, times(1)).cancel(any(ActionHolder.class));
    }

    @Test
    public void cancelLatestAfterStart() {
        final TestAction action = new TestAction();
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>(
                new ActionStateSubscriber<TestAction>().onStart(new Action1<TestAction>() {
                    @Override public void call(TestAction testAction) {
                        actionPipe.cancelLatest();
                    }
                })
        );
        actionPipe.createObservable(action).subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertCanceled(subscriber);
        verify(service, times(1)).cancel(any(ActionHolder.class));
    }

    @Test
    public void clearReplays() {
        actionPipe.send(new TestAction());
        actionPipe.clearReplays();
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observeWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithoutValues(subscriber);
    }

    @Test
    public void clearReplaysSuccess() {
        actionPipe.send(new TestAction());
        actionPipe.clearReplays();
        TestSubscriber<TestAction> subscriber = new TestSubscriber<TestAction>();
        actionPipe.observeSuccessWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithoutValues(subscriber);
    }

    @Test
    public void statusFail() throws JanetException {
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        doThrow(JanetException.class).when(service).sendInternal(any(ActionHolder.class));
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        AssertUtil.assertStatusCount(subscriber, ActionState.Status.FAIL, 1);
    }

    @Test
    public void statusFailFinish() throws JanetException {
        ActionStateSubscriber<TestAction> subscriber = new ActionStateSubscriber<TestAction>();
        Action1<TestAction> onFinish = mock(Action1.class);
        subscriber.onFinish(onFinish);

        actionPipe.clearReplays();
        doThrow(JanetException.class).when(service).sendInternal(any(ActionHolder.class));
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        subscriber.unsubscribe();
        verify(onFinish, times(1)).call(any(TestAction.class));
    }

    @Test
    public void statusSuccessFinish() {
        ActionStateSubscriber<TestAction> subscriber = new ActionStateSubscriber<TestAction>();
        Action1<TestAction> onFinish = mock(Action1.class);
        subscriber.onFinish(onFinish);

        actionPipe.clearReplays();
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        verify(onFinish, times(1)).call(any(TestAction.class));
    }
}
