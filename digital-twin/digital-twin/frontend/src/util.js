export const STATUS = {
  RUNNING: { label: 'Normal', color: '#22c55e' },
  WARNING: { label: 'Warning', color: '#f5a623' },
  OFFLINE: { label: 'Offline', color: '#ef4444' },
};

export const fmtTime = (iso) =>
  iso ? new Date(iso).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }) : '--';

export const fmtDateTime = (iso) =>
  iso
    ? new Date(iso).toLocaleString('en-GB', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })
    : '--';

export const num = (v, d = 1) => (v === null || v === undefined ? '--' : Number(v).toFixed(d));
