package io.techery.janet;

import java.util.HashMap;
import java.util.Map;

import io.techery.janet.converter.Converter;
import io.techery.janet.http.HttpClient;
import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.model.Request;
import io.techery.janet.http.model.Response;

final public class HttpActionAdapter extends ActionAdapter {

    final static String HELPERS_FACTORY_CLASS_SIMPLE_NAME = "ActionHelperFactoryImpl";
    private final static String HELPERS_FACTORY_CLASS_NAME = Janet.class.getPackage()
            .getName() + "." + HELPERS_FACTORY_CLASS_SIMPLE_NAME;

    private ActionHelperFactory actionHelperFactory;
    private final Map<Class, ActionHelper> actionHelperCache = new HashMap<Class, ActionHelper>();

    private final HttpClient client;
    private final Converter converter;
    private final String baseUrl;
    private Callback callback;

    public HttpActionAdapter(String baseUrl, HttpClient client, Converter converter) {
        this.baseUrl = baseUrl;
        this.client = client;
        this.converter = converter;
        loadActionHelperFactory();
    }

    @Override <A> void send(A action) {
        try {
            sendInternal(action);
        } catch (Exception e) {
            if (!(e instanceof JanetServerException)) {
                this.callback.onFail(action, e);
            }
        }
    }

    <A> void sendInternal(A action) throws Exception {
        callback.onStart(action);
        final ActionHelper<A> helper = getActionHelper(action.getClass());
        if (helper == null) {
            throw new JanetInternalException("Something was happened with code generator. Check dependence of janet-http-compiler");
        }
        RequestBuilder builder = new RequestBuilder(baseUrl, converter);
        builder = helper.fillRequest(builder, action);
        Request request = builder.build();
        Response response = client.execute(request);
        action = helper.onResponse(action, response, converter);
        if (!response.isSuccessful()) { //throw exception to change action state
            callback.onServerError(action);
        }
        this.callback.onSuccess(action);
    }

    @Override void setOnResponseCallback(Callback callback) {
        this.callback = callback;
    }


    @Override Class getActionAnnotationClass() {
        return HttpAction.class;
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
            //do nothing. actionHelperFactory will be checked on send()
        }
    }

    interface ActionHelperFactory {
        ActionHelper make(Class actionClass);
    }

    public interface ActionHelper<T> {
        RequestBuilder fillRequest(RequestBuilder requestBuilder, T action);

        T onResponse(T action, Response response, Converter converter);
    }


}
