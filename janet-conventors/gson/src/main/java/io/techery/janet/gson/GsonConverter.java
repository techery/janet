package io.techery.janet.gson;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import io.techery.janet.converter.Converter;
import io.techery.janet.model.ActionBody;
import io.techery.janet.model.BytesArrayBody;

public class GsonConverter implements Converter {
    private final Gson gson;
    private String charset;

    public GsonConverter(Gson gson) {
        this(gson, "UTF-8");
    }

    public GsonConverter(Gson gson, String charset) {
        this.gson = gson;
        this.charset = charset;
    }

    @Override
    public Object fromBody(ActionBody body, Type type) {
        String charset = this.charset;
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(body.in(), charset);
            return gson.fromJson(isr, type);
        } catch (JsonParseException e) {
            System.err.println("Parse error of " + type + ": " + e.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public ActionBody toBody(Object object) {
        try {
            return new BytesArrayBody("application/json; charset=" + charset, gson.toJson(object).getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
