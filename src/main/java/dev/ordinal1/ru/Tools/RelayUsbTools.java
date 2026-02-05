package dev.ordinal1.ru.Tools;

import dev.ordinal1.ru.DTO.RelayUsbPort;
import org.usb4java.BufferUtils;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import javax.usb.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class RelayUsbTools {
    private static final UsbHub rootHub;

    public static UsbHub getRootHub() {
        return rootHub;
    }

    static {
        try {
            rootHub = UsbHostManager.getUsbServices().getRootUsbHub();
        } catch (UsbException e) {
            throw new RuntimeException(e);
        }
    }

    public static UsbDevice findDeviceRecursively(UsbHub hub, short vid, short pid) {
        for (Object deviceOrHub : hub.getAttachedUsbDevices()) {
            if (deviceOrHub instanceof UsbDevice device) {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();

                if (desc.idVendor() == vid && desc.idProduct() == pid) {
                    return device;
                }
            }

            if (deviceOrHub instanceof UsbHub subHub) {
                UsbDevice result = findDeviceRecursively(subHub, vid, pid); // Рекурсивный вызов
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static RelayUsbPort configureUsbPort(UsbDevice device) {
        RelayUsbPort relayUsbPort = new RelayUsbPort();
        relayUsbPort.setDevice(device);
        relayUsbPort.setInterfaceNumber((byte) 0x00);
        UsbEndpoint out = findEndpoint(device);
        if (out == null) return null;
        relayUsbPort.setEndpointAddressOut(out);

        return relayUsbPort;
    }

    private static UsbEndpoint findEndpoint(UsbDevice device) {
        for (UsbConfiguration configuration : (List<UsbConfiguration>) device.getUsbConfigurations()) {
            for (UsbInterface iface : (List<UsbInterface>) configuration.getUsbInterfaces()) {
                for (UsbEndpoint endpoint : (List<UsbEndpoint>) iface.getUsbEndpoints()) {
                    if (endpoint.getDirection() == UsbConst.ENDPOINT_DIRECTION_OUT) {
                        return endpoint;
                    }
                }
            }
        }
        return null;
    }

    public static int write(DeviceHandle handle, byte endpointOut, byte[] data){
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return write(handle, endpointOut, buffer);
    }

    public static int write(DeviceHandle handle, byte endpointOut, ByteBuffer data){
        IntBuffer transferred = IntBuffer.allocate(1);
        int result = LibUsb.bulkTransfer(handle, endpointOut, data,
                transferred, 2000);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to send data", result);
        }

        return transferred.get();
    }
}
