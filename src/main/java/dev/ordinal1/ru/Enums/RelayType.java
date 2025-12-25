package dev.ordinal1.ru.Enums;

public enum RelayType {
    LC_US(new byte[]{(byte) 0x2007, (byte) 0x5131}),
    CUSTOM(new byte[2]);


    private final byte[] identifier;

    RelayType(byte[] identifier) {
        this.identifier = identifier;
    }

    public byte[] getIdentifier() {
        return identifier;
    }
}
