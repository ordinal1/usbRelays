package dev.ordinal1.ru.DTO;

import javax.usb.UsbDevice;
import javax.usb.UsbEndpoint;

public record RelayUsbPort(UsbDevice device, byte interfaceNumber, UsbEndpoint endpointAddressOut) {
}
