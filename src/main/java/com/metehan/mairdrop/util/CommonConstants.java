package com.metehan.mairdrop.util;

public final class CommonConstants {

    public static final String NETWORK_GROUP = "NETWORK_GROUP";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String LOCAL_NETWORK = "LOCAL_NETWORK";

    // STOMP CONNECT header names carrying the client's identity, and the session-attribute key the
    // auth interceptor binds the verified device id under so later SUBSCRIBE frames can be scoped.
    public static final String HEADER_DEVICE_ID = "deviceId";
    public static final String HEADER_TOKEN = "token";
    public static final String SESSION_DEVICE_ID = "DEVICE_ID";

    // Client IP resolved at the handshake, stashed so room-join brute-force can be rate-limited per IP.
    public static final String SESSION_CLIENT_IP = "CLIENT_IP";

    private CommonConstants() {
        // utility class
    }
}
