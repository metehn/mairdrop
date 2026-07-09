package com.metehan.mairdrop.config;

import com.metehan.mairdrop.service.ConnectionIdentityRegistry;
import com.metehan.mairdrop.util.CommonConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StompAuthChannelInterceptorTest {

    private StompAuthChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new StompAuthChannelInterceptor(new ConnectionIdentityRegistry());
    }

    private Message<byte[]> frame(StompCommand command, String sessionId,
                                  Map<String, Object> attributes, Consumer<StompHeaderAccessor> customize) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId(sessionId);
        accessor.setSessionAttributes(attributes);
        customize.accept(accessor);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("CONNECT binds the device id; the session may then subscribe to its own topic")
    void connectBindsAndOwnSubscribeAllowed() {
        Map<String, Object> attrs = new HashMap<>();
        interceptor.preSend(frame(StompCommand.CONNECT, "s1", attrs, a -> {
            a.setNativeHeader(CommonConstants.HEADER_DEVICE_ID, "dev1");
            a.setNativeHeader(CommonConstants.HEADER_TOKEN, "t1");
        }), null);

        assertEquals("dev1", attrs.get(CommonConstants.SESSION_DEVICE_ID));
        assertDoesNotThrow(() -> interceptor.preSend(
                frame(StompCommand.SUBSCRIBE, "s1", attrs, a -> a.setDestination("/topic/devices/dev1")), null));
        assertDoesNotThrow(() -> interceptor.preSend(
                frame(StompCommand.SUBSCRIBE, "s1", attrs, a -> a.setDestination("/topic/webrtc/dev1")), null));
    }

    @Test
    @DisplayName("CONNECT without a deviceId header is rejected")
    void connectWithoutDeviceIdRejected() {
        assertThrows(MessagingException.class, () -> interceptor.preSend(
                frame(StompCommand.CONNECT, "s1", new HashMap<>(), a -> { }), null));
    }

    @Test
    @DisplayName("CONNECT claiming an id already held by a different token is rejected")
    void connectTakeoverRejected() {
        interceptor.preSend(frame(StompCommand.CONNECT, "s1", new HashMap<>(), a -> {
            a.setNativeHeader(CommonConstants.HEADER_DEVICE_ID, "dev1");
            a.setNativeHeader(CommonConstants.HEADER_TOKEN, "owner");
        }), null);

        assertThrows(MessagingException.class, () -> interceptor.preSend(
                frame(StompCommand.CONNECT, "s2", new HashMap<>(), a -> {
                    a.setNativeHeader(CommonConstants.HEADER_DEVICE_ID, "dev1");
                    a.setNativeHeader(CommonConstants.HEADER_TOKEN, "attacker");
                }), null));
    }

    @Test
    @DisplayName("a session may NOT subscribe to another device's topic")
    void foreignSubscribeRejected() {
        Map<String, Object> attrs = new HashMap<>();
        interceptor.preSend(frame(StompCommand.CONNECT, "s1", attrs, a -> {
            a.setNativeHeader(CommonConstants.HEADER_DEVICE_ID, "dev1");
            a.setNativeHeader(CommonConstants.HEADER_TOKEN, "t1");
        }), null);

        assertThrows(MessagingException.class, () -> interceptor.preSend(
                frame(StompCommand.SUBSCRIBE, "s1", attrs, a -> a.setDestination("/topic/devices/victim")), null));
        assertThrows(MessagingException.class, () -> interceptor.preSend(
                frame(StompCommand.SUBSCRIBE, "s1", attrs, a -> a.setDestination("/topic/room/victim")), null));
    }

    @Test
    @DisplayName("non device-scoped destinations are not restricted")
    void unscopedDestinationAllowed() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(CommonConstants.SESSION_DEVICE_ID, "dev1");

        assertDoesNotThrow(() -> interceptor.preSend(
                frame(StompCommand.SUBSCRIBE, "s1", attrs, a -> a.setDestination("/topic/public/news")), null));
    }
}
