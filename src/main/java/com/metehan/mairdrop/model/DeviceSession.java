package com.metehan.mairdrop.model;

public class DeviceSession {
    private String deviceId;
    private String deviceName;
    private String sessionId;
    private boolean active;
    private long lastSeen;
    private String ipAddress;

    public DeviceSession(String deviceId) {
        this.deviceId = deviceId;
        this.deviceName = deviceId;
        this.active = true;
        this.lastSeen = System.currentTimeMillis();
    }

    public DeviceSession(String deviceId, String deviceName) {
        this.deviceId = deviceId;
        this.deviceName = deviceName != null ? deviceName : deviceId;
        this.active = true;
        this.lastSeen = System.currentTimeMillis();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            this.lastSeen = System.currentTimeMillis();
        }
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }
}