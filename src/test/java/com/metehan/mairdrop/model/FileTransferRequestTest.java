package com.metehan.mairdrop.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileTransferRequestTest {

    @Test
    void testGettersAndSetters() {
        FileTransferRequest request = new FileTransferRequest();

        request.setSenderDeviceId("sender1");
        request.setTargetDeviceId("target1");
        request.setFileName("file.txt");

        assertEquals("sender1", request.getSenderDeviceId());
        assertEquals("target1", request.getTargetDeviceId());
        assertEquals("file.txt", request.getFileName());
    }

    @Test
    void testDefaultValues() {
        FileTransferRequest request = new FileTransferRequest();

        assertNull(request.getSenderDeviceId());
        assertNull(request.getTargetDeviceId());
        assertNull(request.getFileName());
    }
}
