import { listMyTickets } from '../api/endpoints';
import type { TicketResponse } from '../api/types';
import { AsyncBoundary } from '../components/AsyncBoundary';
import { useApiResource } from '../hooks/useApiResource';
import { formatDateTime, formatEnum, formatText } from '../lib/format';

function statusClass(status: TicketResponse['status']): string {
  switch (status) {
    case 'SOLD':
      return 'badge badge--ok';
    case 'USED':
      return 'badge badge--warn';
    case 'CANCELLED':
      return 'badge badge--danger';
    default:
      return 'badge';
  }
}

function isEmptyList(tickets: TicketResponse[]): boolean {
  return tickets.length === 0;
}

export function TicketsPage() {
  const { state, reload } = useApiResource(listMyTickets);

  return (
    <section className="page">
      <header className="page__header">
        <h1>My tickets</h1>
        <p className="page__source">
          <code>GET /api/tickets/my-tickets</code> · ticket-service — the tickets
          of the signed-in user
        </p>
      </header>

      <AsyncBoundary
        state={state}
        onRetry={reload}
        isEmpty={isEmptyList}
        emptyMessage="This account holds no tickets."
      >
        {(tickets) => (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th scope="col">Ticket code</th>
                  <th scope="col">Category</th>
                  <th scope="col">Event</th>
                  <th scope="col">Seat</th>
                  <th scope="col">Sold at</th>
                  <th scope="col">Status</th>
                </tr>
              </thead>
              <tbody>
                {tickets.map((ticket) => (
                  <tr key={ticket.id}>
                    <td className="cell-title">{ticket.ticketCode}</td>
                    <td>{formatText(ticket.categoryName)}</td>
                    <td className="cell-sub">{formatText(ticket.eventId)}</td>
                    <td>{formatText(ticket.seatInfo)}</td>
                    <td>{formatDateTime(ticket.soldAt)}</td>
                    <td>
                      <span className={statusClass(ticket.status)}>
                        {formatEnum(ticket.status)}
                      </span>
                    </td>
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
