package io.techery.janet;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.compiler.utils.ActionClass;

public class AsyncActionClass extends ActionClass {

    private final String event;
    private final AsyncAction.Type type;

    public AsyncActionClass(Elements elementUtils, TypeElement typeElement) {
        super(AsyncAction.class, elementUtils, typeElement);
        AsyncAction annotation = typeElement.getAnnotation(AsyncAction.class);
        type = annotation.type();
        event = annotation.value();
    }

    public String getEvent() {
        return event;
    }

    public AsyncAction.Type getType() {
        return type;
    }
}
