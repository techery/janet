package io.techery.janet.validation;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;

import io.techery.janet.AsyncActionClass;
import io.techery.janet.async.annotations.SyncedResponse;
import io.techery.janet.compiler.utils.validation.ValidationError;
import io.techery.janet.compiler.utils.validation.Validator;

public class TypeIncomingValidator implements Validator<AsyncActionClass> {

    @Override public Set<ValidationError> validate(AsyncActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        if (value.isIncoming()) {
            for (ExecutableElement cons :
                    ElementFilter.constructorsIn(value.getTypeElement().getEnclosedElements())) {
                if (!cons.getParameters().isEmpty()) {
                    errors.add(new ValidationError("The class is missing a default constructor. For RECEIVE type you have to use default constructor", value
                            .getTypeElement()));
                }
            }

//            if (!value.getAnnotatedElements(SyncedResponse.class).isEmpty()) {
//                errors.add(new ValidationError("This class can not contain synced response. It is for receiving only", value
//                        .getTypeElement()));
//            }
        }
        return errors;
    }
}
