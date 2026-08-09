import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterAll, afterEach, beforeAll } from 'vitest';
import { setAuthToken, setUnauthorizedHandler } from '../api/client';
import { server } from './server';

// `onUnhandledRequest: 'error'` makes any call to an endpoint the panel is not
// supposed to touch fail the test instead of silently hitting the network.
beforeAll(() => {
  server.listen({ onUnhandledRequest: 'error' });
});

afterEach(() => {
  cleanup();
  server.resetHandlers();
  setAuthToken(null);
  setUnauthorizedHandler(null);
  window.sessionStorage.clear();
});

afterAll(() => {
  server.close();
});
