import { useState } from 'react';
import { FileText } from 'lucide-react';
import api, { downloadCsv, errMsg, usePolling } from '../api';

export default function Reports() {
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [machineId, setMachineId] = useState('');
  const { data: machines } = usePolling(() => api.get('/machines').then((r) => r.data), [], 0);
  const get = async (path, params, name) => { try { await downloadCsv(path, params, name); } catch (e) { alert(errMsg(e)); } };

  return (
    <div className="panel">
      <h3>Reports (CSV export)</h3>
      <div className="filters inline">
        <label>From<input type="date" value={from} onChange={(e) => setFrom(e.target.value)} /></label>
        <label>To<input type="date" value={to} onChange={(e) => setTo(e.target.value)} /></label>
        <label>Machine<select value={machineId} onChange={(e) => setMachineId(e.target.value)}><option value="">All</option>{(machines || []).map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}</select></label>
      </div>
      <div className="report-cards">
        <div className="report"><FileText size={30} /><h4>Inspection History</h4><p>All component inspection records for the selected period / machine.</p>
          <button className="btn-primary" onClick={() => get('/reports/inspections.csv', { from: from || undefined, to: to || undefined, machineId: machineId || undefined }, 'inspections.csv')}>Download CSV</button></div>
        <div className="report"><FileText size={30} /><h4>Alert Log</h4><p>Every alert with severity, status and acknowledgement.</p>
          <button className="btn-primary" onClick={() => get('/reports/alerts.csv', {}, 'alerts.csv')}>Download CSV</button></div>
        <div className="report"><FileText size={30} /><h4>Maintenance Log</h4><p>All maintenance requests with priority and status.</p>
          <button className="btn-primary" onClick={() => get('/reports/maintenance.csv', {}, 'maintenance.csv')}>Download CSV</button></div>
      </div>
    </div>
  );
}
