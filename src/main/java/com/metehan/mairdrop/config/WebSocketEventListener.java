package com.metehan.mairdrop.config;

import com.metehan.mairdrop.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final DeviceService deviceService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(DeviceService deviceService, SimpMessagingTemplate messagingTemplate) {
        this.deviceService = deviceService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        log.info("New connection: {}", event.getMessage().getHeaders());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("Connection closed: {}", sessionId);

        String deviceId = deviceService.getDeviceIdBySessionId(sessionId);
        if (deviceId != null) {
            deviceService.unregisterDevice(deviceId);
            broadcastDeviceList();
        }
    }

    private void broadcastDeviceList() {
        messagingTemplate.convertAndSend("/topic/devices", deviceService.getActiveDevices());
    }
}
