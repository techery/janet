package io.techery.janet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

final class AsyncActionSynchronizer {

    final static long PENDING_TIMEOUT = 60 * 1000;
    final static int PENDING_ACTIONS_EVENT_LIMIT = 20;

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<AsyncActionWrapper>> pendingForResponse;

    private final OnCleanedListener cleanedListener;
    private final ScheduledExecutorService expireExecutor;


    AsyncActionSynchronizer(OnCleanedListener cleanedListener) {
        this.cleanedListener = cleanedListener;
        this.pendingForResponse = new ConcurrentHashMap<String, CopyOnWriteArrayList<AsyncActionWrapper>>();
        this.expireExecutor = Executors.newSingleThreadScheduledExecutor(new SingleNamedThreadFactory("AsyncActionSynchronizer-Expirer"));
    }

    void put(String event, AsyncActionWrapper wrapper) {
        CopyOnWriteArrayList<AsyncActionWrapper> cache = pendingForResponse.get(event);
        if (cache == null) {
            cache = new CopyOnWriteArrayList<AsyncActionWrapper>();
            CopyOnWriteArrayList<AsyncActionWrapper> _cache = pendingForResponse.putIfAbsent(event, cache);
            if (_cache != null) {
                cache = _cache;
            }
        }
        cache.add(wrapper);
        ScheduledFuture future = expireExecutor.schedule(new AsyncActionWrapperRunnable(wrapper) {
            @Override void onRun(AsyncActionWrapper wrapper) {
                onTimeout(wrapper);
            }
        }, wrapper.getResponseTimeout(), TimeUnit.MILLISECONDS);
        wrapper.setExpireFuture(future);
        if (cache.size() > PENDING_ACTIONS_EVENT_LIMIT) {
            AsyncActionWrapper removed = cache.remove(0);
            wrapper.cancelExpireFuture();
            if (cleanedListener != null && removed != null) {
                cleanedListener.onCleaned(removed, OnCleanedListener.Reason.LIMIT);
            }
        }
    }

    List<AsyncActionWrapper> sync(String event, Object responseAction, Predicate predicate) {
        if (contains(event)) {
            CopyOnWriteArrayList<AsyncActionWrapper> cache = pendingForResponse.get(event);
            List<AsyncActionWrapper> result = new ArrayList<AsyncActionWrapper>();
            for (AsyncActionWrapper wrapper : cache) {
                if (predicate.call(wrapper, responseAction)) {
                    result.add(wrapper);
                    wrapper.cancelExpireFuture();
                }
            }
            cache.removeAll(result);
            return result;
        }
        return Collections.emptyList();
    }

    boolean contains(String event) {
        return pendingForResponse.containsKey(event);
    }

    void remove(AsyncActionWrapper wrapper) {
        CopyOnWriteArrayList<AsyncActionWrapper> cache = pendingForResponse.get(wrapper.getResponseEvent());
        boolean removed = cache.remove(wrapper);
        if (removed && cleanedListener != null) {
            cleanedListener.onCleaned(wrapper, OnCleanedListener.Reason.CANCEL);
        }
    }

    private void onTimeout(AsyncActionWrapper wrapper) {
        CopyOnWriteArrayList<AsyncActionWrapper> cache = pendingForResponse.get(wrapper.getResponseEvent());
        boolean removed = cache.remove(wrapper);
        if (removed && cleanedListener != null) {
            cleanedListener.onCleaned(wrapper, OnCleanedListener.Reason.TIMEOUT);
        }
        wrapper.cancelExpireFuture();
    }

    private abstract static class AsyncActionWrapperRunnable implements Runnable {
        private final WeakReference<AsyncActionWrapper> wrapperReference;

        private AsyncActionWrapperRunnable(AsyncActionWrapper wrapper) {
            this.wrapperReference = new WeakReference<AsyncActionWrapper>(wrapper);
        }

        @Override public void run() {
            AsyncActionWrapper wrapper = wrapperReference.get();
            if (wrapper != null) {
                onRun(wrapper);
            }
        }

        abstract void onRun(AsyncActionWrapper wrapper);
    }

    private static class SingleNamedThreadFactory implements ThreadFactory {

        private final String name;

        private SingleNamedThreadFactory(String name) {
            this.name = name;
        }

        @Override public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, name);
            thread.setDaemon(true);
            return thread;
        }

    }

    interface Predicate {
        boolean call(AsyncActionWrapper wrapper, Object responseAction);
    }

    interface OnCleanedListener {

        enum Reason {TIMEOUT, LIMIT, CANCEL}

        void onCleaned(AsyncActionWrapper wrapper, Reason reason);
    }
}
