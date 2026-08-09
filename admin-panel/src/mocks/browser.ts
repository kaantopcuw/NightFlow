import { setupWorker } from 'msw/browser';
import { handlers } from './handlers';

/** Service-worker gateway used when `VITE_ENABLE_MOCKS=true`. */
export const worker = setupWorker(...handlers);
