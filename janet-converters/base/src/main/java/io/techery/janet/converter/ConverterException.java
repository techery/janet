package io.techery.janet.converter;

public class ConverterException extends RuntimeException {

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConverterException(Throwable cause) {
        super(cause);
    }

    public static ConverterException forSerialization(Throwable cause) {
        return new ConverterException("Can't serialize", cause);
    }

    public static ConverterException forDeserialization(Throwable cause) {
        return new ConverterException("Can't deserialize", cause);
    }

}
