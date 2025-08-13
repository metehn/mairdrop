package com.metehan.mairdrop.controller;

import com.metehan.mairdrop.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WebSocketControllerTest {

    private DeviceService deviceService;
    private SimpMessagingTemplate messagingTemplate;
    private WebSocketController controller;

    @BeforeEach
    void setUp() {
        deviceService = mock(DeviceService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        controller = new WebSocketController(deviceService, messagingTemplate);
    }

    @Test
    void testRegisterDevice() {
        String deviceId = "device1";
        when(deviceService.getActiveDevices()).thenReturn(List.of("device1", "device2"));

        controller.registerDevice(deviceId);

        verify(deviceService).registerDevice(deviceId);

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/devices"), captor.capture());

        List<String> activeDevices = captor.getValue();
        assertEquals(2, activeDevices.size());
        assertEquals("device1", activeDevices.get(0));
    }

    @Test
    void testUnregisterDevice() {
        String deviceId = "device1";
        when(deviceService.getActiveDevices()).thenReturn(List.of("device2"));

        controller.unregisterDevice(deviceId);

        verify(deviceService).unregisterDevice(deviceId);

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/devices"), captor.capture());

        List<String> activeDevices = captor.getValue();
        assertEquals(1, activeDevices.size());
        assertEquals("device2", activeDevices.get(0));
    }
}
