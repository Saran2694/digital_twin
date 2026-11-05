import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import api, { usePolling } from '../api';
import StatCards from '../components/StatCards.jsx';
import FactoryMap from '../components/FactoryMap.jsx';
import MachineDetails from '../components/MachineDetails.jsx';
import SensorTrends from '../components/SensorTrends.jsx';
import RecentAlerts from '../components/RecentAlerts.jsx';
import MaintenancePanel from '../components/MaintenancePanel.jsx';

export default function Dashboard() {
  const { summary } = useOutletContext();
  const { data: machines = [] } = usePolling(() => api.get('/machines').then((r) => r.data), [], 10000);
  const { data: alerts } = usePolling(() => api.get('/alerts/recent', { params: { limit: 3 } }).then((r) => r.data), [], 10000);
  const { data: requests } = usePolling(() => api.get('/maintenance', { params: { limit: 3 } }).then((r) => r.data), [], 15000);
  const [selectedId, setSelectedId] = useState(null);
  const list = machines || [];

  // default selection: first machine in WARNING state, otherwise the first machine
  useEffect(() => {
    if (!selectedId && list.length) setSelectedId((list.find((m) => m.status === 'WARNING') || list[0]).id);
  }, [list, selectedId]);

  const selected = list.find((m) => m.id === selectedId);

  return (
    <>
      <StatCards summary={summary} />
      <div className="grid-2">
        <div className="panel map-panel"><FactoryMap machines={list} selectedId={selectedId} onSelect={setSelectedId} /></div>
        <MachineDetails machine={selected} />
      </div>
      <div className="grid-3">
        <SensorTrends machineId={selectedId} />
        <RecentAlerts alerts={alerts || []} />
        <MaintenancePanel items={requests || []} />
      </div>
    </>
  );
}
