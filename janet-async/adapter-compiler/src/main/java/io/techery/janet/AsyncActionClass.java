package io.techery.janet;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import io.techery.janet.async.annotations.AsyncAction;
import io.techery.janet.async.annotations.AsyncMessage;
import io.techery.janet.async.annotations.SyncedResponse;
import io.techery.janet.body.BytesArrayBody;
import io.techery.janet.compiler.utils.ActionClass;

public class AsyncActionClass extends ActionClass {

    public final static String WRAPPER_SUFFIX = "Wrapper";

    private String event;
    private final boolean incoming;
    private SyncedResponseInfo responseInfo;
    private Element messageField;
    private boolean isBytesMessage;


    public AsyncActionClass(Elements elementUtils, TypeElement typeElement) {
        super(AsyncAction.class, elementUtils, typeElement);
        AsyncAction annotation = typeElement.getAnnotation(AsyncAction.class);
        this.incoming = annotation.incoming();
        this.event = annotation.value();
        List<Element> messageFields = getAnnotatedElements(AsyncMessage.class);
        for (Element field : messageFields) {
            this.messageField = field;
            break;
        }

        if (messageField == null) { //validator throw a error
            return;
        }

        //defining message is bytes
        ClassName bytesArrayBody = ClassName.get(BytesArrayBody.class);
        TypeMirror messageSuperClass = elementUtils.getTypeElement(messageField.asType().toString()).getSuperclass();
        while (messageSuperClass != null && messageSuperClass.getKind() != TypeKind.NONE) {
            TypeName typeName = ClassName.get(messageSuperClass);
            if (typeName.toString().equals(bytesArrayBody.toString())) {
                isBytesMessage = true;
                break;
            }
            messageSuperClass = elementUtils.getTypeElement(messageSuperClass.toString()).getSuperclass();
        }
        //getting response info
        List<Element> fields = getAnnotatedElements(SyncedResponse.class);
        if (!fields.isEmpty()) {
            responseInfo = new SyncedResponseInfo(elementUtils, fields.get(0));
        }
    }

    public String getEvent() {
        return event;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public String getWrapperName() {
        return getTypeElement().getSimpleName() + WRAPPER_SUFFIX;
    }

    public String getFullWrapperName() {
        return getPackageName() + "." + getTypeElement().getSimpleName() + WRAPPER_SUFFIX;
    }

    public boolean isBytesMessage() {
        return isBytesMessage;
    }

    public SyncedResponseInfo getResponseInfo() {
        return responseInfo;
    }

    public Element getMessageField() {
        return messageField;
    }

    final public static class SyncedResponseInfo {
        public final String responseEvent;
        public TypeElement syncPredicateElement;
        public final Element responseField;
        public final TypeElement responseFieldType;

        public SyncedResponseInfo(Elements elementUtils, Element responseField) {
            this.responseField = responseField;
            for (AnnotationMirror annotation : responseField.getAnnotationMirrors()) {
                if (ClassName.get(SyncedResponse.class).equals(ClassName.get(annotation.getAnnotationType()))) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> valuesMap = annotation.getElementValues();
                    for (ExecutableElement key : valuesMap.keySet()) {
                        if (key.getSimpleName().contentEquals("value")) {
                            TypeMirror valueMirror = (TypeMirror) valuesMap.get(key).getValue();
                            this.syncPredicateElement = elementUtils.getTypeElement(ClassName.get(valueMirror)
                                    .toString());
                        }
                    }
                    break;
                }
            }
            TypeName responseActionName = ClassName.get(responseField.asType());
            responseFieldType = elementUtils.getTypeElement(responseActionName.toString());
            AsyncAction asyncAction = responseFieldType.getAnnotation(AsyncAction.class);
            this.responseEvent = asyncAction.value();
        }
    }
}
