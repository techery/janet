package io.techery.janet.http.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Response status code.
 * Possible types of field:
 * Boolean.class, Integer.class, Long.class, String.class, boolean.class, int.class, long.class
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Status {
}
