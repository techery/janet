package io.techery.janet.validation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;

import io.techery.janet.AsyncActionClass;
import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.compiler.utils.validation.ValidationError;
import io.techery.janet.compiler.utils.validation.Validator;

public class SyncedResponseValidator implements Validator<AsyncActionClass> {

    @Override public Set<ValidationError> validate(AsyncActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        if (value.getResponseInfo() != null) {
            AsyncAction asyncActionAnnotation = value.getResponseInfo().responseFieldType.getAnnotation(AsyncAction.class);
            if(asyncActionAnnotation == null || !asyncActionAnnotation.incoming()){
                errors.add(new ValidationError("Synced response must be as incoming async action", value.getResponseInfo().responseField));
            }
            if (value.getResponseInfo().syncPredicateElement == null) {
                errors.add(new ValidationError("No sync predicate", value.getResponseInfo().responseField));
            }
            for (ExecutableElement cons :
                    ElementFilter.constructorsIn(value.getResponseInfo().syncPredicateElement.getEnclosedElements())) {
                if (!cons.getParameters().isEmpty()) {
                    errors.add(new ValidationError("The class is missing a default constructor", value.getResponseInfo().syncPredicateElement));
                }
            }
        }
        return errors;
    }
}
