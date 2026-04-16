package dev.ordinal1.ru;

import dev.ordinal1.ru.DTO.RelayDevice;
import dev.ordinal1.ru.DTO.RelayUsbPort;
import dev.ordinal1.ru.Enums.RelayOperation;
import dev.ordinal1.ru.Enums.RelayType;
import dev.ordinal1.ru.Exceptions.UsbRelayNotFound;
import dev.ordinal1.ru.Interfaces.RelayInterface;
import dev.ordinal1.ru.Streams.RelayUsbStream;
import dev.ordinal1.ru.Tools.RelayUsbTools;

import javax.usb.UsbDevice;
import java.io.IOException;

public class UsbRelay implements RelayInterface {
    private final RelayDevice relayDevice;

    private RelayUsbStream stream;

    public UsbRelay(RelayType type) throws IOException {
        relayDevice = type.device();

        if (notReconnected())
            throw new UsbRelayNotFound("Device not found");
    }

    public UsbRelay(RelayDevice relayDevice) throws IOException {
        this.relayDevice = relayDevice;

        if (notReconnected())
            throw new UsbRelayNotFound("Device not found");
    }

    public static RelayUsbPort find(RelayDevice relayDevice) {
        UsbDevice targetDevice = RelayUsbTools.findDeviceRecursively(RelayUsbTools.getRootHub(), relayDevice);
        if (targetDevice == null) return null;
        return RelayUsbTools.configureUsbPort(targetDevice);
    }

    @Override
    public boolean isConnected(RelayDevice relayDevice) {
        return find(relayDevice) != null;
    }

    @Override
    public boolean isConnected(RelayType type) {
        return find(type.device()) != null;
    }

    public RelayUsbPort find() {
        return find(relayDevice);
    }

    /**
     * Attempts to connect/reconnect to a device
     * @return connection result
     */
    public boolean notReconnected() throws IOException {
        RelayUsbPort port = find();
        if (port == null) throw new UsbRelayNotFound("Device not found!");

        stream = new RelayUsbStream(port);
        return false;
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
