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

public class ProtobufConverter implements Converter {
    private static final String MIME_TYPE = "application/x-protobuf";

    @Override public Object fromBody(ActionBody body, Type type) {
        if (!(type instanceof Class<?>)) {
            throw new IllegalArgumentException("Expected a raw Class<?> but was " + type);
        }
        Class<?> c = (Class<?>) type;
        if (!AbstractMessageLite.class.isAssignableFrom(c)) {
            throw new IllegalArgumentException("Expected a protobuf message but was " + c.getName());
        }

        String mimeType = body.mimeType();
        if (!MIME_TYPE.equals(mimeType)) {
            throw new RuntimeException("Response content type was not a proto: " + mimeType);
        }

        try {
            Method parseFrom = c.getMethod("parseFrom", InputStream.class);
            return parseFrom.invoke(null, body.in());
        } catch (InvocationTargetException e) {
            throw new RuntimeException(c.getName() + ".parseFrom() failed", e.getCause());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Expected a protobuf message but was " + c.getName());
        } catch (IllegalAccessException e) {
            throw new AssertionError();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public ActionBody toBody(Object object) {
        if (!(object instanceof AbstractMessageLite)) {
            throw new IllegalArgumentException("Request has to inherit MessageLite");
        }

        return new BytesArrayBody(MIME_TYPE, ((MessageLite) object).toByteArray());
    }
}