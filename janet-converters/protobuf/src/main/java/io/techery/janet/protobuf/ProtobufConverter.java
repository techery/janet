package io.techery.janet.protobuf;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.MessageLite;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import io.techery.janet.body.ActionBody;
import io.techery.janet.body.BytesArrayBody;
import io.techery.janet.converter.Converter;
import io.techery.janet.converter.ConverterException;

public class ProtobufConverter implements Converter {
    private static final String MIME_TYPE = "application/x-protobuf";

    @Override public Object fromBody(ActionBody body, Type type) throws ConverterException {
        if (!(type instanceof Class<?>)) {
            throw ConverterException.forDeserialization(new IllegalArgumentException("Expected a raw Class<?> but was " + type));
        }
        Class<?> c = (Class<?>) type;
        if (!AbstractMessageLite.class.isAssignableFrom(c)) {
            throw ConverterException.forDeserialization(new IllegalArgumentException("Expected a protobuf message but was " + c.getName()));
        }

        String mimeType = body.mimeType();
        if (!MIME_TYPE.equals(mimeType)) {
            throw ConverterException.forDeserialization(new RuntimeException("Response content type was not a proto: " + mimeType));
        }

        try {
            Method parseFrom = c.getMethod("parseFrom", InputStream.class);
            return parseFrom.invoke(null, body.in());
        } catch (InvocationTargetException e) {
            throw ConverterException.forDeserialization(new RuntimeException(c.getName() + ".parseFrom() failed", e.getCause()));
        } catch (NoSuchMethodException e) {
            throw ConverterException.forDeserialization(new IllegalArgumentException("Expected a protobuf message but was " + c.getName()));
        } catch (IllegalAccessException e) {
            throw ConverterException.forDeserialization(new AssertionError());
        } catch (IOException e) {
            throw ConverterException.forDeserialization(new RuntimeException(e));
        }
    }

    @Override public ActionBody toBody(Object object) throws ConverterException {
        if (!(object instanceof AbstractMessageLite)) {
            throw ConverterException.forSerialization(new IllegalArgumentException("Request has to inherit MessageLite"));
        }

        return new BytesArrayBody(MIME_TYPE, ((MessageLite) object).toByteArray());
    }
}