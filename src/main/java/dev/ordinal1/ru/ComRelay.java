package dev.ordinal1.ru;

import com.fazecast.jSerialComm.SerialPort;
import dev.ordinal1.ru.DTO.RelayUsbPort;
import dev.ordinal1.ru.Enums.RelayOperation;
import dev.ordinal1.ru.Enums.RelayType;
import dev.ordinal1.ru.Exceptions.ComRelayNotOpen;
import dev.ordinal1.ru.Interfaces.RelayInterface;
import dev.ordinal1.ru.Tools.RelayUsbTools;

import javax.usb.UsbDevice;
import java.io.IOException;

public class ComRelay implements RelayInterface {
    private final SerialPort serialPort;

    public ComRelay(String portName) {
        this.serialPort = SerialPort.getCommPort(portName);

        if (!serialPort.openPort())
            throw new ComRelayNotOpen("The device cannot be opened");
    }


    public boolean isConnected(RelayType type) {
        return find(type.getIdentifier()[0], type.getIdentifier()[1]) != null;
    }

    @Override
    public boolean isConnected(short pid, short vid) {
        return find(pid, vid) != null;
    }

    /**
     * Performs a specified operation
     * @param operation close or Open Operation
     */
    public void sendCommand(RelayOperation operation) throws IOException, InterruptedException {
        if (serialPort.isOpen()) {
            serialPort.writeBytes(operation.getCommand(), operation.getCommand().length);
            Thread.sleep(1200);
            return;
        }

        throw new IOException("Serial port is closed!");
    }

    /**
     * Opens the relay for a given period of time in ms
     * @param operation operation with back (open)
     * @param ms latency in milliseconds
     */
    public void sendCommand(RelayOperation operation, long ms) throws IOException, InterruptedException {
        if (operation.getBack() == null) return;

        if (serialPort.isOpen()) {
            serialPort.writeBytes(operation.getCommand(), operation.getCommand().length);
            Thread.sleep(ms);
            serialPort.writeBytes(operation.getBack().getCommand(), operation.getBack().getCommand().length);
            Thread.sleep(1200);
            return;
        }

        throw new IOException("Serial port is closed!");
    }

    public static RelayUsbPort find(short pid, short vid) {
        UsbDevice targetDevice = RelayUsbTools.findDeviceRecursively(RelayUsbTools.getRootHub(), vid, pid);
        if (targetDevice == null) return null;
        return RelayUsbTools.configureUsbPort(targetDevice);
    }

    @Override
    public void close() throws Exception {
        if (serialPort.isOpen())
            serialPort.closePort();
    }
}
