package io.techery.janet.http.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.techery.janet.HttpActionService;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * URL resolved against the base URL which set to {@linkplain HttpActionService}
 * <p>
 * Also it can be as a url path (@HttpAction.value()) if field value doesn't contain a scheme (http, https, ...).
 * <p>
 * Annotated field can be only as String, java.net.URI, android.net.Uri, okhttp3.HttpUrl or com.squareup.okhttp.HttpUrl
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Url {}
