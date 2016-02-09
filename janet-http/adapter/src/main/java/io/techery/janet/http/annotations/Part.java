package io.techery.janet.http.annotations;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.techery.janet.http.model.MultipartRequestBody;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface Part {
    String value();

    String encoding() default MultipartRequestBody.DEFAULT_TRANSFER_ENCODING;
}
