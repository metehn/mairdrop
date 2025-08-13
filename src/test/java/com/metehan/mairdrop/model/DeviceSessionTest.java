package com.metehan.mairdrop.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeviceSessionTest {

    @Test
    void testConstructorWithDeviceId() {
        DeviceSession session = new DeviceSession("device1");

        assertEquals("device1", session.getDeviceId());
        assertEquals("device1", session.getDeviceName());
        assertTrue(session.isActive());
        assertNotNull(session.getLastSeen());
    }

    @Test
    void testConstructorWithDeviceIdAndName() {
        DeviceSession session = new DeviceSession("device1", "MyDevice");

        assertEquals("device1", session.getDeviceId());
        assertEquals("MyDevice", session.getDeviceName());
        assertTrue(session.isActive());
        assertNotNull(session.getLastSeen());

        DeviceSession session2 = new DeviceSession("device2", null);
        assertEquals("device2", session2.getDeviceName());
    }

    @Test
    void testSettersAndGetters() {
        DeviceSession session = new DeviceSession("device1");
        session.setDeviceId("newId");
        session.setDeviceName("NewName");
        session.setSessionId("sess123");
        session.setIpAddress("127.0.0.1");

        assertEquals("newId", session.getDeviceId());
        assertEquals("NewName", session.getDeviceName());
        assertEquals("sess123", session.getSessionId());
        assertEquals("127.0.0.1", session.getIpAddress());
    }

    @Test
    void testSetActiveAndUpdateLastSeen() throws InterruptedException {
        DeviceSession session = new DeviceSession("device1");
        long originalLastSeen = session.getLastSeen();

        Thread.sleep(10);
        session.setActive(true);
        assertTrue(session.isActive());
        assertTrue(session.getLastSeen() >= originalLastSeen);

        long oldLastSeen = session.getLastSeen();
        Thread.sleep(10);
        session.updateLastSeen();
        assertTrue(session.getLastSeen() > oldLastSeen);
    }

    @Test
    void testSetLastSeen() {
        DeviceSession session = new DeviceSession("device1");
        session.setLastSeen(123456789L);
        assertEquals(123456789L, session.getLastSeen());
    }
}
