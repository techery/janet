package io.techery.janet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import io.techery.janet.helper.ActionStateSubscriber;
import rx.functions.Action0;
import rx.observers.TestSubscriber;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JanetTest {


    private Janet janet;
    private ActionService service;
    private ActionPipe<TestAction> actionPipe;

    @Before
    public void setup() throws JanetException {
        service = spy(ActionService.class);
        when(service.getSupportedAnnotationType()).thenReturn(MockAction.class);
        doAnswer(new SuccessAnswer(service)).when(service).sendInternal(any(ActionHolder.class));
        janet = new Janet.Builder().addService(service).build();
        actionPipe = janet.createPipe(TestAction.class);
    }

    @Test
    public void createObservable() {
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        assertSubscriberWithStates(subscriber);
    }

    @Test
    public void sendWithObserve() {
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observe().subscribe(subscriber);
        actionPipe.send(new TestAction());
        subscriber.unsubscribe();
        assertSubscriberWithStates(subscriber);
    }

    @Test
    public void sendWithObserveWithReplay() {
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observeWithReplay().subscribe(subscriber);
        actionPipe.send(new TestAction());
        subscriber.unsubscribe();
        assertSubscriberWithStates(subscriber);

        subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observeWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        assertSubscriberWithSingleValue(subscriber);
    }

    @Test
    public void createObservableSuccess() {
        TestSubscriber<TestAction> subscriber = new TestSubscriber<TestAction>();
        TestAction action = new TestAction();
        actionPipe.createObservableSuccess(action).subscribe(subscriber);
        subscriber.unsubscribe();
        assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);
    }

    @Test
    public void sendWithObserveSuccess() {
        TestSubscriber<TestAction> subscriber = new TestSubscriber<TestAction>();
        TestAction action = new TestAction();
        actionPipe.observeSuccess().subscribe(subscriber);
        actionPipe.send(action);
        subscriber.unsubscribe();
        assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);
    }

    @Test
    public void sendWithObserveSuccessWithReplay() {
        TestSubscriber<TestAction> subscriber = new TestSubscriber<TestAction>();
        TestAction action = new TestAction();
        actionPipe.observeSuccessWithReplay().subscribe(subscriber);
        actionPipe.send(action);
        subscriber.unsubscribe();
        assertSubscriberWithSingleValue(subscriber);
        subscriber.assertValue(action);

        subscriber = new TestSubscriber<TestAction>();
        actionPipe.observeSuccessWithReplay()
                .subscribe(subscriber);
        subscriber.unsubscribe();
        assertSubscriberWithSingleValue(subscriber);
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
        List<ActionState<TestAction>> values = subscriber.getOnNextEvents();
        assertStatusCount(values, ActionState.Status.START, 1);
        assertStatusCount(values, ActionState.Status.FAIL, 1);
        Assert.assertThat(values.get(1).exception, instanceOf(CancelException.class));
        verify(service, times(1)).cancel(any(ActionHolder.class));
    }

    @Test
    public void clearReplays() {
        actionPipe.send(new TestAction());
        actionPipe.clearReplays();
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.observeWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        assertSubscriberWithoutValues(subscriber);
    }

    @Test
    public void clearReplaysSuccess() {
        actionPipe.send(new TestAction());
        actionPipe.clearReplays();
        TestSubscriber<TestAction> subscriber = new TestSubscriber<TestAction>();
        actionPipe.observeSuccessWithReplay().subscribe(subscriber);
        subscriber.unsubscribe();
        assertSubscriberWithoutValues(subscriber);
    }

    @Test
    public void statusFail() throws JanetException {
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        doThrow(JanetException.class).when(service).sendInternal(any(ActionHolder.class));
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        subscriber.unsubscribe();
        assertSubscriberWithSingleValue(subscriber);
        assertStatusCount(subscriber.getOnNextEvents(), ActionState.Status.FAIL, 1);
    }


    private static void assertSubscriberWithStates(TestSubscriber<ActionState<TestAction>> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertValueCount(4);
        subscriber.assertUnsubscribed();
        List<ActionState<TestAction>> values = subscriber.getOnNextEvents();
        assertStatusCount(values, ActionState.Status.START, 1);
        assertStatusCount(values, ActionState.Status.PROGRESS, 2);
        assertStatusCount(values, ActionState.Status.SUCCESS, 1);
    }

    private static void assertSubscriberWithSingleValue(TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        subscriber.assertUnsubscribed();
    }

    private static void assertSubscriberWithoutValues(TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        subscriber.assertUnsubscribed();
    }

    private static void assertStatusCount(List<ActionState<TestAction>> values, ActionState.Status status, int count) {
        int i = 0;
        for (ActionState state : values) {
            if (status == state.status) {
                i++;
            }
        }
        if (i != count) {
            throw new AssertionError("Number of events with status " + status + " differ; expected: " + count + ", actual: " + i);
        }
    }

    @MockAction
    private static class TestAction {}

    @Target(TYPE)
    @Retention(RUNTIME)
    private @interface MockAction {}

    private static class SuccessAnswer implements Answer<Void> {

        private final ActionService service;

        private SuccessAnswer(ActionService service) {this.service = service;}

        @Override public Void answer(InvocationOnMock invocation) throws Throwable {
            ActionHolder holder = (ActionHolder) invocation.getArguments()[0];
            service.callback.onStart(holder);
            service.callback.onProgress(holder, 1);
            service.callback.onProgress(holder, 99);
            service.callback.onSuccess(holder);
            return null;
        }
    }
}
