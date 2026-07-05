// @ts-check
const { defineConfig, devices } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  // The backend keeps all device/room state in shared in-memory singletons with no per-test
  // tenant isolation, so concurrent test runs interfere with each other (e.g. broadcasts meant
  // for one test's devices land while another test's room-creation round trip is in flight).
  workers: 1,
  retries: 0,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'retain-on-failure',
    // null lets the page fill the actual (maximized, in --headed) browser window
    // instead of being capped to Playwright's default fixed viewport size.
    viewport: null,
    launchOptions: {
      // SLOWMO=500 npx playwright test --headed to watch actions happen at a human pace.
      ...(parseInt(process.env.SLOWMO, 10) > 0 ? { slowMo: parseInt(process.env.SLOWMO, 10) } : {}),
      args: [
        // Chrome obfuscates host ICE candidates behind a .local mDNS name by default; two
        // browser contexts on the same machine can't resolve each other's, so WebRTC file
        // transfer tests need real local IPs to find a direct candidate pair.
        '--disable-features=WebRtcHideLocalIpsWithMdns',
        '--start-maximized',
      ],
    },
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'], viewport: null } },
  ],
  webServer: {
    command: './mvnw -q -Dcheckstyle.skip=true -Djacoco.skip=true spring-boot:run',
    url: 'http://localhost:8080',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
});
