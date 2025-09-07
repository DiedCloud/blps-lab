package com.example.blps.jca;

import com.example.blps.dao.controller.model.HiveApiResponseDTO;
import com.example.blps.dao.controller.model.ModerationResultDTO;
import com.example.blps.exception.HiveException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HiveConnection implements Connection, AutoCloseable {
    private final HiveClient hive;

    public ModerationResultDTO moderate(String text) throws ResourceException {
        try {
            HiveApiResponseDTO apiResponse = hive.moderate(text).orElseThrow(() ->  new HiveException("Failed to send message to Hive"));

            return ModerationResultDTO.fromHive(apiResponse);

        } catch (Exception e) {
            throw new ResourceException("Error calling Hive", e);
        }
    }

    @Override
    public Interaction createInteraction() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        return null;
    }

    @Override
    public void close() throws ResourceException {
    }
}
