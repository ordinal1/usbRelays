package dev.ordinal1.ru;

import dev.ordinal1.ru.DTO.RelayUsbPort;
import dev.ordinal1.ru.Enums.RelayOperation;
import dev.ordinal1.ru.Enums.RelayType;
import dev.ordinal1.ru.Exceptions.UsbRelayNotFound;
import dev.ordinal1.ru.Streams.RelayUsbStream;
import dev.ordinal1.ru.Tools.RelayUsbTools;

import javax.usb.UsbDevice;
import java.io.IOException;

public class UsbRelay implements AutoCloseable {
    private final short pid;
    private final short vid;

    private RelayUsbStream stream;

    public UsbRelay(RelayType type) throws IOException {
        pid = type.getIdentifier()[0];
        vid = type.getIdentifier()[1];

        if (!reconnect())
            throw new UsbRelayNotFound("Device not found");
    }

    public UsbRelay(short pid, short vid) throws IOException {
        this.pid = pid;
        this.vid = vid;

        if (!reconnect())
            throw new UsbRelayNotFound("Device not found");
    }

    public static boolean isConnected(short pid, short vid) {
        return find(pid, vid) != null;
    }

    public static boolean isConnected(RelayType type) {
        short pid = type.getIdentifier()[0];
        short vid = type.getIdentifier()[1];
        return find(pid, vid) != null;
    }

    public static RelayUsbPort find(short pid, short vid) {
        UsbDevice targetDevice = RelayUsbTools.findDeviceRecursively(RelayUsbTools.getRootHub(), vid, pid);
        if (targetDevice == null) return null;
        return RelayUsbTools.configureUsbPort(targetDevice);
    }

    public RelayUsbPort find() {
        return find(pid, vid);
    }

    /**
     * Attempts to connect/reconnect to a device
     * @return connection result
     */
    public boolean reconnect() throws IOException {
        RelayUsbPort port = find();
        if (port == null) throw new UsbRelayNotFound("Device not found!");

        stream = new RelayUsbStream(port);
        return true;
    }

    /**
     * Performs a specified operation
     * @param operation close or Open Operation
     */
    public void sendCommand(RelayOperation operation) throws IOException, InterruptedException {
        if (stream.isOpened()) {
            stream.write(operation.getCommand());
            Thread.sleep(1200);
            return;
        }

        throw new IOException("Stream is closed!");
    }

    /**
     * Opens the relay for a given period of time in ms
     * @param operation operation with back (open)
     * @param ms latency in milliseconds
     */
    public void sendCommand(RelayOperation operation, long ms) throws IOException, InterruptedException {
        if (operation.getBack() == null) return;

        if (stream.isOpened()) {
            stream.write(operation.getCommand());
            Thread.sleep(ms);
            stream.write(operation.getBack().getCommand());
            Thread.sleep(1200);
            return;
        }

        throw new IOException("Stream is closed!");
    }

    @Override
    public void close() throws Exception {
        if (stream != null && stream.isOpened()) {
            stream.close();
        }
    }
}
