package io.techery.janet.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

import io.techery.janet.HttpActionClass;
import io.techery.janet.compiler.utils.validation.ValidationError;
import io.techery.janet.compiler.utils.validation.Validator;
import io.techery.janet.http.annotations.Response;

public class ResponseValidator implements Validator<HttpActionClass> {
    @Override
    public Set<ValidationError> validate(HttpActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        ValidationError error = validateInternal(value);
        if (error != null) {
            errors.add(error);
        }
        return errors;
    }

    private static ValidationError validateInternal(HttpActionClass value) {
        List<Element> annotations = value.getAnnotatedElements(Response.class);
        if (annotations.isEmpty()) return null;
        for (Element element : annotations) {
            Response annotation = element.getAnnotation(Response.class);
            if (annotation.value() < Response.ERROR) {
                return lessThanZero("@Response.value()", element);
            }
            if (annotation.max() < 0) {
                return lessThanZero("@Response.max()", element);
            }
            if (annotation.min() < 0) {
                return lessThanZero("@Response.min()", element);
            }
            if (annotation.value() != 0
                    && (annotation.min() > 0 || annotation.max() > 0)) {
                return new ValidationError("There is no possibility to specify status code with using arguments min() and max()", element);
            }
        }
        return null;
    }

    private static ValidationError lessThanZero(String label, Element element) {
        return new ValidationError("%s can not be less than 0", element, label);
    }
}
