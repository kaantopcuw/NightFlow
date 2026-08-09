import type { ReactNode } from 'react';
import type { AsyncState } from '../hooks/useApiResource';

interface AsyncBoundaryProps<T> {
  readonly state: AsyncState<T>;
  readonly onRetry: () => void;
  /** Lets each screen decide what "no rows" means for its own payload. */
  readonly isEmpty: (data: T) => boolean;
  readonly emptyMessage: string;
  readonly children: (data: T) => ReactNode;
}

/**
 * Renders the loading, error and empty states for a resource, and hands the
 * loaded value to `children` only in the success case.
 */
export function AsyncBoundary<T>({
  state,
  onRetry,
  isEmpty,
  emptyMessage,
  children,
}: AsyncBoundaryProps<T>) {
  switch (state.status) {
    case 'loading':
      return (
        <p className="state state--loading" role="status">
          Loading…
        </p>
      );

    case 'error':
      return (
        <div className="state state--error" role="alert">
          <p>{state.message}</p>
          <button type="button" className="button" onClick={onRetry}>
            Try again
          </button>
        </div>
      );

    case 'success':
      return isEmpty(state.data) ? (
        <p className="state state--empty">{emptyMessage}</p>
      ) : (
        <>{children(state.data)}</>
      );
  }
}
