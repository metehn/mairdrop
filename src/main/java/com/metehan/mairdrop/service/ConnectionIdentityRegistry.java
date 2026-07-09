package com.metehan.mairdrop.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which ownership token currently holds each device id across live WebSocket sessions.
 *
 * <p>A device id is the public routing address peers use to reach each other, so it cannot be
 * secret. Ownership of that id — and therefore the right to subscribe to its private topics — is
 * proven with a token the owning browser keeps to itself. This registry enforces one token per
 * device id at connect time: a session presenting a different token for an id another live session
 * already holds is refused, which stops an attacker from binding a victim's id to eavesdrop on it.
 */
@Component
public class ConnectionIdentityRegistry {

    private final Map<String, String> deviceIdToToken = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToDeviceId = new ConcurrentHashMap<>();

    /**
     * Attempts to bind {@code deviceId} (with {@code token}) to {@code sessionId}.
     *
     * @return true if the id is unclaimed or already held by the same token (a legitimate
     * reconnect / refresh); false if a different token already holds it (a takeover attempt).
     */
    public synchronized boolean claim(String sessionId, String deviceId, String token) {
        // A null token (a legacy/tokenless client) is normalised to "" so the map — which forbids
        // null values — can still track it; two tokenless claims for the same id then agree.
        String normalized = (token != null) ? token : "";
        String currentToken = deviceIdToToken.get(deviceId);
        if (currentToken != null && !currentToken.equals(normalized)) {
            return false;
        }
        deviceIdToToken.put(deviceId, normalized);
        sessionToDeviceId.put(sessionId, deviceId);
        return true;
    }

    /** Releases whatever id {@code sessionId} held, keeping the id's token if another live session still holds it. */
    public synchronized void release(String sessionId) {
        String deviceId = sessionToDeviceId.remove(sessionId);
        if (deviceId != null && !sessionToDeviceId.containsValue(deviceId)) {
            deviceIdToToken.remove(deviceId);
        }
    }
}
