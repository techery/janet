package io.techery.janet;

import java.util.concurrent.ConcurrentHashMap;

abstract class AsyncActionsRosterBase {

    protected final ConcurrentHashMap<String, Class> map = new ConcurrentHashMap<String, Class>();

    Class getActionClass(String event) {
        return map.get(event);
    }

    boolean containsEvent(String event) {
        return map.containsKey(event);
    }
}