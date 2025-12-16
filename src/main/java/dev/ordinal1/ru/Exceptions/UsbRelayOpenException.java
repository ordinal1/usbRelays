package dev.ordinal1.ru.Exceptions;

import java.io.IOException;

public class UsbRelayOpenException extends IOException {
    public UsbRelayOpenException(String message) {
        super(message);
    }

    public UsbRelayOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
