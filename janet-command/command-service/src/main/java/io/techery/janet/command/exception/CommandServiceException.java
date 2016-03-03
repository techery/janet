package io.techery.janet.command.exception;

import io.techery.janet.CommandActionBase;
import io.techery.janet.JanetException;

public class CommandServiceException extends JanetException {

    public CommandServiceException(CommandActionBase action, Throwable cause) {
        super("Something went wrong with " + action.getClass().getSimpleName(), cause);
    }
}
