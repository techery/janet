package io.techery.janet.http.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface HttpAction {

    Method method() default Method.GET;

    String value() default "";

    Type type() default Type.SIMPLE;

    String[] headers() default {};

    enum Type {
        /**
         * No content-specific logic required.
         */
        SIMPLE,
        /**
         * Multi-part request body.
         */
        MULTIPART,
        /**
         * Form URL-encoded request body.
         */
        FORM_URL_ENCODED
    }

    enum Method {
        GET(false), POST(true), PUT(true), DELETE(true), HEAD(false), PATCH(true);

        private boolean hasBody;

        Method(boolean hasBody) {
            this.hasBody = hasBody;
        }

        public boolean hasBody() {
            return hasBody;
        }
    }
}
