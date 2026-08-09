import { listVenues } from '../api/endpoints';
import type { VenueResponse } from '../api/types';
import { AsyncBoundary } from '../components/AsyncBoundary';
import { useApiResource } from '../hooks/useApiResource';
import { formatEnum, formatNumber, formatText } from '../lib/format';

function isEmptyList(venues: VenueResponse[]): boolean {
  return venues.length === 0;
}

export function VenuesPage() {
  const { state, reload } = useApiResource(listVenues);

  return (
    <section className="page">
      <header className="page__header">
        <h1>Venues</h1>
        <p className="page__source">
          <code>GET /api/venues</code> · venue-service
        </p>
      </header>

      <AsyncBoundary
        state={state}
        onRetry={reload}
        isEmpty={isEmptyList}
        emptyMessage="No venues are registered yet."
      >
        {(venues) => (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th scope="col">Venue</th>
                  <th scope="col">Type</th>
                  <th scope="col">City</th>
                  <th scope="col">District</th>
                  <th scope="col">Capacity</th>
                  <th scope="col">Address</th>
                </tr>
              </thead>
              <tbody>
                {venues.map((venue) => (
                  <tr key={venue.id}>
                    <td className="cell-title">{venue.name}</td>
                    <td>
                      <span className="badge">{formatEnum(venue.type)}</span>
                    </td>
                    <td>{formatText(venue.city)}</td>
                    <td>{formatText(venue.district)}</td>
                    <td>{formatNumber(venue.capacity)}</td>
                    <td className="cell-sub">{formatText(venue.address)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </AsyncBoundary>
    </section>
  );
}
