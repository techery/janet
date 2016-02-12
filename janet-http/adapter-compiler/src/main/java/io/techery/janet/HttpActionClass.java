package io.techery.janet;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import io.techery.janet.compiler.utils.ActionClass;
import io.techery.janet.http.annotations.HttpAction;

public class HttpActionClass extends ActionClass {
    private final HttpAction.Method method;
    private final String path;
    private final HttpAction.Type requestType;

    public HttpActionClass(Elements elementUtils, TypeElement typeElement) {
        super(HttpAction.class, elementUtils, typeElement);
        HttpAction annotation = typeElement.getAnnotation(HttpAction.class);
        method = annotation.method();
        path = annotation.value();
        requestType = annotation.type();
    }

    public HttpAction.Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public HttpAction.Type getRequestType() {
        return requestType;
    }

    public String getHelperName() {
        return getTypeElement().getSimpleName() + HttpHelpersGenerator.HELPER_SUFFIX;
    }

    public String getFullHelperName() {
        return getPackageName() + "." + getTypeElement().getSimpleName() + HttpHelpersGenerator.HELPER_SUFFIX;
    }

}