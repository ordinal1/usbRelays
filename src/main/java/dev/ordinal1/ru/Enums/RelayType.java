package dev.ordinal1.ru.Enums;

import lombok.Getter;

public enum RelayType {
    LC_US1 (new byte[]{(byte) 0x2007, (byte) 0x5131}),
    CUSTOM(new byte[2]);


    @Getter
    private final byte[] identifier;

    RelayType(byte[] identifier) {
        this.identifier = identifier;
    }
}
