// @ts-check
const { test, expect } = require('@playwright/test');
const fs = require('fs');
const os = require('os');
const path = require('path');

// Big enough that the transfer stays visible for a few seconds instead of
// completing in a single instant data-channel message.
const LARGE_FILE_SIZE_BYTES = 20 * 1024 * 1024;

test.describe('File transfer', () => {
  test('sending a file completes and shows a direct (non-relay) connection', async ({ browser }) => {
    const largeFilePath = path.join(os.tmpdir(), 'mairdrop-e2e-large-sample.bin');
    fs.writeFileSync(largeFilePath, Buffer.alloc(LARGE_FILE_SIZE_BYTES, 'm'));

    const contextA = await browser.newContext();
    const contextB = await browser.newContext();
    const pageA = await contextA.newPage();
    const pageB = await contextB.newPage();

    try {
      await pageA.goto('/');
      await pageB.goto('/');

      const nameB = await pageB.locator('#deviceIdSpan').textContent();
      const deviceIdB = await pageB.evaluate(() => sessionStorage.getItem('deviceId'));
      await expect(pageA.locator('#deviceList')).toContainText(nameB);

      await pageA.setInputFiles('#fileInput', largeFilePath);
      await pageA.getByRole('button', { name: '📤 Send' }).click();

      await pageB.getByRole('button', { name: '✓ Accept' }).click();

      // Check relay status while the transfer is in progress (after ICE connects, before the
      // DOM is rebuilt on completion). On localhost WebRTC must always find a direct host
      // candidate — relay fallback indicates a misconfigured ICE server list.
      await expect(pageA.locator('#progress-fill-' + deviceIdB)).not.toHaveClass(/via-relay/, { timeout: 15000 });

      await expect(pageA.locator('#alertContainer')).toContainText('All files sent', { timeout: 30000 });
    } finally {
      await contextA.close();
      await contextB.close();
      fs.rmSync(largeFilePath, { force: true });
    }
  });
});
