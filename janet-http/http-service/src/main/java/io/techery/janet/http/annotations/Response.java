package io.techery.janet.http.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Body for action response
 * <p>
 * By default body is parsed only for successful response (200..300).
 * Other conditions are specified via arguments.
 * <p>
 * For Example,
 * <ul>
 * <li>{@code @Response(Response.ERROR)} - for unsuccessful response {@code (status >= 300)}</li>
 * <li>{@code @Response(401)} - for response with specific status. In example, the annotated field will be filled only for
 * response with status code 401</li>
 * <li>{@code @Response(min = 200, max = 202)} - for response with specific status range. In example, the annotated field
 * will be filled only for response with status code from 200 to 202 inclusive</li>
 * <li>{@code @Response(min = 300)} - if one of point of range is not set this point set as infinity. In example,
 * the field will be filled for response witch status code equals or greater than 300</li>
 * </ul>
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Response {

    /**
     * HTTP status code of server response.
     * <p>
     * To handle only successful response use {@code SUCCESS} or {@code ERROR}
     * to handle error response
     */
    int value() default 0;

    int min() default 0; //from infinity

    int max() default 0; //to infinity

    /** for successful statuses (200..300)) */
    int SUCCESS = -1;
    /** for unsuccessful statuses */
    int ERROR = -2;

}
