package io.techery.janet.validation;

import java.util.HashSet;
import java.util.Set;

import io.techery.janet.AsyncActionClass;
import io.techery.janet.compiler.utils.validation.ValidationError;
import io.techery.janet.compiler.utils.validation.Validator;

public class MessageValidator implements Validator<AsyncActionClass> {

    @Override public Set<ValidationError> validate(AsyncActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        if (value.getMessageField() == null) {
            errors.add(new ValidationError("Action must have a message field", value.getTypeElement()));
        }
        return errors;
    }
}
