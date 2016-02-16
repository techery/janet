package io.techery.janet.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.techery.janet.AsyncActionClass;
import io.techery.janet.async.annotations.SyncedResponse;
import io.techery.janet.async.annotations.AsyncMessage;
import io.techery.janet.compiler.utils.validation.AnnotationQuantityValidator;
import io.techery.janet.compiler.utils.validation.FieldsModifiersValidator;
import io.techery.janet.compiler.utils.validation.ValidationError;
import io.techery.janet.compiler.utils.validation.Validator;

public class AsyncActionValidators implements Validator<AsyncActionClass> {

    private final List<Validator<AsyncActionClass>> validators;

    public AsyncActionValidators() {
        validators = new ArrayList<Validator<AsyncActionClass>>();
        validators.add(new FieldsModifiersValidator<AsyncActionClass>());
        validators.add(new MessageValidator());
        validators.add(new TypeIncomingValidator());
        validators.add(new SyncedResponseValidator());
        validators.add(new AnnotationQuantityValidator<AsyncActionClass>(AsyncMessage.class, 1));
        validators.add(new AnnotationQuantityValidator<AsyncActionClass>(SyncedResponse.class, 1));
    }

    @Override
    public Set<ValidationError> validate(AsyncActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        for (Validator<AsyncActionClass> validator : validators) {
            errors.addAll(validator.validate(value));
        }
        return errors;
    }
}
