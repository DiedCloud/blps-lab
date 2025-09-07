package com.example.blps.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import lombok.RequiredArgsConstructor;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.security.auth.Subject;

@RequiredArgsConstructor
public class HiveConnectionFactory implements ConnectionFactory {
    private final HiveManagedConnectionFactory managedConnectionFactory;

    @Override
    public Connection getConnection() throws ResourceException {
        var subject = new Subject();
        var managedConnection = managedConnectionFactory.createManagedConnection(subject, null);
        return (Connection) managedConnection.getConnection(subject, null);
    }

    @Override
    public Connection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
        return null;
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        return null;
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public void setReference(Reference reference) {
    }

    @Override
    public Reference getReference() throws NamingException {
        return null;
    }
}
