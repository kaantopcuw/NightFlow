import { useCallback, useEffect, useState } from 'react';
import { toErrorMessage } from '../api/client';

/**
 * Loading / error / success as a discriminated union, so a screen cannot render
 * `data` without having proved it exists.
 */
export type AsyncState<T> =
  | { readonly status: 'loading' }
  | { readonly status: 'error'; readonly message: string }
  | { readonly status: 'success'; readonly data: T };

export type Loader<T> = (signal: AbortSignal) => Promise<T>;

export interface ApiResource<T> {
  readonly state: AsyncState<T>;
  readonly reload: () => void;
}

interface SettledResult<T> {
  readonly loader: Loader<T>;
  readonly nonce: number;
  readonly state: AsyncState<T>;
}

/**
 * Runs `loader` on mount and whenever it changes, aborting the in-flight
 * request on unmount so a late response can never write into an unmounted tree.
 *
 * The settled result is tagged with the request that produced it. "Loading" is
 * therefore derived during render — the state is stale exactly when the tag no
 * longer matches the current request — instead of being pushed with an extra
 * `setState` inside the effect.
 *
 * `loader` must be referentially stable (a module-level function or `useCallback`).
 */
export function useApiResource<T>(loader: Loader<T>): ApiResource<T> {
  const [nonce, setNonce] = useState(0);
  const [settled, setSettled] = useState<SettledResult<T> | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    loader(controller.signal)
      .then((data) => {
        if (!controller.signal.aborted) {
          setSettled({ loader, nonce, state: { status: 'success', data } });
        }
      })
      .catch((error: unknown) => {
        if (!controller.signal.aborted) {
          setSettled({ loader, nonce, state: { status: 'error', message: toErrorMessage(error) } });
        }
      });

    return () => {
      controller.abort();
    };
  }, [loader, nonce]);

  const isCurrent = settled !== null && settled.loader === loader && settled.nonce === nonce;
  const state: AsyncState<T> = isCurrent ? settled.state : { status: 'loading' };

  const reload = useCallback(() => {
    setNonce((current) => current + 1);
  }, []);

  return { state, reload };
}
