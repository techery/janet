package io.techery.janet.converter;

public class ConverterException extends RuntimeException {

    private ConverterException(String message) {
        super(message);
    }

    private ConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    private ConverterException(Throwable cause) {
        super(cause);
    }

    public static ConverterException forSerialization(Throwable cause) {
        return new ConverterException("Can't serialize", cause);
    }

    public static ConverterException forDeserialization(Throwable cause) {
        return new ConverterException("Can't deserialize", cause);
    }

}
