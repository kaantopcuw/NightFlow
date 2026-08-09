import react from '@vitejs/plugin-react';
import { loadEnv } from 'vite';
// `vitest/config` re-exports Vite's `defineConfig` with the `test` block typed.
import { configDefaults, defineConfig } from 'vitest/config';

/** Matches the live integration suite, which needs a running backend. */
const LIVE_TESTS = '**/*.live.test.{ts,tsx}';

/**
 * The NightFlow gateway does not send CORS headers, so the browser cannot call
 * it directly from the Vite dev origin. During development every `/api/**`
 * request is proxied through the dev server instead, which keeps the browser on
 * a single origin and makes CORS irrelevant.
 *
 * `--mode live` switches the Vitest block over to the live integration suite:
 * MSW is not installed, the client is pointed at a real gateway, and only
 * `*.live.test.ts` files run. See `npm run test:live` and README.md.
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const gatewayUrl = env['VITE_GATEWAY_URL'] ?? 'http://localhost:8080';
  const isLive = mode === 'live';

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: gatewayUrl,
          changeOrigin: true,
        },
      },
    },
    test: {
      environment: 'jsdom',
      globals: false,
      css: false,
      // The default suite installs MSW; the live suite must not, or it would
      // never reach the gateway it is meant to prove.
      setupFiles: isLive ? [] : ['./src/test/setup.ts'],
      include: isLive ? [`src/${LIVE_TESTS}`] : configDefaults.include,
      exclude: isLive ? configDefaults.exclude : [...configDefaults.exclude, LIVE_TESTS],
      // A real gateway plus a cold JVM route is far slower than an MSW handler.
      testTimeout: isLive ? 30_000 : 5_000,
      // Tests run in Node, where `fetch` cannot resolve origin-relative URLs.
      // Giving the API client an absolute base keeps the request URLs identical
      // to what the MSW handlers register — and, in live mode, points the very
      // same client at the real gateway.
      env: isLive
        ? {
            VITE_API_BASE_URL: gatewayUrl,
            VITE_LIVE_EMAIL: env['VITE_LIVE_EMAIL'] ?? 'reviewbot@example.com',
            VITE_LIVE_PASSWORD: env['VITE_LIVE_PASSWORD'] ?? 'test1234',
          }
        : {
            VITE_API_BASE_URL: 'http://localhost:8080',
          },
    },
  };
});
