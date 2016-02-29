package io.techery.janet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.techery.janet.converter.Converter;
import io.techery.janet.converter.ConverterException;
import io.techery.janet.http.HttpClient;
import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.exception.HttpServiceException;
import io.techery.janet.http.exception.HttpException;
import io.techery.janet.http.model.Request;
import io.techery.janet.http.model.Response;

final public class HttpActionService extends ActionService {

    final static String HELPERS_FACTORY_CLASS_SIMPLE_NAME = "HttpActionHelperFactory";
    private final static String HELPERS_FACTORY_CLASS_NAME = Janet.class.getPackage()
            .getName() + "." + HELPERS_FACTORY_CLASS_SIMPLE_NAME;
    private final int PROGRESS_THRESHOLD = 5;

    private ActionHelperFactory actionHelperFactory;
    private final Map<Class, ActionHelper> actionHelperCache = new HashMap<Class, ActionHelper>();

    private final HttpClient client;
    private final Converter converter;
    private final String baseUrl;
    private final List<Object> runningActions;

    public HttpActionService(String baseUrl, HttpClient client, Converter converter) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("baseUrl == null");
        }
        if (client == null) {
            throw new IllegalArgumentException("client == null");
        }
        if (converter == null) {
            throw new IllegalArgumentException("converter == null");
        }
        this.baseUrl = baseUrl;
        this.client = client;
        this.converter = converter;
        this.runningActions = new CopyOnWriteArrayList<Object>();
        loadActionHelperFactory();
    }

    @Override protected Class getSupportedAnnotationType() {
        return HttpAction.class;
    }

    @Override protected <A> void sendInternal(ActionHolder<A> holder) throws HttpServiceException {
        callback.onStart(holder);
        A action = holder.action();
        runningActions.add(action);
        final ActionHelper<A> helper = getActionHelper(action.getClass());
        if (helper == null) {
            throw new JanetInternalException("Something was happened with code generator. Check dependence of janet-http-compiler");
        }
        RequestBuilder builder = new RequestBuilder(baseUrl, converter);
        Response response;
        try {
            builder = helper.fillRequest(builder, action);
            Request request = builder.build();
            throwIfCanceled(action);
            response = client.execute(request, new ActionRequestCallback<A>(holder) {
                private int lastProgress;

                @Override public void onProgress(int progress) {
                    if (progress > lastProgress + PROGRESS_THRESHOLD) {
                        callback.onProgress(holder, progress);
                        lastProgress = progress;
                    }
                }
            });
            throwIfCanceled(action);
            if (!response.isSuccessful()) {
                throw new HttpException(response.getStatus(), response.getReason());
            }
            action = helper.onResponse(action, response, converter);
        } catch (IOException e) {
            throw new HttpServiceException(e);
        } catch (ConverterException e) {
            throw new HttpServiceException(e);
        } catch (HttpException e) {
            throw new HttpServiceException(e);
        } catch (CancelException e) {
            return;
        } finally {
            runningActions.remove(action);
        }
        this.callback.onSuccess(holder);
    }

    @Override protected <A> void cancel(ActionHolder<A> holder) {
        runningActions.remove(holder.action());
    }

    private void throwIfCanceled(Object action) throws CancelException {
        if (!runningActions.contains(action)) {
            throw new CancelException();
        }
    }

    private ActionHelper getActionHelper(Class actionClass) {
        ActionHelper helper = actionHelperCache.get(actionClass);
        if (helper == null && actionHelperFactory != null) {
            synchronized (actionHelperFactory) {
                helper = actionHelperFactory.make(actionClass);
                actionHelperCache.put(actionClass, helper);
            }
        }
        return helper;
    }

    private void loadActionHelperFactory() {
        try {
            Class<? extends ActionHelperFactory> clazz
                    = (Class<? extends ActionHelperFactory>) Class.forName(HELPERS_FACTORY_CLASS_NAME);
            actionHelperFactory = clazz.newInstance();
        } catch (Exception e) {
            throw new JanetInternalException("Can't initialize ActionHelperFactory - generator failed", e);
        }
    }

    interface ActionHelperFactory {
        ActionHelper make(Class actionClass);
    }

    public interface ActionHelper<T> {
        RequestBuilder fillRequest(RequestBuilder requestBuilder, T action) throws ConverterException;

        T onResponse(T action, Response response, Converter converter) throws ConverterException;
    }

    private static abstract class ActionRequestCallback<A> implements HttpClient.RequestCallback {

        protected final ActionHolder<A> holder;

        private ActionRequestCallback(ActionHolder<A> holder) {this.holder = holder;}

    }

    private static class CancelException extends Throwable {}


}
