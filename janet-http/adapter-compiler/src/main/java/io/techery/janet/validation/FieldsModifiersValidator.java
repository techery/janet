package io.techery.janet.validation;

import io.techery.janet.HttpActionClass;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

public class FieldsModifiersValidator implements Validator<HttpActionClass> {
    @Override
    public Set<ValidationError> validate(HttpActionClass value) {
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
