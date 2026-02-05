package dev.ordinal1.ru.Streams;

import dev.ordinal1.ru.DTO.RelayUsbPort;
import dev.ordinal1.ru.Exceptions.UsbCloseException;
import dev.ordinal1.ru.Exceptions.UsbRelayOpenException;
import dev.ordinal1.ru.Tools.RelayUsbTools;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class RelayUsbStream extends OutputStream {
    private static final int BUFFER_SIZE = 4096;

    private final DeviceHandle handle;
    private final PipedInputStream pipedInputStream;
    private final PipedOutputStream pipedOutputStream;
    private final RelayUsbPort relayUsbPort;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Thread usbThread;

    public RelayUsbStream(RelayUsbPort relayUsbPort) throws UsbRelayOpenException {
        this.relayUsbPort = relayUsbPort;

        // Opening USB device
        this.handle = openUsbDevice(relayUsbPort);

        // Initializing streams
        this.pipedOutputStream = new PipedOutputStream();
        this.pipedInputStream = new PipedInputStream(BUFFER_SIZE);

        try {
            this.pipedOutputStream.connect(pipedInputStream);
        } catch (IOException e) {
            throw new UsbRelayOpenException("Failed to connect streams", e);
        }

        // Starting worker thread
        this.usbThread = new Thread(createUsbTask(), "usbThread");
        this.usbThread.setDaemon(true);
        this.usbThread.start();
    }

    /**
     * Opens USB device by VID/PID
     */
    private DeviceHandle openUsbDevice(RelayUsbPort relayUsbPort) throws UsbRelayOpenException {
        short vid = relayUsbPort.getDevice().getUsbDeviceDescriptor().idVendor();
        short pid = relayUsbPort.getDevice().getUsbDeviceDescriptor().idProduct();

        DeviceHandle handle = LibUsb.openDeviceWithVidPid(null, vid, pid);
        if (handle == null) {
            throw new UsbRelayOpenException(
                    String.format("Device not found (VID: 0x%04X, PID: 0x%04X)", vid, pid)
            );
        }
        return handle;
    }

    public boolean isOpened() {
        return !closed.get();
    }

    /**
     * Creates task for processing data from buffer to USB
     */
    private Runnable createUsbTask() {
        return () -> {
            try {
                initializeUsb();
                processUsbData();
            } catch (Exception e) {
                handleUsbThreadError(e);
            } finally {
                cleanupUsb();
            }
        };
    }

    /**
     * Initializes USB library and claims interface
     */
    private void initializeUsb() throws LibUsbException {
        int result = LibUsb.init(null);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Failed to initialize libusb", result);
        }

        LibUsb.setAutoDetachKernelDriver(handle, true);

        result = LibUsb.claimInterface(handle, relayUsbPort.getInterfaceNumber());
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Failed to claim interface", result);
        }
    }

    /**
     * Main USB data processing loop
     */
    private void processUsbData() throws IOException {
        byte endpointOut = relayUsbPort.getEndpointAddressOut()
                .getUsbEndpointDescriptor()
                .bEndpointAddress();
        int packetSize = relayUsbPort.getEndpointAddressOut()
                .getUsbEndpointDescriptor()
                .wMaxPacketSize();

        byte[] buffer = new byte[packetSize];
        int bytesRead;

        while (!closed.get() && (bytesRead = pipedInputStream.read(buffer)) != -1) {
            writeToUsb(endpointOut, buffer, bytesRead);
        }
    }

    /**
     * Sends data to USB device
     */
    private void writeToUsb(byte endpointOut, byte[] buffer, int bytesRead) {
        int offset = 0;
        while (offset < bytesRead) {
            byte[] chunk = Arrays.copyOfRange(buffer, offset, bytesRead);
            int sent = RelayUsbTools.write(handle, endpointOut, chunk);
            offset += sent;
        }
    }

    /**
     * Handles errors in worker thread
     */
    private void handleUsbThreadError(Exception e) {
        System.err.println("Error in USB stream: " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Cleans up USB resources
     */
    private void cleanupUsb() {
        try {
            LibUsb.releaseInterface(handle, relayUsbPort.getInterfaceNumber());
        } catch (Exception e) {
            System.err.println("Error while releasing interface: " + e.getMessage());
        }

        try {
            LibUsb.close(handle);
            LibUsb.exit(null);
        } catch (Exception e) {
            System.err.println("Error while closing USB: " + e.getMessage());
        }
    }

    @Override
    public void write(int b) throws IOException {
        ensureOpen();
        pipedOutputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        ensureOpen();
        pipedOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        pipedOutputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        ensureOpen();
        pipedOutputStream.flush();
    }

    @Override
    public void close() throws UsbCloseException {
        if (!closed.compareAndSet(false, true)) {
            return; // Already closed
        }

        closeStreams();
        waitForUsbThread();
    }

    /**
     * Closes I/O streams
     */
    private void closeStreams() throws UsbCloseException {
        try {
            if (pipedOutputStream != null) {
                pipedOutputStream.close();
            }
            if (pipedInputStream != null) {
                pipedInputStream.close();
            }
        } catch (IOException e) {
            throw new UsbCloseException("Error closing streams", e);
        }
    }

    /**
     * Waits for worker thread to finish
     */
    private void waitForUsbThread() {
        try {
            usbThread.join(5000); // Wait 5 seconds
            if (usbThread.isAlive()) {
                System.err.println("USB thread did not finish in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks whether the stream is open
     */
    private void ensureOpen() throws IOException {
        if (closed.get()) {
            throw new IOException("Stream is already closed!");
        }
    }
}