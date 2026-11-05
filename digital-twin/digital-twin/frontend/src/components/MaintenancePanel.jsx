import { Link } from 'react-router-dom';
import { Wrench, Calendar, CheckCircle2, ArrowRight } from 'lucide-react';

export const STATUS_LABEL = { PENDING: 'Pending', IN_PROGRESS: 'In Progress', COMPLETED: 'Completed' };
const ICON = { PENDING: <Wrench size={26} />, IN_PROGRESS: <Calendar size={26} />, COMPLETED: <CheckCircle2 size={26} /> };

export default function MaintenancePanel({ items = [] }) {
  return (
    <div className="panel">
      <div className="panel-head">
        <h3>Maintenance Requests</h3>
        <Link to="/maintenance" className="view-all">View All <ArrowRight size={14} /></Link>
      </div>
      {items.map((m) => (
        <div className="list-row" key={m.id}>
          <div className={`row-icon m-${m.status}`}>{ICON[m.status]}</div>
          <div className="row-body">
            <div className="row-title">{m.title}</div>
            <div className="row-sub">{m.machineName}</div>
          </div>
          <span className={`pill ${m.status}`}>{STATUS_LABEL[m.status]}</span>
        </div>
      ))}
      {items.length === 0 && <p className="muted">No requests</p>}
    </div>
  );
}
