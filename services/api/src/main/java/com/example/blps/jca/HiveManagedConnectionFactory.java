package com.example.blps.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Set;

@RequiredArgsConstructor
public class HiveManagedConnectionFactory implements ManagedConnectionFactory {
    private final HiveClient hive;

    @Override
    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return new HiveConnectionFactory(this);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new HiveConnectionFactory(this);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo info) {
        return new HiveManagedConnection(hive);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws ResourceException {
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

}
