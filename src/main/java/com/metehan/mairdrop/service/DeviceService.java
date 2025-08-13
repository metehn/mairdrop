package com.metehan.mairdrop.service;

import com.metehan.mairdrop.model.DeviceSession;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceService {
    private final Map<String, DeviceSession> devices = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToDevice = new ConcurrentHashMap<>(); // sessionId -> deviceId

    public void registerDevice(String deviceId) {
        registerDevice(deviceId, null);
    }

    public void registerDevice(String deviceId, String sessionId) {
        DeviceSession session = new DeviceSession(deviceId);
        session.setSessionId(sessionId);
        devices.put(deviceId, session);

        if (sessionId != null) {
            sessionToDevice.put(sessionId, deviceId);
        }

        System.out.println("Cihaz kaydedildi: " + deviceId + " (Session: " + sessionId + ")");
    }

    public void unregisterDevice(String deviceId) {
        DeviceSession session = devices.get(deviceId);
        if (session != null) {
            session.setActive(false);
            if (session.getSessionId() != null) {
                sessionToDevice.remove(session.getSessionId());
            }
            System.out.println("Cihaz kaydı silindi: " + deviceId);
        }
    }

    public void markDeviceActive(String deviceId) {
        DeviceSession session = devices.get(deviceId);
        if (session != null) {
            session.setActive(true);
        }
    }

    public String getDeviceIdBySessionId(String sessionId) {
        return sessionToDevice.get(sessionId);
    }

    public List<String> getActiveDevices() {
        List<String> activeDevices = new ArrayList<>();
        for (DeviceSession device : devices.values()) {
            if (device.isActive()) {
                activeDevices.add(device.getDeviceId());
            }
        }
        System.out.println("Aktif cihazlar: " + activeDevices);
        return activeDevices;
    }

    public int getDeviceCount() {
        return (int) devices.values().stream().filter(DeviceSession::isActive).count();
    }

    public void cleanupInactiveDevices() {
        long currentTime = System.currentTimeMillis();
        long maxInactiveTime = 30 * 60 * 1000; // 30 dakika

        devices.entrySet().removeIf(entry -> {
            DeviceSession session = entry.getValue();
            if (!session.isActive() || (currentTime - session.getLastSeen() > maxInactiveTime)) {
                if (session.getSessionId() != null) {
                    sessionToDevice.remove(session.getSessionId());
                }
                return true;
            }
            return false;
        });
    }
}