import { useState } from 'react';
import api, { errMsg, usePolling } from '../api';
import { useAuth } from '../auth.jsx';
import { fmtDateTime } from '../util';
import { STATUS_LABEL } from '../components/MaintenancePanel.jsx';

const EMPTY = { machineId: '', title: '', description: '', priority: 'MEDIUM', assignedTo: '', dueDate: '' };

export default function Maintenance() {
  const { isAdmin } = useAuth();
  const [status, setStatus] = useState('');
  const [form, setForm] = useState(null);
  const { data: machines } = usePolling(() => api.get('/machines').then((r) => r.data), [], 0);
  const { data, reload } = usePolling(() => api.get('/maintenance', { params: { status } }).then((r) => r.data), [status], 15000);

  const save = async (e) => {
    e.preventDefault();
    const body = { ...form, machineId: Number(form.machineId), dueDate: form.dueDate || null };
    try {
      if (form.id) await api.put(`/maintenance/${form.id}`, body); else await api.post('/maintenance', body);
      setForm(null);
      reload();
    } catch (err) { alert(errMsg(err)); }
  };
  const setSt = async (id, s) => { try { await api.put(`/maintenance/${id}/status`, null, { params: { status: s } }); reload(); } catch (e) { alert(errMsg(e)); } };
  const del = async (id) => { if (window.confirm('Delete this request?')) { await api.delete(`/maintenance/${id}`); reload(); } };
  const f = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <div className="panel">
      <div className="filters inline">
        <label>Status<select value={status} onChange={(e) => setStatus(e.target.value)}><option value="">All</option><option value="PENDING">Pending</option><option value="IN_PROGRESS">In Progress</option><option value="COMPLETED">Completed</option></select></label>
        <button className="btn-primary" onClick={() => setForm({ ...EMPTY, machineId: machines?.[0]?.id || '' })}>+ New Request</button>
      </div>
      <div className="table-wrap">
        <table>
          <thead><tr><th>#</th><th>Request</th><th>Machine</th><th>Priority</th><th>Status</th><th>Requested by</th><th>Assigned to</th><th>Created</th><th>Due</th><th>Actions</th></tr></thead>
          <tbody>
            {(data || []).map((m) => (
              <tr key={m.id}>
                <td>{m.id}</td>
                <td><b>{m.title}</b><div className="row-sub">{m.description}</div></td>
                <td>{m.machineName}</td>
                <td><span className={`pill pr-${m.priority}`}>{m.priority}</span></td>
                <td>
                  <select className={`pill ${m.status}`} value={m.status} onChange={(e) => setSt(m.id, e.target.value)}>
                    {Object.entries(STATUS_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                  </select>
                </td>
                <td>{m.requestedBy}</td><td>{m.assignedTo || '--'}</td><td>{fmtDateTime(m.createdAt)}</td><td>{m.dueDate || '--'}</td>
                <td className="actions">
                  <button className="btn-sm" onClick={() => setForm({ ...m, dueDate: m.dueDate || '', assignedTo: m.assignedTo || '', description: m.description || '' })}>Edit</button>
                  {isAdmin && <button className="btn-sm red" onClick={() => del(m.id)}>Delete</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {form && (
        <div className="modal-bg" onClick={() => setForm(null)}>
          <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={save}>
            <h3>{form.id ? 'Edit' : 'New'} Maintenance Request</h3>
            <label>Machine<select value={form.machineId} onChange={f('machineId')} required>{(machines || []).map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}</select></label>
            <label>Title<input value={form.title} onChange={f('title')} required /></label>
            <label>Description<textarea rows={3} value={form.description} onChange={f('description')} /></label>
            <label>Priority<select value={form.priority} onChange={f('priority')}><option>LOW</option><option>MEDIUM</option><option>HIGH</option></select></label>
            <label>Assigned to<input value={form.assignedTo} onChange={f('assignedTo')} /></label>
            <label>Due date<input type="date" value={form.dueDate} onChange={f('dueDate')} /></label>
            <div className="modal-actions"><button type="button" className="btn-sm" onClick={() => setForm(null)}>Cancel</button><button className="btn-primary">Save</button></div>
          </form>
        </div>
      )}
    </div>
  );
}
