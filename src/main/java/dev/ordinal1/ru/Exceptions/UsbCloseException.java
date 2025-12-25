package dev.ordinal1.ru.Exceptions;

import java.io.IOException;

public class UsbCloseException extends IOException {
    public UsbCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
