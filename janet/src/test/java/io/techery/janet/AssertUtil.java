package io.techery.janet;

import org.junit.Assert;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.reactivex.subscribers.TestSubscriber;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertTrue;

public final class AssertUtil {

    private AssertUtil() {
    }

    public static <T> void assertSubscriberWithSingleValue(TestSubscriber<T> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        assertTrue(subscriber.isDisposed());
    }

    public static <T> void assertSubscriberWithoutValues(TestSubscriber<T> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertNoValues();
        assertTrue(subscriber.isDisposed());
    }

    public static <T> void assertCanceled(TestSubscriber<ActionState<T>> subscriber) {
        subscriber.assertNoErrors();
        assertTrue(subscriber.isDisposed());
        AssertUtil.assertStatusCount(subscriber, ActionState.Status.START, 1);
        AssertUtil.assertStatusCount(subscriber, ActionState.Status.FAIL, 1);
        JanetException exception = subscriber.values().get(subscriber.values().size() - 1).exception;
        Assert.assertThat(exception, instanceOf(CancelException.class));
    }

    public static <T> void assertStatusCount(TestSubscriber<ActionState<T>> subscriber, ActionState.Status status, int count) {
        int i = 0;
        for (Object state : subscriber.getEvents().get(0)) {
            if (status == ((ActionState) state).status) {
                i++;
            }
        }
        if (i != count) {
            throw new AssertionError("Number of events with status " + status + " differ; expected: " + count + ", actual: " + i);
        }
    }

    public static class SuccessAnswer implements Answer<Void> {

        private final ActionService service;

        public SuccessAnswer(ActionService service) {
            this.service = service;
        }

        @Override public Void answer(InvocationOnMock invocation) throws Throwable {
            ActionHolder holder = (ActionHolder) invocation.getArguments()[0];
            service.callback.onStart(holder);
            service.callback.onProgress(holder, 1);
            service.callback.onProgress(holder, 99);
            service.callback.onSuccess(holder);
            return null;
        }

        public static <T> void assertAllStatuses(TestSubscriber<ActionState<T>> subscriber) {
            subscriber.assertNoErrors();
            subscriber.assertValueCount(4);
            assertTrue(subscriber.isDisposed());
            assertStatusCount(subscriber, ActionState.Status.START, 1);
            assertStatusCount(subscriber, ActionState.Status.PROGRESS, 2);
            assertStatusCount(subscriber, ActionState.Status.SUCCESS, 1);
        }

        public static <T> void assertNoStatuses(TestSubscriber<ActionState<T>> subscriber) {
            subscriber.assertNoErrors();
            subscriber.assertValueCount(0);
            assertTrue(subscriber.isDisposed());
        }
    }
}
