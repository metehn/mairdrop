package com.metehan.mairdrop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class CleanupServiceTest {

    private DeviceService deviceService;
    private FileSharingService fileSharingService;
    private CleanupService cleanupService;

    @BeforeEach
    void setUp() {
        deviceService = mock(DeviceService.class);
        fileSharingService = mock(FileSharingService.class);
        cleanupService = new CleanupService(deviceService, fileSharingService);
    }

    @Test
    void testCleanupInactiveDevices() {
        cleanupService.cleanupInactiveDevices();

        verify(deviceService, times(1)).cleanupInactiveDevices();
        verifyNoInteractions(fileSharingService);
    }

    @Test
    void testCleanupOldFiles() {
        cleanupService.cleanupOldFiles();

        verify(fileSharingService, times(1)).cleanupOldFiles();
        verifyNoInteractions(deviceService);
    }
}
