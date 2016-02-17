package io.techery.janet.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

import io.techery.janet.HttpActionClass;
import io.techery.janet.compiler.utils.validation.ValidationError;
import io.techery.janet.compiler.utils.validation.Validator;
import io.techery.janet.http.annotations.HttpAction;

public class RequestTypeValidator implements Validator<HttpActionClass> {

    private final Class annotationClass;
    private final HttpAction.Type[] requestTypes;

    public RequestTypeValidator(Class annotationClass, HttpAction.Type... requestTypes) {
        this.annotationClass = annotationClass;
        this.requestTypes = requestTypes;
    }

    @Override
    public Set<ValidationError> validate(HttpActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        String bodyName = annotationClass.getSimpleName();
        List<HttpAction.Type> typesList = Arrays.asList(requestTypes);
        for (Element element : value.getTypeElement().getEnclosedElements()) {
            if (element.getAnnotation(annotationClass) == null) continue;
            if (!typesList.contains(value.getRequestType())) {
                errors.add(new ValidationError("It's possible to use %s only with %s request types ", element, bodyName, typesList
                        .toString()));
            }
        }
        return errors;
    }
}
