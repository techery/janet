package io.techery.janet;

import java.util.concurrent.CopyOnWriteArrayList;

import io.techery.janet.command.annotations.CommandAction;
import io.techery.janet.command.exception.CommandAdapterException;

final public class CommandActionAdapter extends ActionAdapter {

    private final CopyOnWriteArrayList<CommandActionBase> runningActions;

    public CommandActionAdapter() {
        this.runningActions = new CopyOnWriteArrayList<CommandActionBase>();
    }

    @Override protected Class getSupportedAnnotationType() {
        return CommandAction.class;
    }

    @SuppressWarnings("unchecked")
    @Override protected <A> void sendInternal(A action) throws JanetException {
        callback.onStart(action);
        CommandActionBase commandAction = checkAndCast(action);
        runningActions.add(commandAction);
        try {
            Object result = commandAction.run(new ActionProgressInvoker<A>(action, callback));
            if (isCanceled(commandAction)) return;
            commandAction.setResult(result);
        } catch (Throwable t) {
            throw new CommandAdapterException(
                    String.format("Something went wrong with %s", action.getClass().getCanonicalName()), t
            );
        } finally {
            runningActions.remove(commandAction);
        }
        callback.onSuccess(action);
    }

    @Override protected <A> void cancel(A action) {
        CommandActionBase commandAction = checkAndCast(action);
        commandAction.cancel();
        runningActions.remove(commandAction);
    }

    private boolean isCanceled(CommandActionBase commandAction) {
        return !runningActions.contains(commandAction);
    }

    private CommandActionBase checkAndCast(Object action) {
        if (!(action instanceof CommandActionBase)) {
            throw new JanetInternalException(String.format("%s must extend %s", action.getClass()
                    .getCanonicalName(), CommandActionBase.class.getCanonicalName()));
        }
        return (CommandActionBase) action;
    }

    private static class ActionProgressInvoker<A> implements CommandActionBase.CommandCallback {

        private final A action;
        private final Callback callback;

        private ActionProgressInvoker(A action, Callback callback) {
            this.action = action;
            this.callback = callback;
        }

        @Override public void onProgress(int progress) {
            callback.onProgress(action, progress);
        }
    }
}
