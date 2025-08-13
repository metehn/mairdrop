package com.metehan.mairdrop.controller;

import com.metehan.mairdrop.service.DeviceService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private final DeviceService deviceService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(DeviceService deviceService, SimpMessagingTemplate messagingTemplate) {
        this.deviceService = deviceService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/register")
    public void registerDevice(String deviceId) {
        deviceService.registerDevice(deviceId);
        broadcastDeviceList();
    }

    @MessageMapping("/unregister")
    public void unregisterDevice(String deviceId) {
        deviceService.unregisterDevice(deviceId);
        broadcastDeviceList();
    }

    private void broadcastDeviceList() {
        messagingTemplate.convertAndSend("/topic/devices", deviceService.getActiveDevices());
    }
}