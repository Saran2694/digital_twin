import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Home, Factory, BarChart3, AlertTriangle, Wrench, FileText, Settings, Bell, Maximize2, LogOut } from 'lucide-react';
import { useAuth } from '../auth.jsx';
import api, { usePolling } from '../api';

const NAV = [
  { to: '/', label: 'Dashboard', icon: Home, end: true },
  { to: '/factory', label: 'Factory View', icon: Factory },
  { to: '/analytics', label: 'Analytics', icon: BarChart3 },
  { to: '/alerts', label: 'Alerts', icon: AlertTriangle, badge: true },
  { to: '/maintenance', label: 'Maintenance', icon: Wrench },
  { to: '/reports', label: 'Reports', icon: FileText },
  { to: '/settings', label: 'Settings', icon: Settings },
];

const TITLES = {
  '/': 'Digital Twin Monitoring Portal',
  '/factory': 'Digital Factory View',
  '/analytics': 'Historical Data Analytics',
  '/alerts': 'Alert Management',
  '/maintenance': 'Maintenance Management',
  '/reports': 'Reports',
  '/settings': 'Admin Dashboard',
};

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { data: summary, error } = usePolling(() => api.get('/dashboard/summary').then((r) => r.data), [], 10000);
  const [updated, setUpdated] = useState(new Date());
  useEffect(() => { if (summary) setUpdated(new Date()); }, [summary]);
  const { pathname } = useLocation();
  const initials = (user?.fullName || user?.username || '?').split(' ').map((s) => s[0]).join('').slice(0, 2).toUpperCase();
  const title = TITLES[pathname] || TITLES['/'];
  const subtitle = 'Real-Time Factory Intelligence';

  const toggleFullscreen = () => {
    if (document.fullscreenElement) document.exitFullscreen();
    else document.documentElement.requestFullscreen();
  };

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand"><img src="/assets/logo.png" alt="Casting Twin" /></div>
        <nav>
          {NAV.map(({ to, label, icon: Icon, end, badge }) => (
            <NavLink key={to} to={to} end={end} className={({ isActive }) => 'nav-item' + (isActive ? ' active' : '')}>
              <Icon size={22} />
              <span>{label}</span>
              {badge && summary?.activeAlerts > 0 && <b className="badge">{summary.activeAlerts}</b>}
            </NavLink>
          ))}
        </nav>
        <div className="user-box">
          <div className="avatar">{initials}</div>
          <div>
            <div className="user-name">{user?.fullName || user?.username}</div>
            <div className="user-role">{user?.role === 'ADMIN' ? 'Admin' : user?.role}</div>
          </div>
        </div>
        <button className="logout" onClick={() => { logout(); navigate('/login'); }}>
          <LogOut size={18} /> Logout
        </button>
      </aside>

      <div className="main">
        <header className="topbar">
          <div>
            <h1>{title}</h1>
            <p>{subtitle}</p>
          </div>
          <div className="top-right">
            <span className="live"><i className={error ? 'dot off' : 'dot'} /> {error ? 'Offline' : 'Live'}</span>
            <span className="updated">Last Updated: {updated.toLocaleTimeString('en-US')}</span>
            <button className="icon-btn" onClick={() => navigate('/alerts')} title="Alerts">
              <Bell size={22} />
              {summary?.activeAlerts > 0 && <b className="badge small">{summary.activeAlerts}</b>}
            </button>
            <button className="icon-btn" onClick={toggleFullscreen} title="Fullscreen"><Maximize2 size={22} /></button>
          </div>
        </header>
        <div className="content"><Outlet context={{ summary }} /></div>
      </div>
    </div>
  );
}
