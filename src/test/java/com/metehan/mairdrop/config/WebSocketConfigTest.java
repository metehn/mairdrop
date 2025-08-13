package com.metehan.mairdrop.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.SimpleBrokerRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    private WebSocketConfig webSocketConfig;

    @BeforeEach
    void setUp() {
        webSocketConfig = new WebSocketConfig();
    }

    @Test
    void testConfigureMessageBroker() {
        MessageBrokerRegistry registry = Mockito.mock(MessageBrokerRegistry.class);
        SimpleBrokerRegistration brokerRegistration = Mockito.mock(SimpleBrokerRegistration.class);

        when(registry.enableSimpleBroker("/topic", "/queue")).thenReturn(brokerRegistration);
        when(registry.setApplicationDestinationPrefixes("/app")).thenReturn(registry);

        webSocketConfig.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic", "/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void testRegisterStompEndpoints() {
        StompEndpointRegistry endpointRegistry = Mockito.mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = Mockito.mock(StompWebSocketEndpointRegistration.class);

        when(endpointRegistry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns("*")).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(null);

        webSocketConfig.registerStompEndpoints(endpointRegistry);

        verify(endpointRegistry).addEndpoint("/ws");
        verify(registration).setAllowedOriginPatterns("*");
        verify(registration).withSockJS();
    }
}
