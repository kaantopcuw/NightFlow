/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Prefix put in front of every API path. Empty keeps requests same-origin. */
  readonly VITE_API_BASE_URL?: string;
  /** Target of the `/api` dev-server proxy (gateway-service). */
  readonly VITE_GATEWAY_URL?: string;
  /** `"true"` serves mock data through MSW instead of calling the gateway. */
  readonly VITE_ENABLE_MOCKS?: string;
  /** Account the live integration suite (`npm run test:live`) signs in with. */
  readonly VITE_LIVE_EMAIL?: string;
  /** Password for `VITE_LIVE_EMAIL`. Test account only — never a real secret. */
  readonly VITE_LIVE_PASSWORD?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
