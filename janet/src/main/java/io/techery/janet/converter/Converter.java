package io.techery.janet.converter;

import java.lang.reflect.Type;

import io.techery.janet.model.ActionBody;

public interface Converter {

    Object fromBody(ActionBody body, Type type);

    ActionBody toBody(Object object);
}
