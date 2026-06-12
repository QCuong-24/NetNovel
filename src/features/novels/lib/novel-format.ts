export function formatCount(value?: number) {
  return Intl.NumberFormat(undefined, { notation: 'compact' }).format(value ?? 0);
}

export function formatDateTime(value?: string) {
  if (!value) {
    return 'Unknown';
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}
