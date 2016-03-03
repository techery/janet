package io.techery.janet;

import io.techery.janet.command.annotations.CommandAction;
import io.techery.janet.command.exception.CommandServiceException;

final public class CommandActionService extends ActionService {

    @Override protected Class getSupportedAnnotationType() {
        return CommandAction.class;
    }

    @SuppressWarnings("unchecked")
    @Override protected <A> void sendInternal(ActionHolder<A> holder) throws JanetException {
        callback.onStart(holder);
        CommandActionBase action = checkAndCast(holder.action());
        try {
            action.run(new ActionProgressInvoker((ActionHolder<CommandActionBase>) holder, callback));
        } catch (Throwable t) {
            if (action.isCanceled()) return;
            throw new CommandServiceException(
                    String.format("Something went wrong with %s", action.getClass().getCanonicalName()), t
            );
        }
    }

    @Override protected <A> void cancel(ActionHolder<A> holder) {
        CommandActionBase action = checkAndCast(holder.action());
        action.cancel();
        action.setCanceled(true);
    }

    private static CommandActionBase checkAndCast(Object action) {
        if (!(action instanceof CommandActionBase)) {
            throw new JanetInternalException(String.format("%s must extend %s", action.getClass()
                    .getCanonicalName(), CommandActionBase.class.getCanonicalName()));
        }
        return (CommandActionBase) action;
    }

    private static class ActionProgressInvoker implements CommandActionBase.CommandCallback {

        private final ActionHolder<CommandActionBase> holder;
        private final Callback callback;

        private ActionProgressInvoker(ActionHolder<CommandActionBase> holder, Callback callback) {
            this.holder = holder;
            this.callback = callback;
        }

        @Override public void onProgress(int progress) {
            callback.onProgress(holder, progress);
        }

        @Override public void onSuccess(Object result) {
            if (!holder.action().isCanceled()) {
                holder.action().setResult(result);
                callback.onSuccess(holder);
            }
        }

        @Override public void onFail(Throwable throwable) {
            if (!holder.action().isCanceled()) {
                callback.onFail(holder, new CommandServiceException("Something went wrong with " + holder.action()
                        .getClass()
                        .getSimpleName(), throwable));
            }
        }
    }
}
