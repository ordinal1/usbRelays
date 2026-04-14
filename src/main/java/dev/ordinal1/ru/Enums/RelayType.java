package dev.ordinal1.ru.Enums;

public enum RelayType {
    LC_US(new byte[]{(byte) 0x2007, (byte) 0x5131}),
    LC_US2(new byte[]{(byte) 0x1A86 , (byte) 0x7523}),
    CUSTOM(new byte[2]);


    private final byte[] identifier;

    RelayType(byte[] identifier) {
        this.identifier = identifier;
    }

    public byte[] getIdentifier() {
        return identifier;
    }
}
