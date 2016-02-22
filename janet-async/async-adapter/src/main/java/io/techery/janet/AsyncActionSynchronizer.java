package io.techery.janet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.DelayQueue;

final class AsyncActionSynchronizer {

    final static long PENDING_TIMEOUT = 60 * 1000;
    final static int PENDING_ACTIONS_EVENT_LIMIT = 20;

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<AsyncActionWrapper>> pendingForResponse;

    private final DelayQueue<AsyncActionWrapper> delayQueue;
    private final OnCleanedListener cleanedListener;


    AsyncActionSynchronizer(OnCleanedListener cleanedListener) {
        this.cleanedListener = cleanedListener;
        this.pendingForResponse = new ConcurrentHashMap<String, CopyOnWriteArrayList<AsyncActionWrapper>>();
        this.delayQueue = new DelayQueue<AsyncActionWrapper>();
    }

    void put(String event, AsyncActionWrapper wrapper) {
        cleanup();
        CopyOnWriteArrayList<AsyncActionWrapper> cache = pendingForResponse.get(event);
        if (cache == null) {
            cache = new CopyOnWriteArrayList<AsyncActionWrapper>();
            CopyOnWriteArrayList<AsyncActionWrapper> _cache = pendingForResponse.putIfAbsent(event, cache);
            if (_cache != null) {
                cache = _cache;
            }
        }
        cache.add(wrapper);
        delayQueue.add(wrapper);
        if (cache.size() > PENDING_ACTIONS_EVENT_LIMIT) {
            AsyncActionWrapper removed = cache.remove(0);
            if (cleanedListener != null && removed != null) {
                cleanedListener.onCleaned(removed, OnCleanedListener.Reason.LIMIT);
            }
        }
    }

    List<AsyncActionWrapper> sync(String event, Object responseAction, Predicate predicate) {
        cleanup();
        if (contains(event)) {
            CopyOnWriteArrayList<AsyncActionWrapper> cache = pendingForResponse.get(event);
            List<AsyncActionWrapper> result = new ArrayList<AsyncActionWrapper>();
            for (AsyncActionWrapper wrapper : cache) {
                if (predicate.call(wrapper, responseAction)) {
                    result.add(wrapper);
                }
            }
            cache.removeAll(result);
            return result;
        }
        return Collections.emptyList();
    }

    boolean contains(String event) {
        cleanup();
        return pendingForResponse.containsKey(event);
    }

    private void cleanup() {
        AsyncActionWrapper wrapper = delayQueue.poll();
        while (wrapper != null) {
            CopyOnWriteArrayList<AsyncActionWrapper> cache = pendingForResponse.get(wrapper.getResponseEvent());
            cache.remove(wrapper);
            if (cleanedListener != null) {
                cleanedListener.onCleaned(wrapper, OnCleanedListener.Reason.TIMEOUT);
            }
            wrapper = delayQueue.poll();
        }
    }

    interface Predicate {
        boolean call(AsyncActionWrapper wrapper, Object responseAction);
    }

    interface OnCleanedListener {

        enum Reason {TIMEOUT, LIMIT}

        void onCleaned(AsyncActionWrapper wrapper, Reason reason);
    }
}
