import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api, { usePolling } from '../api';
import FactoryMap from '../components/FactoryMap.jsx';
import MachineDetails from '../components/MachineDetails.jsx';
import SensorTrends from '../components/SensorTrends.jsx';
import { STATUS, fmtDateTime, num } from '../util';

/** Digital factory view + live equipment monitoring table. */
export default function FactoryView() {
  const [params] = useSearchParams();
  const { data: machines } = usePolling(() => api.get('/machines').then((r) => r.data), [], 10000);
  const list = machines || [];
  const [selectedId, setSelectedId] = useState(params.get('machine') ? Number(params.get('machine')) : null);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => { if (!selectedId && list.length) setSelectedId(list[0].id); }, [list, selectedId]);
  const selected = list.find((m) => m.id === selectedId);
  const rows = list.filter((m) => filter === 'ALL' || m.status === filter);

  return (
    <>
      <div className="grid-2">
        <div className="panel map-panel"><FactoryMap machines={list} selectedId={selectedId} onSelect={setSelectedId} /></div>
        <MachineDetails machine={selected} />
      </div>
      <div className="grid-wide">
        <div className="panel">
          <div className="panel-head">
            <h3>Live Equipment Monitoring</h3>
            <div className="range-tabs">
              {['ALL', 'RUNNING', 'WARNING', 'OFFLINE'].map((s) => (
                <button key={s} className={s === filter ? 'on' : ''} onClick={() => setFilter(s)}>{s === 'ALL' ? 'All' : STATUS[s].label}</button>
              ))}
            </div>
          </div>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Machine</th><th>Line</th><th>Material</th><th>Status</th><th>Temp °C</th><th>Mold °C</th><th>Pressure bar</th><th>Run hrs</th><th>Defect % (7d)</th><th>Alerts</th><th>Updated</th></tr></thead>
              <tbody>
                {rows.map((m) => (
                  <tr key={m.id} className={m.id === selectedId ? 'sel' : ''} onClick={() => setSelectedId(m.id)}>
                    <td><b>{m.name}</b></td><td>{m.line}</td><td>{m.material}</td>
                    <td><span className="pill status" style={{ background: STATUS[m.status].color }}>{STATUS[m.status].label}</span></td>
                    <td>{num(m.temperature)}</td><td>{num(m.moldTemp)}</td><td>{num(m.pressure)}</td><td>{num(m.runningHours)}</td>
                    <td>{num(m.defectRate)}</td><td>{m.openAlerts}</td><td>{fmtDateTime(m.lastUpdated)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        <SensorTrends machineId={selectedId} />
      </div>
    </>
  );
}
