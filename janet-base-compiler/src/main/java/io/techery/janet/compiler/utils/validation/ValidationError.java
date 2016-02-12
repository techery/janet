package io.techery.janet.compiler.utils.validation;

import javax.lang.model.element.Element;

public class ValidationError {

    private final String message;

    private final Element element;

    public ValidationError(String message, Element element, String... args) {
        this(String.format(message, (Object[])args), element);
    }

    public ValidationError(String message, Element element) {
        this.message = message;
        this.element = element;
    }

    public String getMessage() {
        return message;
    }

    public Element getElement() {
        return element;
    }
}
