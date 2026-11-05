import { useState } from 'react';
import api, { errMsg, usePolling } from '../api';
import { fmtDateTime } from '../util';
import { alertIcon } from '../components/RecentAlerts.jsx';

export default function Alerts() {
  const [status, setStatus] = useState('');
  const [severity, setSeverity] = useState('');
  const [machineId, setMachineId] = useState('');
  const [page, setPage] = useState(0);
  const size = 15;
  const { data: machines } = usePolling(() => api.get('/machines').then((r) => r.data), [], 0);
  const { data, reload } = usePolling(
    () => api.get('/alerts', { params: { status, severity, machineId: machineId || undefined, page, size } }).then((r) => r.data),
    [status, severity, machineId, page], 10000);
  const act = async (id, what) => {
    try { await api.put(`/alerts/${id}/${what}`); reload(); } catch (e) { alert(errMsg(e)); }
  };
  const pages = data ? Math.max(1, Math.ceil(data.total / size)) : 1;
  const reset = (setter) => (e) => { setter(e.target.value); setPage(0); };

  return (
    <div className="panel">
      <div className="filters inline">
        <label>Status<select value={status} onChange={reset(setStatus)}><option value="">All</option><option>OPEN</option><option>ACKNOWLEDGED</option><option>RESOLVED</option></select></label>
        <label>Severity<select value={severity} onChange={reset(setSeverity)}><option value="">All</option><option>CRITICAL</option><option>WARNING</option><option>INFO</option></select></label>
        <label>Machine<select value={machineId} onChange={reset(setMachineId)}><option value="">All</option>{(machines || []).map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}</select></label>
        <span className="muted">{data?.total ?? 0} alerts</span>
      </div>
      <div className="table-wrap">
        <table>
          <thead><tr><th></th><th>Alert</th><th>Type</th><th>Severity</th><th>Status</th><th>Time</th><th>Acknowledged by</th><th>Actions</th></tr></thead>
          <tbody>
            {(data?.items || []).map((a) => (
              <tr key={a.id}>
                <td>{alertIcon(a)}</td>
                <td><b>{a.title}</b><div className="row-sub">{a.message}</div></td>
                <td>{a.type}</td>
                <td><span className={`pill sev-${a.severity}`}>{a.severity}</span></td>
                <td><span className={`pill st-${a.status}`}>{a.status}</span></td>
                <td>{fmtDateTime(a.createdAt)}</td>
                <td>{a.acknowledgedBy || '--'}</td>
                <td className="actions">
                  {a.status === 'OPEN' && <button className="btn-sm" onClick={() => act(a.id, 'acknowledge')}>Acknowledge</button>}
                  {a.status !== 'RESOLVED' && <button className="btn-sm green" onClick={() => act(a.id, 'resolve')}>Resolve</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="pager">
        <button className="btn-sm" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
        <span>Page {page + 1} / {pages}</span>
        <button className="btn-sm" disabled={page + 1 >= pages} onClick={() => setPage(page + 1)}>Next</button>
      </div>
    </div>
  );
}
