package io.techery.janet.sample.network;


import io.techery.janet.http.annotations.Status;

/**
 * This action class was created to show,
 * that action helper will be generated to fill the
 * annotated variables of super class too.
 */
public class BaseAction {

    @Status
    boolean success;

    public boolean isSuccess() {
        return success;
    }
}
