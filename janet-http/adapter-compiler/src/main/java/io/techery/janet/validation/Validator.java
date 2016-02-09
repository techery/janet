package io.techery.janet.validation;

import java.util.Set;

public interface Validator<T> {
    Set<ValidationError> validate(T value);
}
