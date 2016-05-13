package io.techery.janet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.LinkedList;

import io.techery.janet.model.TestAction;
import io.techery.janet.util.StubServiceWrapper;
import rx.observers.TestSubscriber;

import static io.techery.janet.AssertUtil.assertStatusCount;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ActionServiceWrapper.class, ActionService.class, CallbackWrapper.class})
public class TestServiceWrapper extends BaseTest {

    @Test public void intercept() throws JanetException {
        ActionService actionService = provideService();
        StubServiceWrapper wrapperService = spy(new StubServiceWrapper(actionService) {
            @Override protected <A> boolean onInterceptSend(ActionHolder<A> holder) {
                return true;
            }
        });
        Janet janet = provideJanet(wrapperService);
        ActionService.Callback callback = spy(actionService.callback);
        wrapperService.callback = callback;
        actionService.callback = callback;
        ActionPipe<TestAction> actionPipe = providePipe(janet);
        //
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        subscriber.unsubscribe();
        //
        verify(wrapperService, times(1)).sendInternal(any(ActionHolder.class));
        verify(actionService, never()).sendInternal(any(ActionHolder.class));
        verify(callback, never()).onStart(any(ActionHolder.class));
        AssertUtil.assertSubscriberWithSingleValue(subscriber);
    }

    @Test public void passThrough() throws JanetException, Exception {
        ActionService actionService = provideService();
        StubServiceWrapper wrapperService = spy(new StubServiceWrapper(actionService) {
            @Override protected <A> boolean onInterceptSend(ActionHolder<A> holder) {
                return false;
            }
        });
        Janet janet = provideJanet(wrapperService);
        ActionService.Callback callback = spy(wrapperService.callback);
        wrapperService.callback = callback;
        actionService.callback = callback;
        ActionPipe<TestAction> actionPipe = providePipe(janet);
        //
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        subscriber.unsubscribe();
        //
        verify(wrapperService, times(1)).sendInternal(any(ActionHolder.class));
        verify(actionService, times(1)).sendInternal(any(ActionHolder.class));
        verify(callback, times(1)).onStart(any(ActionHolder.class));
        AssertUtil.SuccessAnswer.assertAllStatuses(subscriber);
    }

    @Test public void multiWrapping() throws JanetException, Exception {
        ActionService actionService = provideService();
        //
        doAnswer(new Answer() {
            @Override public Object answer(InvocationOnMock invocation) throws Throwable {
                ActionHolder holder = (ActionHolder) invocation.getArguments()[0];
                ActionService service = (ActionService) invocation.getMock();
                service.callback.onStart(holder);
                service.callback.onProgress(holder, 50);
                throw new JanetException();
            }
        }).when(actionService).sendInternal(any(ActionHolder.class));
        //
        LinkedList<StubServiceWrapper> wrappers = new LinkedList<StubServiceWrapper>();

        for (int i = 0; i < 3; i++) {
            StubServiceWrapper wrapper = new StubServiceWrapper(
                    wrappers.isEmpty() ? actionService : wrappers.getLast()
            );
            wrapper.setStubCallback(spy(StubServiceWrapper.StubCallback.class));
            wrappers.add(wrapper);
        }
        //
        Janet janet = provideJanet(wrappers.getLast());
        //
        ActionPipe<TestAction> actionPipe = providePipe(janet);
        //
        TestSubscriber<ActionState<TestAction>> subscriber = new TestSubscriber<ActionState<TestAction>>();
        actionPipe.createObservable(new TestAction()).subscribe(subscriber);
        subscriber.unsubscribe();
        //
        verify(actionService, times(1)).sendInternal(any(ActionHolder.class));
        for (StubServiceWrapper wrapper : wrappers) {
            verify(wrapper.getStubCallback(), times(1)).onInterceptSend(any(ActionHolder.class));
            verify(wrapper.getStubCallback(), times(1)).onInterceptStart(any(ActionHolder.class));
            verify(wrapper.getStubCallback(), times(1)).onInterceptProgress(any(ActionHolder.class), anyInt());
            verify(wrapper.getStubCallback(), times(1)).onInterceptFail(any(ActionHolder.class), any(JanetException.class));
        }
        //
        subscriber.assertNoErrors();
        subscriber.assertValueCount(3);
        subscriber.assertUnsubscribed();
        assertStatusCount(subscriber, ActionState.Status.START, 1);
        assertStatusCount(subscriber, ActionState.Status.PROGRESS, 1);
        assertStatusCount(subscriber, ActionState.Status.FAIL, 1);
    }

}
