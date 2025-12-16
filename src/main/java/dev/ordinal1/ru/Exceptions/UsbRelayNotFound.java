package dev.ordinal1.ru.Exceptions;

import java.io.IOException;

public class UsbRelayNotFound extends IOException {
    public UsbRelayNotFound(String message) {
        super(message);
    }
}
