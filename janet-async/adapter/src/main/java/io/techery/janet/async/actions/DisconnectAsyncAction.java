package io.techery.janet.async.actions;

import io.techery.janet.async.annotations.AsyncAction;

@AsyncAction(value = "disconnect", incoming = true)
final public class DisconnectAsyncAction implements SystemAction {

    private String reason;

    public DisconnectAsyncAction() {
        this("unknown");
    }

    public DisconnectAsyncAction(String reason) {this.reason = reason;}

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
