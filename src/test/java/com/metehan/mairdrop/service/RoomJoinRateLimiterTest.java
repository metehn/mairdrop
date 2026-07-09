package com.metehan.mairdrop.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomJoinRateLimiterTest {

    @Test
    @DisplayName("allows attempts until the failure threshold, then blocks")
    void blocksAfterThreshold() {
        RoomJoinRateLimiter limiter = new RoomJoinRateLimiter(3, 60_000L);

        assertTrue(limiter.isAllowed("ip1"));
        limiter.recordFailure("ip1");
        limiter.recordFailure("ip1");
        assertTrue(limiter.isAllowed("ip1"), "still under the limit after 2 failures");
        limiter.recordFailure("ip1");

        assertFalse(limiter.isAllowed("ip1"), "blocked once 3 failures are recorded");
    }

    @Test
    @DisplayName("failure counts are tracked independently per key")
    void keysAreIndependent() {
        RoomJoinRateLimiter limiter = new RoomJoinRateLimiter(2, 60_000L);

        limiter.recordFailure("ip1");
        limiter.recordFailure("ip1");

        assertFalse(limiter.isAllowed("ip1"));
        assertTrue(limiter.isAllowed("ip2"), "a different client is unaffected");
    }

    @Test
    @DisplayName("attempts are allowed again once old failures age out of the window")
    void reallowsAfterWindow() throws InterruptedException {
        RoomJoinRateLimiter limiter = new RoomJoinRateLimiter(2, 100L);

        limiter.recordFailure("ip1");
        limiter.recordFailure("ip1");
        assertFalse(limiter.isAllowed("ip1"));

        Thread.sleep(150);

        assertTrue(limiter.isAllowed("ip1"), "the window elapsed, so old failures no longer count");
    }
}
