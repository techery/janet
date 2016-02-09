package io.techery.janet.sample.tools;

import android.util.Log;

import rx.Observable;
import rx.Observer;
import rx.plugins.DebugHook;
import rx.plugins.DebugNotification;
import rx.plugins.DebugNotificationListener;

import static rx.plugins.DebugNotification.quote;

public class AndroidLogHook extends DebugHook<Void> {

    private final static String TAG = "RXDebug";

    public AndroidLogHook() {
        super(new DebugNotificationListener<Void>() {
            @Override
            public <T> T onNext(DebugNotification<T> n) {
                Log.d(TAG, n.toString());
                return super.onNext(n);
            }

            @Override
            public <T> Void start(DebugNotification<T> n) {
                if (n.getKind() == DebugNotification.Kind.OnError) {
                    Log.w(TAG, String.format("Observable problem: %s", notificationToString(n)), n.getThrowable());
                }
                return super.start(n);
            }

            @Override
            public void error(Void context, Throwable e) {
                Log.e(TAG, "Observable error", e);
                super.error(context, e);
            }
        });
    }


    private static String notificationToString(DebugNotification n) {
        StringBuilder s = new StringBuilder("{");
        s.append("\"observer\": ");
        //
        Observer observer = n.getObserver();
        DebugNotification.Kind kind = n.getKind();
        Observable source = n.getSource();
        Observable.OnSubscribe sourceFunc = n.getSourceFunc();
        Observable.Operator from = n.getFrom();
        Observable.Operator to = n.getTo();
        //
        if (observer != null)
            s.append("\"")
                    .append(observer.getClass().getName())
                    .append("@")
                    .append(Integer.toHexString(observer.hashCode()))
                    .append("\"");
        else s.append("null");
        s.append(", \"type\": \"").append(kind).append("\"");
        if (kind == DebugNotification.Kind.OnNext)
            s.append(", \"value\": ").append(quote(n.getValue()));
        if (kind == DebugNotification.Kind.OnError) {
            Throwable throwable = n.getThrowable();
            s.append(", \"exception\": \"")
                    .append(throwable.getMessage() == null ? throwable : throwable.getMessage()
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\""))
                    .append("\"");
        }
        if (kind == DebugNotification.Kind.Request)
            s.append(", \"n\": ").append(n.getN());
        if (source != null)
            s.append(", \"source\": \"")
                    .append(source.getClass().getName())
                    .append("@")
                    .append(Integer.toHexString(source.hashCode()))
                    .append("\"");
        if (sourceFunc != null)
            s.append(", \"sourceFunc\": \"")
                    .append(sourceFunc.getClass().getName())
                    .append("@")
                    .append(Integer.toHexString(sourceFunc.hashCode()))
                    .append("\"");
        if (from != null)
            s.append(", \"from\": \"")
                    .append(from.getClass().getName())
                    .append("@")
                    .append(Integer.toHexString(from.hashCode()))
                    .append("\"");
        if (to != null)
            s.append(", \"to\": \"")
                    .append(to.getClass().getName())
                    .append("@")
                    .append(Integer.toHexString(to.hashCode()))
                    .append("\"");
        s.append("}");
        return s.toString();

    }
}
