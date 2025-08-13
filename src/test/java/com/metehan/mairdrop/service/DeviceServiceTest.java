package com.metehan.mairdrop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeviceServiceTest {

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService();
    }

    @Test
    void testRegisterDeviceWithoutSession() {
        deviceService.registerDevice("device1");

        List<String> active = deviceService.getActiveDevices();
        assertEquals(1, active.size());
        assertTrue(active.contains("device1"));
        assertNull(deviceService.getDeviceIdBySessionId("anySession"));
    }

    @Test
    void testRegisterDeviceWithSession() {
        deviceService.registerDevice("device2", "sess1");

        assertEquals("device2", deviceService.getDeviceIdBySessionId("sess1"));

        List<String> active = deviceService.getActiveDevices();
        assertEquals(1, active.size());
        assertTrue(active.contains("device2"));
    }

    @Test
    void testUnregisterDevice() {
        deviceService.registerDevice("device3", "sess3");
        deviceService.unregisterDevice("device3");

        assertEquals(0, deviceService.getDeviceCount());
        assertNull(deviceService.getDeviceIdBySessionId("sess3"));
        assertTrue(deviceService.getActiveDevices().isEmpty());
    }

    @Test
    void testMarkDeviceActive() throws InterruptedException {
        deviceService.registerDevice("device4");
        deviceService.unregisterDevice("device4");

        assertEquals(0, deviceService.getDeviceCount());

        deviceService.markDeviceActive("device4");
        assertEquals(1, deviceService.getDeviceCount());
    }

    @Test
    void testGetActiveDevicesAndCount() {
        deviceService.registerDevice("d1");
        deviceService.registerDevice("d2");
        deviceService.registerDevice("d3");

        deviceService.unregisterDevice("d2");

        List<String> active = deviceService.getActiveDevices();
        assertEquals(2, active.size());
        assertTrue(active.contains("d1"));
        assertTrue(active.contains("d3"));
        assertEquals(2, deviceService.getDeviceCount());
    }

    @Test
    void testCleanupInactiveDevices() throws InterruptedException {
        deviceService.registerDevice("d1");
        deviceService.registerDevice("d2");
        deviceService.unregisterDevice("d1");

        assertEquals(1, deviceService.getActiveDevices().size());
        assertEquals(1, deviceService.getDeviceCount());

        deviceService.cleanupInactiveDevices();

        List<String> active = deviceService.getActiveDevices();
        assertEquals(1, active.size());
        assertTrue(active.contains("d2"));
    }
}
