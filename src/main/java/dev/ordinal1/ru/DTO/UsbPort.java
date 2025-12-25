package dev.ordinal1.ru.DTO;

import javax.usb.UsbDevice;
import javax.usb.UsbEndpoint;

public class UsbPort {
    private UsbDevice device;
    private byte interfaceNumber;
    private UsbEndpoint endpointAddressOut;

    public void setDevice(UsbDevice device) {
        this.device = device;
    }

    public void setInterfaceNumber(byte interfaceNumber) {
        this.interfaceNumber = interfaceNumber;
    }

    public void setEndpointAddressOut(UsbEndpoint endpointAddressOut) {
        this.endpointAddressOut = endpointAddressOut;
    }

    public UsbDevice getDevice() {
        return device;
    }

    public byte getInterfaceNumber() {
        return interfaceNumber;
    }

    public UsbEndpoint getEndpointAddressOut() {
        return endpointAddressOut;
    }
}
