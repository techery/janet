package io.techery.janet.compiler.utils.validation;

import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

import io.techery.janet.compiler.utils.ActionClass;
import io.techery.janet.compiler.utils.TypeUtils;

public class AnnotationTypesValidator<T extends ActionClass> implements Validator<T> {

    private final Class annotationClass;
    private final Type[] types;
    private final ArrayList<String> typeNames;

    public AnnotationTypesValidator(Class annotationClass, Type... types) {
        this.annotationClass = annotationClass;
        this.types = types;
        typeNames = new ArrayList<String>();
        for (Type type : types) {
            typeNames.add(TypeName.get(type).toString());
        }
    }

    @Override
    public Set<ValidationError> validate(T value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        List<Element> elements = value.getAnnotatedElements(annotationClass);
        for (Element element : elements) {
            Annotation annotation = element.getAnnotation(annotationClass);
            if (TypeUtils.containsType(element, types)) {
                continue;
            }
            errors.add(new ValidationError("Fields annotated with %s should one from these types %s", element, annotation
                    .toString(), typeNames.toString()));
        }
        return errors;
    }
}
