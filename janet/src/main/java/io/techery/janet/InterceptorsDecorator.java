package io.techery.janet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class InterceptorsDecorator implements Interceptor {

    private Map<ActionState.Status, LinkedList<Interceptor>> interceptorsMap;

    InterceptorsDecorator() {
        interceptorsMap = new HashMap<ActionState.Status, LinkedList<Interceptor>>();
        //init map keys
        for (ActionState.Status status : ActionState.Status.values()) {
            interceptorsMap.put(status, new LinkedList<Interceptor>());
        }
    }

    void add(ActionState.Status status, Interceptor interceptor) {
        interceptorsMap.get(status).add(interceptor);
    }

    @Override public void intercept(ActionState state) {
        for (Interceptor interceptor : interceptorsMap.get(state.status)) {
            interceptor.intercept(state);
        }
    }
}
