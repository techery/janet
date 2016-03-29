package io.techery.janet;

import org.junit.Test;

import io.techery.janet.helper.ActionStateSubscriber;
import io.techery.janet.model.TestAction;
import rx.functions.Action0;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestPipeOperations extends BaseTest {

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
        actionPipe.observeWithReplay().subscribe(subscriber);
        actionPipe.send(new TestAction());
        subscriber.unsubscribe();
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);

        subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observeWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
    }

    @Test
    public void createObservableSuccess() {
        TestSubscriber<TestAction> subscriber = new TestSubscriber<TestAction>();
        TestAction action = new TestAction();
        actionPipe.createObservableSuccess(action).subscribe(subscriber);
        subscriber.unsubscribe();
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
        actionPipe.observeSuccessWithReplay().subscribe(subscriber);
        actionPipe.send(action);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);

        subscriber = new TestSubscriber<TestAction>();
        actionPipe.observeSuccessWithReplay()
                .subscribe(subscriber);
        subscriber.unsubscribe();
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);
    }

    @Test
    public void cancelAfterSend() {
        final TestAction action = new TestAction();
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>(
                new ActionStateSubscriber<TestAction>()
                        .onStart(new Action0() {
                            @Override public void call() {
                                actionPipe.cancel(action);
                            }
                        }));
        actionPipe.createObservable(action).subscribe(subscriber);
        subscriber.unsubscribe();
        subscriber.assertNoErrors();
        subscriber.assertUnsubscribed();
        AssertUtil.assertStatusCount(subscriber, ActionState.Status.START, 1);
        AssertUtil.assertStatusCount(subscriber, ActionState.Status.FAIL, 1);
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

}
