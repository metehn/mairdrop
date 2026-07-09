package com.metehan.mairdrop.config;

import com.metehan.mairdrop.service.ConnectionIdentityRegistry;
import com.metehan.mairdrop.util.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Authorises inbound STOMP frames so a client can only listen on its own device's topics.
 *
 * <p>The SimpleBroker itself performs no authorisation: without this, any connection could subscribe
 * to {@code /topic/**​/{someoneElsesDeviceId}} and eavesdrop on that device's peer list, room events
 * (including the room code), WebRTC signalling (file names/sizes/hashes, SDP, ICE) and visibility
 * changes. On CONNECT this binds the client's device id to the session after proving ownership with
 * its token; every SUBSCRIBE is then required to target that same device id.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);
    private static final Set<String> DEVICE_SCOPED_TOPICS = Set.of("devices", "webrtc", "room", "visibility");

    private final ConnectionIdentityRegistry identityRegistry;

    public StompAuthChannelInterceptor(ConnectionIdentityRegistry identityRegistry) {
        this.identityRegistry = identityRegistry;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT || command == StompCommand.STOMP) {
            authorizeConnect(accessor);
        } else if (command == StompCommand.SUBSCRIBE) {
            authorizeSubscribe(accessor);
        }
        return message;
    }

    private void authorizeConnect(StompHeaderAccessor accessor) {
        String deviceId = accessor.getFirstNativeHeader(CommonConstants.HEADER_DEVICE_ID);
        String token = accessor.getFirstNativeHeader(CommonConstants.HEADER_TOKEN);
        if (deviceId == null || deviceId.isBlank()) {
            log.warn("STOMP CONNECT rejected: no deviceId header");
            throw new MessagingException("A deviceId is required to connect");
        }
        if (!identityRegistry.claim(accessor.getSessionId(), deviceId, token)) {
            log.warn("STOMP CONNECT rejected for {}: device id already held by a different token", deviceId);
            throw new MessagingException("Device id is already in use");
        }
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes != null) {
            attributes.put(CommonConstants.SESSION_DEVICE_ID, deviceId);
        }
    }

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        String[] parts = destination.split("/");
        // Only device-scoped topics ("/topic/{type}/{deviceId}") are guarded; anything else passes.
        if (parts.length < 4 || !"topic".equals(parts[1]) || !DEVICE_SCOPED_TOPICS.contains(parts[2])) {
            return;
        }
        String target = parts[3];
        Map<String, Object> attributes = accessor.getSessionAttributes();
        String owner = (attributes != null) ? (String) attributes.get(CommonConstants.SESSION_DEVICE_ID) : null;
        if (owner == null || !owner.equals(target)) {
            log.warn("SUBSCRIBE rejected: session for {} may not subscribe to {}", owner, destination);
            throw new MessagingException("Not allowed to subscribe to another device's topic");
        }
    }
}
