package dev.ordinal1.ru;

import com.fazecast.jSerialComm.SerialPort;
import dev.ordinal1.ru.Enums.RelayOperation;
import dev.ordinal1.ru.Enums.RelayType;
import dev.ordinal1.ru.Exceptions.ComRelayNotOpen;

import java.io.IOException;

public class ComRelay implements AutoCloseable{
    private final SerialPort serialPort;

    public ComRelay(String portName) {
        this.serialPort = SerialPort.getCommPort(portName);

        if (!serialPort.openPort())
            throw new ComRelayNotOpen("The device cannot be opened");
    }

    public boolean isConnected(RelayType type) {
        return UsbRelay.isConnected(type);
    }

    public static boolean isConnected(short pid, short vid) {
        return UsbRelay.isConnected(pid, vid);
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

    @Override
    public void close() throws Exception {
        if (serialPort.isOpen())
            serialPort.closePort();
    }
}
