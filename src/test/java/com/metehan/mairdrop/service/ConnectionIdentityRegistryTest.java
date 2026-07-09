package com.metehan.mairdrop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionIdentityRegistryTest {

    private ConnectionIdentityRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ConnectionIdentityRegistry();
    }

    @Test
    @DisplayName("first claim of a device id succeeds and binds its token")
    void firstClaimSucceeds() {
        assertTrue(registry.claim("s1", "dev1", "owner-token"));
    }

    @Test
    @DisplayName("a different token cannot claim a device id already held (takeover refused)")
    void differentTokenRefused() {
        registry.claim("s1", "dev1", "owner-token");

        assertFalse(registry.claim("s2", "dev1", "attacker-token"));
    }

    @Test
    @DisplayName("the same token may re-claim its id (legitimate reconnect while the old session lingers)")
    void sameTokenReclaimAllowed() {
        registry.claim("s1", "dev1", "owner-token");

        assertTrue(registry.claim("s2", "dev1", "owner-token"));
    }

    @Test
    @DisplayName("an id becomes claimable by a new token only once every holding session has released it")
    void reclaimableAfterFullRelease() {
        registry.claim("s1", "dev1", "owner-token");
        registry.claim("s2", "dev1", "owner-token");

        registry.release("s1");
        assertFalse(registry.claim("s3", "dev1", "other-token"), "still held by s2");

        registry.release("s2");
        assertTrue(registry.claim("s3", "dev1", "other-token"), "free after the last holder left");
    }

    @Test
    @DisplayName("tokenless (legacy) claims for the same id agree with each other")
    void tokenlessClaimsAgree() {
        assertTrue(registry.claim("s1", "dev1", null));
        assertTrue(registry.claim("s2", "dev1", null));
    }
}
