import { setupServer } from 'msw/node';
import { handlers } from '../mocks/handlers';

/**
 * The tests run against the same handlers the offline demo uses, so a fixture
 * that stops matching the DTO types breaks the suite too.
 */
export const server = setupServer(...handlers);
