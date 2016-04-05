package io.techery.janet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.techery.janet.converter.Converter;
import io.techery.janet.converter.ConverterException;
import io.techery.janet.http.HttpClient;
import io.techery.janet.http.annotations.Body;
import io.techery.janet.http.annotations.Field;
import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.annotations.Part;
import io.techery.janet.http.annotations.Path;
import io.techery.janet.http.annotations.Query;
import io.techery.janet.http.annotations.RequestHeader;
import io.techery.janet.http.annotations.ResponseHeader;
import io.techery.janet.http.annotations.Status;
import io.techery.janet.http.exception.HttpException;
import io.techery.janet.http.exception.HttpServiceException;
import io.techery.janet.http.model.Request;
import io.techery.janet.http.model.Response;

/**
 * Provide HTTP/HTTPS requests execution. Each HTTP request for {@linkplain HttpActionService} is an individual class that contains
 * all information about the request and response. Http action must be annotated with {@linkplain HttpAction @HttpAction}.
 * <pre>{@code  @HttpAction(value = "/demo", method = HttpAction.Method.GET)
 * public class ExampleAction {}
 * }
 * </pre>
 * To configure request, Action fields can be annotated with:
 * <ul>
 * <li>{@linkplain Path @Path} for path value</li>
 * <li>{@linkplain Query @Query} for request URL parameters</li>
 * <li>{@linkplain Body @Body}  for POST request body</li>
 * <li>{@linkplain RequestHeader @RequestHeader} for request headers</li>
 * <li>{@linkplain Field @Field} for request fields if request type is {@linkplain HttpAction.Type#FORM_URL_ENCODED}</li>
 * <li>{@linkplain Part @Part} for multipart request parts</li>
 * </ul>
 * To process response, special annotations can be used:
 * <ul>
 * <li>{@linkplain io.techery.janet.http.annotations.Response @Response} for getting response body</li>
 * <li>{@linkplain Status @Status} for getting response status. Field types Integer, Long, int or long can be used
 * to get status code or use boolean to know that request was sent successfully</li>
 * <li>{@linkplain ResponseHeader @ResponseHeader} for getting response headers</li>
 * </ul>
 */
final public class HttpActionService extends ActionService {

    final static String HELPERS_FACTORY_CLASS_SIMPLE_NAME = "HttpActionHelperFactory";
    private final static String HELPERS_FACTORY_CLASS_NAME = Janet.class.getPackage()
            .getName() + "." + HELPERS_FACTORY_CLASS_SIMPLE_NAME;
    private final int PROGRESS_THRESHOLD = 5;

    private ActionHelperFactory actionHelperFactory;
    private final Map<Class, ActionHelper> actionHelperCache;
    private final Map<Object, List<Request>> runningRequests;

    private final HttpClient client;
    private final Converter converter;
    private final String baseUrl;

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
        try {
            new URL(baseUrl);
        } catch (MalformedURLException t) {
            throw new IllegalArgumentException("baseUrl is not valid", t);
        }
        this.baseUrl = baseUrl;
        this.client = client;
        this.converter = converter;
        this.actionHelperCache = new HashMap<Class, ActionHelper>();
        this.runningRequests = new ConcurrentHashMap<Object, List<Request>>();
        loadActionHelperFactory();
    }

    @Override protected Class getSupportedAnnotationType() {
        return HttpAction.class;
    }

    @Override protected <A> void sendInternal(ActionHolder<A> holder) throws HttpServiceException {
        callback.onStart(holder);
        A action = holder.action();
        final ActionHelper<A> helper = getActionHelper(action.getClass());
        if (helper == null) {
            throw new JanetInternalException("Something was happened with code generator. Check dependence of janet-http-compiler");
        }
        putRunningAction(action);
        RequestBuilder builder = new RequestBuilder(baseUrl, converter);
        Response response;
        Request request = null;
        try {
            builder = helper.fillRequest(builder, action);
            request = builder.build();
            putRunningRequest(action, request);
            throwIfCanceled(action, request);
            try {
                response = client.execute(request, new ActionRequestCallback<A>(holder) {
                    private int lastProgress;

                    @Override public void onProgress(int progress) {
                        if (progress > lastProgress + PROGRESS_THRESHOLD) {
                            callback.onProgress(holder, progress);
                            lastProgress = progress;
                        }
                    }
                });
            } finally {
                throwIfCanceled(action, request);
            }
            action = helper.onResponse(action, response, converter);
            if (!response.isSuccessful()) {
                throw new HttpException(response);
            }
            throwIfCanceled(action, request);
        } catch (CancelException e) {
            return;
        } catch (Throwable e) {
            throw new HttpServiceException(e);
        } finally {
            if (request != null) {
                List<Request> requests = runningRequests.get(action);
                if (requests != null) {
                    requests.remove(request);
                }
            } else {
                runningRequests.remove(action);
            }
        }
        this.callback.onSuccess(holder);
    }

    @Override protected <A> void cancel(ActionHolder<A> holder) {
        A action = holder.action();
        List<Request> requests = runningRequests.remove(action);
        try {
            if (requests != null) {
                for (Request request : requests) {
                    client.cancel(request);
                }
            }
        } catch (Throwable ignored) {}
    }

    private void putRunningRequest(Object action, Request request) {
        putRunningAction(action);
        runningRequests.get(action).add(request);
    }

    private void putRunningAction(Object action) {
        if (!runningRequests.containsKey(action)) {
            runningRequests.put(action, new CopyOnWriteArrayList<Request>());
        }
    }

    private void throwIfCanceled(Object action, Request request) throws CancelException {
        List<Request> requests = runningRequests.get(action);
        if (requests == null || !requests.contains(request)) {
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

}
