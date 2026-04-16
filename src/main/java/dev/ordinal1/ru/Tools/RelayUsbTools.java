package dev.ordinal1.ru.Tools;

import dev.ordinal1.ru.DTO.RelayDevice;
import dev.ordinal1.ru.DTO.RelayUsbPort;
import dev.ordinal1.ru.Enums.RelayType;
import org.usb4java.BufferUtils;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import javax.usb.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;

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

    public static boolean isConnected(RelayDevice relayDevice) {
        UsbDevice targetDevice = RelayUsbTools.findDeviceRecursively(RelayUsbTools.getRootHub(), relayDevice);
        if (targetDevice == null) return false;
        return RelayUsbTools.configureUsbPort(targetDevice) != null;
    }

    public static UsbDevice findDeviceRecursively(UsbHub hub, RelayDevice relayDevice) {
        for (Object deviceOrHub : hub.getAttachedUsbDevices()) {
            if (deviceOrHub instanceof UsbDevice device) {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();

                if (Objects.equals(desc.idVendor(), relayDevice.vid()) && Objects.equals(desc.idProduct(), relayDevice.pid())) {
                    return device;
                }
            }

            if (deviceOrHub instanceof UsbHub subHub) {
                UsbDevice result = findDeviceRecursively(subHub, relayDevice); // Рекурсивный вызов
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static RelayUsbPort configureUsbPort(UsbDevice device) {
        UsbEndpoint out = findEndpoint(device);
        if (out == null) return null;
        return new RelayUsbPort(device, (byte) 0x00, out);
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
