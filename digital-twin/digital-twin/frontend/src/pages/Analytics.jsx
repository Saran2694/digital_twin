import { useState } from 'react';
import { Bar, BarChart, CartesianGrid, Cell, ComposedChart, Legend, Line, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import api, { usePolling } from '../api';

const COLORS = ['#2f8cff', '#22c55e', '#f5a623', '#ef4444', '#a855f7', '#14b8a6', '#eab308', '#ec4899', '#64748b', '#f97316'];
const tip = { contentStyle: { background: '#0b1830', border: '1px solid #26395e' } };

export default function Analytics() {
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [machineId, setMachineId] = useState('');
  const [gran, setGran] = useState('week');
  const q = { from: from || undefined, to: to || undefined, machineId: machineId || undefined };
  const deps = [from, to, machineId, gran];

  const { data: machines } = usePolling(() => api.get('/machines').then((r) => r.data), [], 0);
  const { data: ov } = usePolling(() => api.get('/analytics/overview', { params: q }).then((r) => r.data), deps, 0);
  const { data: trend } = usePolling(() => api.get('/analytics/trend', { params: { ...q, granularity: gran } }).then((r) => r.data), deps, 0);
  const grp = (by) => () => api.get('/analytics/group', { params: { ...q, by } }).then((r) => r.data);
  const { data: defects } = usePolling(grp('defectType'), deps, 0);
  const { data: lines } = usePolling(grp('line'), deps, 0);
  const { data: materials } = usePolling(grp('material'), deps, 0);
  const { data: shifts } = usePolling(grp('shift'), deps, 0);
  const { data: severity } = usePolling(grp('severity'), deps, 0);
  const { data: disp } = usePolling(grp('disposition'), deps, 0);

  const rate = (rows) => (rows || []).map((r) => ({ ...r, rate: r.total ? +((r.damaged * 100) / r.total).toFixed(1) : 0 }));
  const kpis = ov && [
    ['Components Inspected', ov.total], ['Yield %', ov.yieldPct], ['Damaged', ov.damaged], ['Scrap', ov.scrap],
    ['Rework', ov.rework], ['Avg Cycle (s)', ov.avgCycle], ['Avg Pour Temp °C', ov.avgPourTemp], ['Avg Pressure bar', ov.avgPressure],
  ];

  return (
    <>
      <div className="panel filters">
        <label>From<input type="date" value={from} onChange={(e) => setFrom(e.target.value)} /></label>
        <label>To<input type="date" value={to} onChange={(e) => setTo(e.target.value)} /></label>
        <label>Machine
          <select value={machineId} onChange={(e) => setMachineId(e.target.value)}>
            <option value="">All machines</option>
            {(machines || []).map((m) => <option key={m.id} value={m.id}>{m.name} ({m.line} / {m.material})</option>)}
          </select>
        </label>
        <label>Group by
          <select value={gran} onChange={(e) => setGran(e.target.value)}>
            <option value="day">Day</option><option value="week">Week</option><option value="month">Month</option>
          </select>
        </label>
      </div>

      <div className="kpi-row">
        {(kpis || []).map(([k, v]) => <div className="panel kpi" key={k}><span>{k}</span><b>{v}</b></div>)}
      </div>

      <div className="grid-2e">
        <div className="panel"><h3>Production vs Damaged ({gran})</h3>
          <ResponsiveContainer width="100%" height={260}>
            <ComposedChart data={trend || []}>
              <CartesianGrid stroke="#1b2c4d" vertical={false} />
              <XAxis dataKey="period" stroke="#8da2c4" tick={{ fontSize: 11 }} /><YAxis stroke="#8da2c4" /><Tooltip {...tip} /><Legend />
              <Bar dataKey="total" name="Inspected" fill="#2f8cff" /><Bar dataKey="damaged" name="Damaged" fill="#ef4444" />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
        <div className="panel"><h3>Pour Temperature & Pressure ({gran})</h3>
          <ResponsiveContainer width="100%" height={260}>
            <ComposedChart data={trend || []}>
              <CartesianGrid stroke="#1b2c4d" vertical={false} />
              <XAxis dataKey="period" stroke="#8da2c4" tick={{ fontSize: 11 }} />
              <YAxis yAxisId="t" stroke="#8da2c4" domain={['auto', 'auto']} /><YAxis yAxisId="p" orientation="right" stroke="#8da2c4" domain={['auto', 'auto']} />
              <Tooltip {...tip} /><Legend />
              <Line yAxisId="t" dataKey="avgPourTemp" name="Pour temp °C" stroke="#2f8cff" dot={false} strokeWidth={2} />
              <Line yAxisId="p" dataKey="avgPressure" name="Pressure bar" stroke="#f59e0b" dot={false} strokeWidth={2} />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
        <div className="panel"><h3>Defects by Type</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={defects || []} layout="vertical" margin={{ left: 40 }}>
              <CartesianGrid stroke="#1b2c4d" horizontal={false} />
              <XAxis type="number" stroke="#8da2c4" /><YAxis type="category" dataKey="name" stroke="#8da2c4" width={110} tick={{ fontSize: 11 }} />
              <Tooltip {...tip} /><Bar dataKey="total" name="Count" fill="#f5a623" />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="panel"><h3>Defect Rate % by Line / Material</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={[...rate(lines), ...rate(materials)]}>
              <CartesianGrid stroke="#1b2c4d" vertical={false} />
              <XAxis dataKey="name" stroke="#8da2c4" tick={{ fontSize: 10 }} /><YAxis stroke="#8da2c4" /><Tooltip {...tip} />
              <Bar dataKey="rate" name="Defect %" fill="#a855f7" />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="panel"><h3>Severity</h3>
          <ResponsiveContainer width="100%" height={230}>
            <PieChart><Pie data={severity || []} dataKey="total" nameKey="name" outerRadius={85} label>
              {(severity || []).map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}</Pie><Tooltip {...tip} /><Legend /></PieChart>
          </ResponsiveContainer>
        </div>
        <div className="panel"><h3>Disposition / Shift</h3>
          <ResponsiveContainer width="100%" height={230}>
            <PieChart>
              <Pie data={disp || []} dataKey="total" nameKey="name" outerRadius={60} cx="30%" label>
                {(disp || []).map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}</Pie>
              <Pie data={shifts || []} dataKey="total" nameKey="name" outerRadius={60} cx="75%" label>
                {(shifts || []).map((_, i) => <Cell key={i} fill={COLORS[(i + 4) % COLORS.length]} />)}</Pie>
              <Tooltip {...tip} /><Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </>
  );
}
