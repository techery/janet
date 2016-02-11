package io.techery.janet.validation;

import io.techery.janet.HttpActionClass;
import io.techery.janet.http.annotations.Body;
import io.techery.janet.http.annotations.Field;
import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.annotations.Part;
import io.techery.janet.http.annotations.ResponseHeader;
import io.techery.janet.http.annotations.Status;
import io.techery.janet.http.model.FileBody;
import io.techery.janet.http.model.FormUrlEncodedRequestBody;
import io.techery.janet.http.model.MultipartRequestBody;
import io.techery.janet.body.ActionBody;
import io.techery.janet.body.BytesArrayBody;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestActionValidators implements Validator<HttpActionClass> {

    private final List<Validator<HttpActionClass>> validators;

    public RestActionValidators() {
        validators = new ArrayList<Validator<HttpActionClass>>();
        //general rules
        validators.add(new FieldsModifiersValidator());
        validators.add(new PathValidator());
        validators.add(new BodyValidator());
        validators.add(new RequestTypeValidator(Body.class, HttpAction.Type.SIMPLE));
        validators.add(new RequestTypeValidator(Field.class, HttpAction.Type.FORM_URL_ENCODED));
        validators.add(new RequestTypeValidator(Part.class, HttpAction.Type.MULTIPART));
        //annotation rules
        validators.add(new AnnotationTypesValidator(ResponseHeader.class, String.class));
        validators.add(new AnnotationTypesValidator(Status.class, Boolean.class, Integer.class, Long.class, String.class, boolean.class, int.class, long.class));
        validators.add(new AnnotationTypesValidator(Part.class, File.class, byte[].class, String.class, ActionBody.class,
                BytesArrayBody.class, MultipartRequestBody.class, FormUrlEncodedRequestBody.class, FileBody.class));
        validators.add(new AnnotationQuantityValidator(Body.class, 1));
    }

    @Override
    public Set<ValidationError> validate(HttpActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        for (Validator<HttpActionClass> validator : validators) {
            errors.addAll(validator.validate(value));
        }
        return errors;
    }
}
