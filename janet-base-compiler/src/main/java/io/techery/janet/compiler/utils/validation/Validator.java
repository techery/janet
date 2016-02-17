package io.techery.janet.compiler.utils.validation;

import java.util.Set;

public interface Validator<T> {
    Set<ValidationError> validate(T value);
}
