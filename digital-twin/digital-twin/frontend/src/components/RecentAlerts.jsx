import { Link } from 'react-router-dom';
import { AlertCircle, AlertTriangle, Info, ArrowRight } from 'lucide-react';
import { fmtTime } from '../util';

export const alertIcon = (a) => {
  if (a.status === 'RESOLVED') return <Info size={28} color="#2f8cff" />;
  return a.severity === 'CRITICAL' ? <AlertCircle size={28} color="#ef4444" /> : <AlertTriangle size={28} color="#f5a623" />;
};

export default function RecentAlerts({ alerts = [] }) {
  return (
    <div className="panel">
      <div className="panel-head">
        <h3>Recent Alerts</h3>
        <Link to="/alerts" className="view-all">View All <ArrowRight size={14} /></Link>
      </div>
      {alerts.map((a) => (
        <div className="list-row" key={a.id}>
          <div className="row-icon">{alertIcon(a)}</div>
          <div className="row-body">
            <div className="row-title">{a.title}</div>
            <div className="row-sub">{a.message}</div>
          </div>
          <div className="row-time">{fmtTime(a.createdAt)}</div>
        </div>
      ))}
      {alerts.length === 0 && <p className="muted">No alerts</p>}
    </div>
  );
}
