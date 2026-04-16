package dev.ordinal1.ru.Interfaces;

import dev.ordinal1.ru.DTO.RelayDevice;
import dev.ordinal1.ru.Enums.RelayOperation;
import dev.ordinal1.ru.Enums.RelayType;

import java.io.IOException;

public interface RelayInterface extends AutoCloseable{
    boolean isConnected(RelayType type);

    boolean isConnected(RelayDevice relayDevice);

    boolean notReconnected() throws IOException;
    void sendCommand(RelayOperation operation) throws IOException, InterruptedException;
    void sendCommand(RelayOperation operation, long ms) throws IOException, InterruptedException;
    void close() throws Exception;
}
