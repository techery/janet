package io.techery.janet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;

final class AsyncActionSynchronizer {

    final static long PENDING_TIMEOUT = 60 * 5 * 1000; //5 mins

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<AsyncActionWrapper>> pendingForResponse;

    private final DelayQueue<AsyncActionWrapper> delayQueue;


    AsyncActionSynchronizer() {
        pendingForResponse = new ConcurrentHashMap<String, ConcurrentLinkedQueue<AsyncActionWrapper>>();
        delayQueue = new DelayQueue<AsyncActionWrapper>();
    }


    void put(String event, AsyncActionWrapper wrapper) {
        cleanup();
        ConcurrentLinkedQueue<AsyncActionWrapper> queue = pendingForResponse.get(event);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<AsyncActionWrapper>();
            ConcurrentLinkedQueue<AsyncActionWrapper> _queue = pendingForResponse.putIfAbsent(event, queue);
            if (_queue != null) {
                queue = _queue;
            }
        }
        queue.add(wrapper);
        delayQueue.add(wrapper);
    }

    List<AsyncActionWrapper> poll(String event) {
        cleanup();
        if (contains(event)) {
            ConcurrentLinkedQueue<AsyncActionWrapper> queue = pendingForResponse.get(event);
            List<AsyncActionWrapper> result = new ArrayList<AsyncActionWrapper>();
            AsyncActionWrapper wrapper = queue.poll();
            while (wrapper != null) {
                result.add(wrapper);
                wrapper = queue.poll();
            }
            return result;
        }
        return Collections.emptyList();
    }

    boolean contains(String event) {
        cleanup();
        return pendingForResponse.containsKey(event);
    }

    private void cleanup(){
        AsyncActionWrapper wrapper = delayQueue.poll();
        while (wrapper != null) {
            ConcurrentLinkedQueue<AsyncActionWrapper> queue = pendingForResponse.get(wrapper.getResponseEvent());
            queue.remove(wrapper);
            wrapper = delayQueue.poll();
        }
    }

}
