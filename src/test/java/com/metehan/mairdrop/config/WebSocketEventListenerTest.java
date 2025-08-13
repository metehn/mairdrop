package com.metehan.mairdrop.config;

import com.metehan.mairdrop.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

import static org.mockito.Mockito.*;

class WebSocketEventListenerTest {

    private DeviceService deviceService;
    private SimpMessagingTemplate messagingTemplate;
    private WebSocketEventListener listener;

    @BeforeEach
    void setUp() {
        deviceService = mock(DeviceService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        listener = new WebSocketEventListener(deviceService, messagingTemplate);
    }

    @Test
    void testHandleWebSocketConnectListener() {
        // Arrange
        org.springframework.messaging.Message<String> message =
                new org.springframework.messaging.support.GenericMessage<>("payload");

        SessionConnectEvent event = mock(SessionConnectEvent.class);
        doReturn(message).when(event).getMessage();

        listener.handleWebSocketConnectListener(event);

        verify(event, times(1)).getMessage();
    }


    @Test
    void testHandleWebSocketDisconnectListener_withValidDevice() {
        String sessionId = "123";
        String deviceId = "device-1";

        SessionDisconnectEvent event = mock(SessionDisconnectEvent.class);
        when(event.getSessionId()).thenReturn(sessionId);
        when(deviceService.getDeviceIdBySessionId(sessionId)).thenReturn(deviceId);
        when(deviceService.getActiveDevices()).thenReturn(List.of("device-1", "device-2"));

        listener.handleWebSocketDisconnectListener(event);

        verify(deviceService).getDeviceIdBySessionId(sessionId);
        verify(deviceService).unregisterDevice(deviceId);
        verify(messagingTemplate).convertAndSend("/topic/devices", List.of("device-1", "device-2"));
    }

    @Test
    void testHandleWebSocketDisconnectListener_withNullDevice() {
        String sessionId = "123";
        SessionDisconnectEvent event = mock(SessionDisconnectEvent.class);
        when(event.getSessionId()).thenReturn(sessionId);
        when(deviceService.getDeviceIdBySessionId(sessionId)).thenReturn(null);

        listener.handleWebSocketDisconnectListener(event);

        verify(deviceService).getDeviceIdBySessionId(sessionId);
        verify(deviceService, never()).unregisterDevice(anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

}
