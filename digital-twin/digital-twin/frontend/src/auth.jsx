import { createContext, useContext, useState } from 'react';
import api from './api';

const AuthCtx = createContext(null);
export const useAuth = () => useContext(AuthCtx);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('twin_user')); } catch { return null; }
  });

  const login = async (username, password) => {
    const { data } = await api.post('/auth/login', { username, password });
    localStorage.setItem('twin_token', data.token);
    localStorage.setItem('twin_user', JSON.stringify(data.user));
    setUser(data.user);
  };

  const logout = () => {
    localStorage.removeItem('twin_token');
    localStorage.removeItem('twin_user');
    setUser(null);
  };

  return <AuthCtx.Provider value={{ user, login, logout, isAdmin: user?.role === 'ADMIN' }}>{children}</AuthCtx.Provider>;
}
