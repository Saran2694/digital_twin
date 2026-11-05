import { useNavigate } from 'react-router-dom';
import { AlertTriangle, CheckCircle2, AlertCircle, Thermometer, Activity, Clock, RefreshCw, ArrowRight } from 'lucide-react';
import { STATUS, fmtTime, num } from '../util';

export default function MachineDetails({ machine }) {
  const navigate = useNavigate();
  if (!machine) return <div className="panel"><h3>Machine Details</h3><p className="muted">No machine selected</p></div>;
  const st = STATUS[machine.status] || STATUS.OFFLINE;
  const Icon = machine.status === 'RUNNING' ? CheckCircle2 : machine.status === 'WARNING' ? AlertTriangle : AlertCircle;
  const hot = machine.status !== 'RUNNING';
  return (
    <div className="panel details">
      <h3>Machine Details</h3>
      <div className="details-top">
        <img src="/assets/machine.png" alt={machine.name} />
        <div>
          <div className="m-name">{machine.name}</div>
          <div className="m-type">{machine.machineType}</div>
          <span className="pill status" style={{ background: st.color }}><Icon size={15} /> {machine.status === 'RUNNING' ? 'Normal' : st.label}</span>
        </div>
      </div>
      <div className="kv"><span><Thermometer size={18} /> Temperature</span><b style={{ color: hot ? '#ef4444' : undefined }}>{num(machine.temperature)} °C</b></div>
      <div className="kv"><span><Activity size={18} /> Pressure</span><b>{num(machine.pressure)} bar</b></div>
      <div className="kv"><span><Clock size={18} /> Running Hours</span><b>{num(machine.runningHours)} hrs</b></div>
      <div className="kv small"><span><RefreshCw size={16} /> Last Updated</span><b>{fmtTime(machine.lastUpdated)}</b></div>
      <p className="reason">{machine.statusReason}</p>
      <button className="btn-outline" onClick={() => navigate(`/factory?machine=${machine.id}`)}>
        View Details <ArrowRight size={16} />
      </button>
    </div>
  );
}
