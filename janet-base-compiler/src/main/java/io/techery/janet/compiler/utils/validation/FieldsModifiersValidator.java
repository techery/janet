package io.techery.janet.compiler.utils.validation;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import io.techery.janet.compiler.utils.ActionClass;

public class FieldsModifiersValidator<T extends ActionClass> implements Validator<T> {
    @Override
    public Set<ValidationError> validate(T value) {
        Set<ValidationError> messages = new HashSet<ValidationError>();
        for (Element element : value.getAllAnnotatedMembers()) {
            if (element.getKind() != ElementKind.FIELD) continue;
            boolean hasPrivateModifier = element.getModifiers().contains(Modifier.PRIVATE);
            boolean hasStaticModifier = element.getModifiers().contains(Modifier.STATIC);
            if (hasStaticModifier || hasPrivateModifier) {
                messages.add(new ValidationError("Annotated fields must not be private or static.", element));
            }
        }
        return messages;
    }
}
