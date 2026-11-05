import { useState } from 'react';
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import api, { usePolling } from '../api';

const RANGES = ['1h', '6h', '24h', '7d'];

export default function SensorTrends({ machineId }) {
  const [range, setRange] = useState('1h');
  const { data = [] } = usePolling(
    () => (machineId ? api.get(`/machines/${machineId}/trend`, { params: { range } }).then((r) => r.data) : []),
    [machineId, range], 15000);
  const points = data || [];

  return (
    <div className="panel trends">
      <div className="panel-head">
        <h3>Sensor Trends</h3>
        <div className="range-tabs">
          {RANGES.map((r) => (
            <button key={r} className={r === range ? 'on' : ''} onClick={() => setRange(r)}>{r}</button>
          ))}
        </div>
      </div>
      <div className="legend">
        <span><i style={{ background: '#2f8cff' }} />Temperature (°C)</span>
        <span><i style={{ background: '#f59e0b' }} />Pressure (bar)</span>
      </div>
      <ResponsiveContainer width="100%" height={190}>
        <LineChart data={points} margin={{ top: 6, right: 6, left: -10, bottom: 0 }}>
          <CartesianGrid stroke="#1b2c4d" vertical={false} />
          <XAxis dataKey="label" stroke="#8da2c4" tick={{ fontSize: 11 }} interval="preserveStartEnd" />
          <YAxis yAxisId="t" stroke="#8da2c4" tick={{ fontSize: 11 }} domain={['auto', 'auto']} />
          <YAxis yAxisId="p" orientation="right" stroke="#8da2c4" tick={{ fontSize: 11 }} domain={['auto', 'auto']} />
          <Tooltip contentStyle={{ background: '#0b1830', border: '1px solid #26395e' }} />
          <Line yAxisId="t" type="monotone" dataKey="temperature" name="Temperature" stroke="#2f8cff" dot={false} strokeWidth={2} />
          <Line yAxisId="p" type="monotone" dataKey="pressure" name="Pressure" stroke="#f59e0b" dot={false} strokeWidth={2} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
