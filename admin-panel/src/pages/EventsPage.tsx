import { useCallback, useState } from 'react';
import { listEvents } from '../api/endpoints';
import type { EventResponse, PageResult } from '../api/types';
import { AsyncBoundary } from '../components/AsyncBoundary';
import { useApiResource } from '../hooks/useApiResource';
import { formatDateTime, formatEnum, formatPriceRange, formatText } from '../lib/format';

function statusClass(status: EventResponse['status']): string {
  switch (status) {
    case 'PUBLISHED':
      return 'badge badge--ok';
    case 'CANCELLED':
      return 'badge badge--danger';
    case 'SOLD_OUT':
      return 'badge badge--warn';
    default:
      return 'badge';
  }
}

function isEmptyPage(page: PageResult<EventResponse>): boolean {
  return page.content.length === 0;
}

export function EventsPage() {
  const [page, setPage] = useState(0);

  const loader = useCallback(
    (signal: AbortSignal) => listEvents(page, signal),
    [page],
  );
  const { state, reload } = useApiResource(loader);

  return (
    <section className="page">
      <header className="page__header">
        <h1>Events</h1>
        <p className="page__source">
          <code>GET /api/events</code> · event-catalog-service
        </p>
      </header>

      <AsyncBoundary
        state={state}
        onRetry={reload}
        isEmpty={isEmptyPage}
        emptyMessage="No events have been published yet."
      >
        {(result) => (
          <>
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th scope="col">Event</th>
                    <th scope="col">Date</th>
                    <th scope="col">Venue</th>
                    <th scope="col">Category</th>
                    <th scope="col">Price range</th>
                    <th scope="col">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {result.content.map((event) => (
                    <tr key={event.id}>
                      <td>
                        <span className="cell-title">{event.name}</span>
                        <span className="cell-sub">{event.slug}</span>
                      </td>
                      <td>{formatDateTime(event.eventDate)}</td>
                      <td>
                        <span className="cell-title">{formatText(event.venueName)}</span>
                        <span className="cell-sub">{formatText(event.venueCity)}</span>
                      </td>
                      <td>{formatEnum(event.category)}</td>
                      <td>{formatPriceRange(event.minPrice, event.maxPrice)}</td>
                      <td>
                        <span className={statusClass(event.status)}>
                          {formatEnum(event.status)}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <nav className="pager" aria-label="Event pages">
              <button
                type="button"
                className="button"
                disabled={result.number <= 0}
                onClick={() => {
                  setPage((current) => Math.max(0, current - 1));
                }}
              >
                Previous
              </button>
              <span className="pager__status">
                Page {result.number + 1} of {Math.max(1, result.totalPages)} ·{' '}
                {result.totalElements} event(s)
              </span>
              <button
                type="button"
                className="button"
                disabled={result.number + 1 >= result.totalPages}
                onClick={() => {
                  setPage((current) => current + 1);
                }}
              >
                Next
              </button>
            </nav>
          </>
        )}
      </AsyncBoundary>
    </section>
  );
}
