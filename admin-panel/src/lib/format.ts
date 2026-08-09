const DATE_TIME_FORMAT = new Intl.DateTimeFormat('en-GB', {
  dateStyle: 'medium',
  timeStyle: 'short',
});

const PLACEHOLDER = '—';

/**
 * Jackson serialises `LocalDateTime` without a zone offset, so the value is
 * wall-clock time at the venue. It is rendered as-is rather than converted to
 * the viewer's zone, which would silently shift event times.
 */
export function formatDateTime(value: string | null): string {
  if (value === null || value.length === 0) {
    return PLACEHOLDER;
  }
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return DATE_TIME_FORMAT.format(parsed);
}

export function formatText(value: string | null | undefined): string {
  return value === null || value === undefined || value.length === 0 ? PLACEHOLDER : value;
}

export function formatNumber(value: number | null): string {
  return value === null ? PLACEHOLDER : value.toLocaleString('en-GB');
}

/** Event prices come from event-catalog-service as a min/max range. */
export function formatPriceRange(min: number | null, max: number | null): string {
  if (min === null && max === null) {
    return PLACEHOLDER;
  }
  if (min !== null && max !== null && min !== max) {
    return `${formatPrice(min)} – ${formatPrice(max)}`;
  }
  return formatPrice(min ?? max ?? 0);
}

function formatPrice(value: number): string {
  return `${value.toLocaleString('en-GB', { minimumFractionDigits: 0, maximumFractionDigits: 2 })} ₺`;
}

/** Turns backend enum constants such as `CONCERT_HALL` into `Concert hall`. */
export function formatEnum(value: string | null): string {
  if (value === null || value.length === 0) {
    return PLACEHOLDER;
  }
  const words = value.toLowerCase().split('_');
  const [first, ...rest] = words;
  if (first === undefined) {
    return PLACEHOLDER;
  }
  return [first.charAt(0).toUpperCase() + first.slice(1), ...rest].join(' ');
}
