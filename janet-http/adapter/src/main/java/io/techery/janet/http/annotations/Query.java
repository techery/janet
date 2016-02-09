package io.techery.janet.http.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface Query {
    /**
     * The query parameter name.
     */
    String value();

    /**
     * Specifies whether {@link #value()} is URL encoded.
     */
    boolean encodeName() default false;

    /**
     * Specifies whether the argument value to the annotated method parameter is URL encoded.
     */
    boolean encodeValue() default true;
}
