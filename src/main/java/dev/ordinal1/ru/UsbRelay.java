package dev.ordinal1.ru;

import dev.ordinal1.ru.DTO.UsbPort;
import dev.ordinal1.ru.Enums.Operations;
import dev.ordinal1.ru.Enums.RelayType;
import dev.ordinal1.ru.Streams.UsbStream;
import dev.ordinal1.ru.Tools.UsbTools;
import lombok.Setter;

import javax.usb.UsbDevice;
import java.io.IOException;

@Setter
public class UsbRelay implements AutoCloseable{
    private final short pid;
    private final short vid;

    private UsbStream stream;

    public UsbRelay(RelayType type) throws IOException {
        pid = type.getIdentifier()[0];
        vid = type.getIdentifier()[1];

        reconnect();
    }

    public UsbRelay(short pid, short vid) throws IOException {
        this.pid = pid;
        this.vid = vid;

        reconnect();
    }

    public static boolean isConnected(short pid, short vid) {
        return find(pid, vid) != null;
    }

    public static boolean isConnected(RelayType type) {
        short pid = type.getIdentifier()[0];
        short vid = type.getIdentifier()[1];
        return find(pid, vid) != null;
    }

    public static UsbPort find(short pid, short vid) {
        UsbDevice targetDevice = UsbTools.findDeviceRecursively(UsbTools.getRootHub(), vid, pid);
        if (targetDevice == null) return null;
        return UsbTools.configureUsbPort(targetDevice);
    }

    public UsbPort find() {
        return find(pid, vid);
    }

    /**
     * Attempts to connect/reconnect to a device
     * @return connection result
     */
    public boolean reconnect() throws IOException {
        UsbPort port = find();
        if (port == null) throw new IOException("Device not found!");

        stream = new UsbStream(port);
        return true;
    }

    /**
     * Performs a specified operation
     * @param operation close or Open Operation
     */
    public void sendCommand(Operations operation) throws IOException, InterruptedException {
        if (!stream.isClosed()) {
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
    public void sendCommand(Operations operation, long ms) throws IOException, InterruptedException {
        if (operation.getBack() == null) return;

        if (!stream.isClosed()) {
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
        if (stream != null && !stream.isClosed()) {
            stream.close();
        }
    }
}
