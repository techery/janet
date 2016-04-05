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
    private Type[] types = new Type[0];
    private TypeName[] typeNames = new TypeName[0];
    private final ArrayList<String> typeStrings;

    public AnnotationTypesValidator(Class annotationClass, Type... types) {
        this(annotationClass, types, new TypeName[]{});
    }

    public AnnotationTypesValidator(Class annotationClass, TypeName... typeNames) {
        this(annotationClass, new Type[]{}, typeNames);
    }

    public AnnotationTypesValidator(Class annotationClass, Type[] types, TypeName[] typeNames) {
        this.annotationClass = annotationClass;
        this.types = types;
        this.typeNames = typeNames;
        typeStrings = new ArrayList<String>();
        for (Type type : types) {
            typeStrings.add(TypeName.get(type).toString());
        }
        for (TypeName type : typeNames) {
            typeStrings.add(type.toString());
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
            if (TypeUtils.containsType(element, typeNames)) {
                continue;
            }
            errors.add(new ValidationError("Fields annotated with %s should one from these types %s", element, annotation
                    .toString(), typeStrings.toString()));
        }
        return errors;
    }
}
