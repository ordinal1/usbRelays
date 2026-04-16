package dev.ordinal1.ru.Enums;

import dev.ordinal1.ru.DTO.RelayDevice;

public enum RelayType {
    LC_US(new RelayDevice((short) 0x5131, (short) 0x2007)),
    LC_US2(new RelayDevice((short) 0x7523, (short) 0x1A86));


    private final RelayDevice device;

    RelayType(RelayDevice device) {
        this.device = device;
    }

    public RelayDevice device() {
        return device;
    }
}
