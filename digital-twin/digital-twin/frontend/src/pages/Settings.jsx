import { useState } from 'react';
import api, { errMsg, usePolling } from '../api';
import { useAuth } from '../auth.jsx';

/** Admin dashboard: system overview, Excel import, user and machine management. */
export default function Settings() {
  const { isAdmin } = useAuth();
  const [tab, setTab] = useState('overview');
  if (!isAdmin) return <div className="panel"><h3>Admin Dashboard</h3><p className="muted">Only administrators can open this page.</p></div>;
  return (
    <div className="panel">
      <div className="range-tabs big">
        {[['overview', 'Overview & Import'], ['users', 'Users'], ['machines', 'Machines']].map(([k, v]) => (
          <button key={k} className={tab === k ? 'on' : ''} onClick={() => setTab(k)}>{v}</button>
        ))}
      </div>
      {tab === 'overview' && <Overview />}
      {tab === 'users' && <Users />}
      {tab === 'machines' && <Machines />}
    </div>
  );
}

function Overview() {
  const { data: stats, reload } = usePolling(() => api.get('/admin/stats').then((r) => r.data), [], 0);
  const [msg, setMsg] = useState('');
  const upload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const fd = new FormData();
    fd.append('file', file);
    setMsg('Importing…');
    try {
      const { data } = await api.post('/admin/import', fd);
      setMsg(`Imported ${data.rows} rows, ${data.machines} machines, ${data.alertsCreated} new alerts.`);
      reload();
    } catch (err) { setMsg(errMsg(err)); }
    e.target.value = '';
  };
  const regen = async () => {
    try { const { data } = await api.post('/admin/regenerate'); setMsg(`New alerts: ${data.alertsCreated}, new maintenance requests: ${data.maintenanceCreated}`); reload(); }
    catch (err) { setMsg(errMsg(err)); }
  };
  return (
    <>
      <div className="kpi-row">
        {stats && Object.entries(stats).map(([k, v]) => <div className="panel kpi" key={k}><span>{k}</span><b>{v}</b></div>)}
      </div>
      <h4>Import database (.xlsx)</h4>
      <p className="muted">Columns required: Component_ID, Inspection_Date, Shift, Line, Material, Part_Type, Batch_ID, Pour_Temp_C, Mold_Temp_C, Pressure_bar, Cycle_Time_s, Weight_g, Quality, Defect_Type, Severity, Disposition. Existing Component_IDs are updated.</p>
      <input type="file" accept=".xlsx" onChange={upload} />
      <div style={{ marginTop: 14 }}><button className="btn-sm" onClick={regen}>Re-run alert &amp; maintenance rules</button></div>
      {msg && <p className="ok">{msg}</p>}
    </>
  );
}

function Users() {
  const { data, reload } = usePolling(() => api.get('/admin/users').then((r) => r.data), [], 0);
  const [form, setForm] = useState(null);
  const f = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const save = async (e) => {
    e.preventDefault();
    try {
      if (form.id) await api.put(`/admin/users/${form.id}`, form); else await api.post('/admin/users', form);
      setForm(null); reload();
    } catch (err) { alert(errMsg(err)); }
  };
  const toggle = async (u) => { await api.put(`/admin/users/${u.id}`, { enabled: !u.enabled }); reload(); };
  const del = async (u) => { if (window.confirm(`Delete ${u.username}?`)) { try { await api.delete(`/admin/users/${u.id}`); reload(); } catch (e) { alert(errMsg(e)); } } };
  return (
    <>
      <div className="filters inline"><button className="btn-primary" onClick={() => setForm({ username: '', password: '', fullName: '', role: 'OPERATOR' })}>+ New User</button></div>
      <div className="table-wrap"><table>
        <thead><tr><th>Username</th><th>Full name</th><th>Role</th><th>Enabled</th><th>Actions</th></tr></thead>
        <tbody>{(data || []).map((u) => (
          <tr key={u.id}><td>{u.username}</td><td>{u.fullName}</td><td>{u.role}</td><td>{u.enabled ? 'Yes' : 'No'}</td>
            <td className="actions">
              <button className="btn-sm" onClick={() => setForm({ ...u, password: '' })}>Edit</button>
              <button className="btn-sm" onClick={() => toggle(u)}>{u.enabled ? 'Disable' : 'Enable'}</button>
              <button className="btn-sm red" onClick={() => del(u)}>Delete</button>
            </td></tr>))}</tbody>
      </table></div>
      {form && (
        <div className="modal-bg" onClick={() => setForm(null)}>
          <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={save}>
            <h3>{form.id ? 'Edit user' : 'New user'}</h3>
            <label>Username<input value={form.username} onChange={f('username')} disabled={!!form.id} required /></label>
            <label>Password {form.id && '(leave empty to keep)'}<input type="password" value={form.password} onChange={f('password')} {...(form.id ? {} : { required: true, minLength: 6 })} /></label>
            <label>Full name<input value={form.fullName || ''} onChange={f('fullName')} /></label>
            <label>Role<select value={form.role} onChange={f('role')}><option>ADMIN</option><option>ENGINEER</option><option>OPERATOR</option></select></label>
            <div className="modal-actions"><button type="button" className="btn-sm" onClick={() => setForm(null)}>Cancel</button><button className="btn-primary">Save</button></div>
          </form>
        </div>)}
    </>
  );
}

function Machines() {
  const { data, reload } = usePolling(() => api.get('/machines').then((r) => r.data), [], 0);
  const [edit, setEdit] = useState({});
  const val = (m, k) => (edit[m.id]?.[k] ?? m[k] ?? '');
  const set = (m, k, v) => setEdit({ ...edit, [m.id]: { ...edit[m.id], [k]: v } });
  const save = async (m) => {
    const e = edit[m.id];
    if (!e) return;
    try {
      await api.put(`/admin/machines/${m.id}`, { ...e, posX: e.posX !== undefined ? Number(e.posX) : undefined, posY: e.posY !== undefined ? Number(e.posY) : undefined });
      setEdit({ ...edit, [m.id]: undefined }); reload();
    } catch (err) { alert(errMsg(err)); }
  };
  return (
    <div className="table-wrap"><table>
      <thead><tr><th>Code</th><th>Line / Material</th><th>Name</th><th>Type</th><th>X %</th><th>Y %</th><th></th></tr></thead>
      <tbody>{(data || []).map((m) => (
        <tr key={m.id}><td>{m.code}</td><td>{m.line} / {m.material}</td>
          <td><input value={val(m, 'name')} onChange={(e) => set(m, 'name', e.target.value)} /></td>
          <td><input value={val(m, 'machineType')} onChange={(e) => set(m, 'machineType', e.target.value)} /></td>
          <td><input className="narrow" value={val(m, 'posX')} onChange={(e) => set(m, 'posX', e.target.value)} /></td>
          <td><input className="narrow" value={val(m, 'posY')} onChange={(e) => set(m, 'posY', e.target.value)} /></td>
          <td><button className="btn-sm" disabled={!edit[m.id]} onClick={() => save(m)}>Save</button></td></tr>))}</tbody>
    </table></div>
  );
}
