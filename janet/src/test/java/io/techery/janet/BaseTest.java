package io.techery.janet;

import org.junit.Before;

import io.reactivex.Scheduler;
import io.techery.janet.model.MockAction;
import io.techery.janet.model.TestAction;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class BaseTest {

    protected Janet janet;
    protected ActionService service;
    protected ActionPipe<TestAction> actionPipe;

    @Before
    public void setup() throws JanetException {
        service = provideService();
        janet = provideJanet(service);
        actionPipe = providePipe(janet);
    }

    protected ActionService provideService() throws JanetException {
        ActionService service = spy(ActionService.class);
        when(service.getSupportedAnnotationType()).thenReturn(MockAction.class);
        doAnswer(new AssertUtil.SuccessAnswer(service)).when(service).sendInternal(any(ActionHolder.class));
        return service;
    }

    protected Janet provideJanet(ActionService service) {
        return new Janet.Builder().addService(service).build();
    }

    protected ActionPipe<TestAction> providePipe(Janet janet) {
        return providePipe(janet, null);
    }

    protected ActionPipe<TestAction> providePipe(Janet janet, Scheduler scheduler) {
        return janet.createPipe(TestAction.class, scheduler);
    }

}
