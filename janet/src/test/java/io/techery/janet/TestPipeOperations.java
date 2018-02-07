package io.techery.janet;

import org.junit.Test;

import java.util.concurrent.Executor;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import io.techery.janet.helper.ActionStateSubscriber;
import io.techery.janet.model.TestAction;
import io.techery.janet.util.FakeExecutor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestPipeOperations extends BaseTest {


    @Test
    public void createObservable() {
        TestSubscriber<ActionState<TestAction>> subscriber =
                actionPipe.createObservable(new TestAction()).test();
        subscriber.dispose();
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);
    }

    @Test
    public void sendWithObserve() {
        TestSubscriber<ActionState<TestAction>> subscriber =
                actionPipe.observe().test();
        actionPipe.send(new TestAction());
        subscriber.dispose();
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);
    }

    @Test
    public void sendWithObserveWithReplay() {
        TestSubscriber<ActionState<TestAction>> subscriber;

        actionPipe.send(new TestAction());
        subscriber = actionPipe.observeWithReplay().test();
        subscriber.dispose();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);

        subscriber = actionPipe.observeWithReplay().test();
        subscriber.dispose();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
    }

    @Test
    public void createObservableSuccess() {
        TestAction action = new TestAction();
        TestSubscriber<TestAction> subscriber =
                actionPipe.createObservableResult(action).toFlowable().test();
        subscriber.dispose();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);
    }

    @Test
    public void sendWithObserveSuccess() {
        TestAction action = new TestAction();
        TestSubscriber<TestAction> subscriber =
                actionPipe.observeSuccess().test();
        actionPipe.send(action);
        subscriber.dispose();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);
    }

    @Test
    public void sendWithObserveSuccessWithReplay() {
        TestSubscriber<TestAction> subscriber;

        TestAction action = new TestAction();
        actionPipe.send(action);
        subscriber = actionPipe.observeSuccessWithReplay().test();
        subscriber.dispose();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);

        subscriber = actionPipe.observeSuccessWithReplay().test();
        subscriber.dispose();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);
    }

    @Test
    public void sendWithDefaultScheduler() {
        Executor fakeExecutor = spy(new FakeExecutor());
        //
        ActionPipe<TestAction> actionPipe = providePipe(janet, Schedulers.from(fakeExecutor));
        TestSubscriber<ActionState<TestAction>> subscriber = actionPipe.observe().test();
        actionPipe.send(new TestAction());
        subscriber.dispose();
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);
        verify(fakeExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    public void sendWithOverridenScheduler() {
        Executor defaultExecutor = spy(new FakeExecutor());
        Executor newExecutor = spy(new FakeExecutor());
        //
        ActionPipe<TestAction> actionPipe = providePipe(janet, Schedulers.from(defaultExecutor));
        TestSubscriber<ActionState<TestAction>> subscriber = actionPipe.observe().test();
        actionPipe.send(new TestAction(), Schedulers.from(newExecutor));
        subscriber.dispose();
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);
        verify(defaultExecutor, never()).execute(any(Runnable.class));
        verify(newExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    public void cancelActionAfterStart() {
        final TestAction action = new TestAction();
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<>(
                new ActionStateSubscriber<TestAction>().onStart(testAction -> actionPipe.cancel(action))
        );
        actionPipe.createObservable(action).subscribe(subscriber);
        subscriber.dispose();
        AssertUtil.assertCanceled(subscriber);
        verify(service, times(1)).cancel(any(ActionHolder.class));
    }

    @Test
    public void cancelLatestAfterStart() {
        final TestAction action = new TestAction();
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<>(
                new ActionStateSubscriber<TestAction>().onStart(testAction -> actionPipe.cancelLatest())
        );
        actionPipe.createObservable(action).subscribe(subscriber);
        subscriber.dispose();
        AssertUtil.assertCanceled(subscriber);
        verify(service, times(1)).cancel(any(ActionHolder.class));
    }

    @Test
    public void clearReplays() {
        actionPipe.send(new TestAction());
        actionPipe.clearReplays();
        TestSubscriber<ActionState<TestAction>> subscriber =
                actionPipe.observeWithReplay().test();
        subscriber.dispose();
        AssertUtil.assertSubscriberWithoutValues(subscriber);
    }

    @Test
    public void clearReplaysSuccess() {
        actionPipe.send(new TestAction());
        actionPipe.clearReplays();
        TestSubscriber<TestAction> subscriber =
                actionPipe.observeSuccessWithReplay().test();
        subscriber.dispose();
        AssertUtil.assertSubscriberWithoutValues(subscriber);
    }

    @Test
    public void statusFail() throws JanetException {
        doThrow(JanetException.class).when(service).sendInternal(any(ActionHolder.class));
        TestSubscriber<ActionState<TestAction>> subscriber =
                actionPipe.createObservable(new TestAction()).test();
        subscriber.dispose();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        AssertUtil.assertStatusCount(subscriber, ActionState.Status.FAIL, 1);
    }

    @Test
    public void statusFailFinish() throws JanetException, Exception {
        ActionStateSubscriber<TestAction> subscriber = new ActionStateSubscriber<>();
        Consumer<TestAction> onFinish = mock(Consumer.class);
        subscriber.onFinish(onFinish);

        actionPipe.clearReplays();
        doThrow(JanetException.class).when(service).sendInternal(any(ActionHolder.class));
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        subscriber.dispose();
        verify(onFinish, times(1)).accept(any(TestAction.class));
    }

    @Test
    public void statusSuccessFinish() throws Exception {
        ActionStateSubscriber<TestAction> subscriber = new ActionStateSubscriber<>();
        Consumer<TestAction> onFinish = mock(Consumer.class);
        subscriber.onFinish(onFinish);

        actionPipe.clearReplays();
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        verify(onFinish, times(1)).accept(any(TestAction.class));
    }

    @Test
    public void testBackpressure() {
        TestSubscriber<String> subscriber;
        subscriber = Flowable.range(0, 10)
                .map(integer -> actionPipe.observe())
                .flatMap(f -> f.mergeWith(actionPipe.observe()))
                .map(state -> String.valueOf(state))
                .distinct() //reduce pressure for subscriber
                .test();
        int magicLoopSize = 100;
        Observable.range(0, magicLoopSize)
                .observeOn(Schedulers.io())
                .subscribe(integer -> actionPipe.send(new TestAction()));
        Observable.range(0, magicLoopSize)
                .subscribe(integer -> actionPipe.send(new TestAction()));
        subscriber.dispose();
        subscriber.assertNotComplete();
        subscriber.assertNoErrors();
    }
}
