package dev.ordinal1.ru.Streams;

import dev.ordinal1.ru.Exceptions.UsbRelayOpenException;
import dev.ordinal1.ru.Tools.UsbTools;
import dev.ordinal1.ru.DTO.UsbPort;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

@Slf4j
public class UsbStream extends OutputStream {
    private final DeviceHandle handle;

    private final PipedInputStream pipedInputStream;
    private final PipedOutputStream pipedOutputStream;

    private final UsbPort usbPort;

    @Getter
    private volatile boolean closed = false;

    public UsbStream(UsbPort usbPort) throws UsbRelayOpenException {
        short vid = usbPort.getDevice().getUsbDeviceDescriptor().idVendor();
        short pid = usbPort.getDevice().getUsbDeviceDescriptor().idProduct();
        handle = LibUsb.openDeviceWithVidPid(null, vid, pid);

        this.usbPort = usbPort;
        this.pipedOutputStream = new PipedOutputStream();
        this.pipedInputStream = new PipedInputStream();

        try {
            this.pipedOutputStream.connect(pipedInputStream);
        } catch (IOException e) {
            throw new UsbRelayOpenException("Не удалось соединить потоки", e);
        }

        Thread usbThread = new Thread(createUsbTask());
        usbThread.setDaemon(true);
        usbThread.start();
    }

    private Runnable createUsbTask() {
        return () -> {
            try {
                int result = LibUsb.init(null);

                if (result != LibUsb.SUCCESS) {
                    throw new LibUsbException("Не могу открыть устройство", result);
                }

                if (handle == null) {
                    log.error("Ошибка захвата USB-интерфейса!");
                }

                LibUsb.setAutoDetachKernelDriver(handle, true);
                result = LibUsb.claimInterface(handle, usbPort.getInterfaceNumber());

                if (result != LibUsb.SUCCESS) {
                    log.error("Ошибка захвата USB-интерфейса!", new LibUsbException("Unable to claim interface", result));
                }

                byte endOut = usbPort.getEndpointAddressOut().getUsbEndpointDescriptor().bEndpointAddress();
                int packetSize = usbPort.getEndpointAddressOut().getUsbEndpointDescriptor().wMaxPacketSize();

                byte[] buffer = new byte[packetSize];
                int bytesRead;
                while (!closed && (bytesRead = pipedInputStream.read(buffer)) != -1) {
                    int offset = 0;
                    while (offset < bytesRead) {
                        int sent = UsbTools.write(handle, endOut, Arrays.copyOfRange(buffer, offset, bytesRead));
                        offset += sent;
                    }
                }
            } catch (Exception e) {
                log.error("Произошла ошибка при обработке USB-интерфейса!", e);
            }
        };
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;

        try {
            if (pipedOutputStream != null)
                pipedOutputStream.close();
            if (pipedInputStream != null)
                pipedInputStream.close();
        } catch (IOException e) {
            log.error("Ошибка закрытия стримов!", e);
        }

        int result = LibUsb.releaseInterface(handle, usbPort.getInterfaceNumber());
        if (result != LibUsb.SUCCESS) {
            log.error("Ошибка при отключении от USB-интерфейса!", new LibUsbException("Unable to release interface", result));
        }

        LibUsb.close(handle);
        LibUsb.exit(null);
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
        pipedOutputStream.flush();
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Поток уже закрыт!");
        }
    }
}
