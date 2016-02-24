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
import io.techery.janet.http.exception.HttpAdapterException;
import io.techery.janet.http.exception.HttpException;
import io.techery.janet.http.model.Request;
import io.techery.janet.http.model.Response;

final public class HttpActionAdapter extends ActionAdapter {

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

    public HttpActionAdapter(String baseUrl, HttpClient client, Converter converter) {
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

    @Override Class getSupportedAnnotationType() {
        return HttpAction.class;
    }

    @Override <A> void sendInternal(A action) throws HttpAdapterException {
        callback.onStart(action);
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
            response = client.execute(request, new ActionRequestCallback<A>(action) {
                private int lastProgress;

                @Override public void onProgress(int progress) {
                    if (progress > lastProgress + PROGRESS_THRESHOLD) {
                        callback.onProgress(action, progress);
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
            throw new HttpAdapterException(e);
        } catch (ConverterException e) {
            throw new HttpAdapterException(e);
        } catch (HttpException e) {
            throw new HttpAdapterException(e);
        } catch (CancelException e) {
            return;
        } finally {
            runningActions.remove(action);
        }
        this.callback.onSuccess(action);
    }

    @Override <A> void cancel(A action) {
        runningActions.remove(action);
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

        protected final A action;

        private ActionRequestCallback(A action) {this.action = action;}

    }

    private static class CancelException extends Throwable {}


}
