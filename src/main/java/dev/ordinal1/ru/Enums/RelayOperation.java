package dev.ordinal1.ru.Enums;

public enum RelayOperation {
    CLOSE_1(new byte[]{(byte) 0xA0, 0x01, 0x00, (byte) 0xA1}, null),
    OPEN_1(new byte[]{(byte) 0xA0, 0x01, 0x01, (byte) 0xA2}, CLOSE_1),

    CLOSE_2(new byte[]{(byte) 0xA0, 0x02, 0x00, (byte) 0xA2}, null),
    OPEN_2(new byte[]{(byte) 0xA0, 0x02, 0x01, (byte) 0xA3}, CLOSE_2),

    CLOSE_3(new byte[]{(byte) 0xA0, 0x03, 0x00, (byte) 0xA3}, null),
    OPEN_3(new byte[]{(byte) 0xA0, 0x03, 0x01, (byte) 0xA4}, CLOSE_3),

    CLOSE_4(new byte[]{(byte) 0xA0, 0x04, 0x00, (byte) 0xA4}, null),
    OPEN_4(new byte[]{(byte) 0xA0, 0x04, 0x01, (byte) 0xA5}, CLOSE_4);

    private final byte[] command;
    private final RelayOperation back;

    RelayOperation(byte[] command, RelayOperation back) {
        this.command = command;
        this.back = back;
    }

    public byte[] getCommand() {
        return command;
    }

    public RelayOperation getBack() {
        return back;
    }
}
