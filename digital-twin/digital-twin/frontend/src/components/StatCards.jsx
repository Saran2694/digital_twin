import { Building2, Play, AlertTriangle, AlertCircle, Wrench } from 'lucide-react';

export default function StatCards({ summary }) {
  const s = summary || {};
  const cards = [
    { label: 'Total Machines', value: s.total, icon: Building2, cls: 'blue' },
    { label: 'Running', value: s.running, icon: Play, cls: 'green' },
    { label: 'Warning', value: s.warning, icon: AlertTriangle, cls: 'amber' },
    { label: 'Offline', value: s.offline, icon: AlertCircle, cls: 'red' },
    { label: 'Active Alerts', value: s.activeAlerts, icon: Wrench, cls: 'purple' },
  ];
  return (
    <div className="stat-row">
      {cards.map(({ label, value, icon: Icon, cls }) => (
        <div key={label} className={`stat-card ${cls}`}>
          <div className="stat-icon"><Icon size={34} /></div>
          <div>
            <div className="stat-label">{label}</div>
            <div className="stat-value">{value ?? '-'}</div>
          </div>
        </div>
      ))}
    </div>
  );
}
