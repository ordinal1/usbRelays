package dev.ordinal1.ru.DTO;

import lombok.Data;

import javax.usb.UsbDevice;
import javax.usb.UsbEndpoint;

@Data
public class UsbPort {
    private UsbDevice device;
    private byte interfaceNumber;
    private UsbEndpoint endpointAddressOut;
    private UsbEndpoint endpointAddressIn;
}
