package com.metehan.mairdrop.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Single owner of network-group device-list broadcasts. Routing every publish path (registration,
 * disconnect, visibility toggles) through here keeps the "skip devices that are in a room" rule in
 * one place, so a network broadcast can never overwrite a room member's room-scoped device view.
 */
@Component
public class GroupBroadcaster {

    private final DeviceService deviceService;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public GroupBroadcaster(DeviceService deviceService, RoomService roomService,
                            SimpMessagingTemplate messagingTemplate) {
        this.deviceService = deviceService;
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Publishes the group's visible-device list to every active member of the group, skipping any
     * device that is currently in a public room or hidden-from-room — those devices display a
     * room-scoped list that a network broadcast must not overwrite. Hidden-from-network devices are
     * still notified: they stay on the page and need to see the network list even while invisible
     * to others.
     */
    public void broadcastGroup(String group) {
        if (group == null) {
            return;
        }
        List<String> visibleDevices = deviceService.getActiveDevicesInGroup(group);
        for (String id : deviceService.getAllActiveDevicesInGroup(group)) {
            if (roomService.getRoomCode(id) == null && deviceService.getPendingRoomCode(id) == null) {
                messagingTemplate.convertAndSend("/topic/devices/" + id, visibleDevices);
            }
        }
    }
}
