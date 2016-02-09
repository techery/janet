package io.techery.janet.validation;

import io.techery.janet.HttpActionClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

public class AnnotationQuantityValidator implements Validator<HttpActionClass> {

    private final Class annotationClass;
    private final int maxQuantity;

    public AnnotationQuantityValidator(Class annotationClass, int maxQuantity) {
        this.annotationClass = annotationClass;
        this.maxQuantity = maxQuantity;
    }

    @Override
    public Set<ValidationError> validate(HttpActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        List<Element> annotations = value.getAnnotatedElements(annotationClass);
        if (annotations.size() > maxQuantity) {
            errors.add(new ValidationError("There are more then one field annotated with %s", annotations.get(maxQuantity), annotationClass.getName()));
        }
        return errors;
    }
}
