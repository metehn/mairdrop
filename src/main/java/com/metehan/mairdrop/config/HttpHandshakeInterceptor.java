package com.metehan.mairdrop.config;

import com.metehan.mairdrop.util.CommonConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;

public class HttpHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger log = LoggerFactory.getLogger(HttpHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {

            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String ip = getClientIp(httpRequest);

            String group = isLocalNetwork(ip) ? CommonConstants.LOCAL_NETWORK : ip;

            attributes.put(CommonConstants.NETWORK_GROUP, group);
            attributes.put(CommonConstants.SESSION_CLIENT_IP, ip);
            log.info("Handshake IP: {} -> Group: {}", ip, group);
        }
        return true;
    }

    private boolean isLocalNetwork(String rawIp) {
        String ip = normalizeIp(rawIp);
        return ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || (ip.startsWith("172.") && isPrivate172(ip))
                || ip.equals("127.0.0.1")
                || ip.startsWith("127.")
                || ip.equals("::1")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("fe80:");
    }

    // Strip the IPv4-mapped IPv6 prefix so dual-stack peers reported as ::ffff:192.168.1.5 still
    // match the plain "192.168." style private-range checks.
    private String normalizeIp(String ip) {
        if (ip == null) {
            return "";
        }
        if (ip.startsWith("::ffff:") && ip.indexOf('.') > 0) {
            return ip.substring("::ffff:".length());
        }
        return ip;
    }

    // RFC 1918: 172.16.0.0/12 covers 172.16.x.x through 172.31.x.x
    private boolean isPrivate172(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwarded = request.getHeader(CommonConstants.X_FORWARDED_FOR);

        // Only trust X-Forwarded-For when the direct peer is our own reverse proxy (a
        // private/loopback address). A client connecting directly could otherwise spoof the header
        // to land itself in the LOCAL_NETWORK group and reach every device on the real LAN.
        if (forwarded != null && !forwarded.isBlank() && isLocalNetwork(remoteAddr)) {
            String[] hops = forwarded.split(",");
            // The rightmost entry is the address the trusted proxy actually observed; entries to its
            // left are attacker-controllable and must be ignored. Scan from the right for the first
            // non-blank hop so a degenerate header (e.g. "," or a trailing comma) neither throws nor
            // yields an empty group; fall back to the direct peer if every hop is blank.
            for (int i = hops.length - 1; i >= 0; i--) {
                String hop = hops[i].trim();
                if (!hop.isEmpty()) {
                    return hop;
                }
            }
        }
        return remoteAddr != null ? remoteAddr.trim() : "";
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
    }
}