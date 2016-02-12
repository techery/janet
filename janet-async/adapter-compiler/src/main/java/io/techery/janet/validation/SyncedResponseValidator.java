package io.techery.janet.validation;

import java.util.Set;

import io.techery.janet.AsyncActionClass;
import io.techery.janet.compiler.utils.ActionClass;
import io.techery.janet.compiler.utils.validation.ValidationError;
import io.techery.janet.compiler.utils.validation.Validator;

public class SyncedResponseValidator implements Validator<AsyncActionClass> {

    @Override public Set<ValidationError> validate(AsyncActionClass value) {
        return null;
    }
}
