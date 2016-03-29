package io.techery.janet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.techery.janet.model.TestAction;
import io.techery.janet.util.StubServiceWrapper;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
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
        AssertUtil.SuccessAnswer.assertNoStatuses(subscriber);
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

}
