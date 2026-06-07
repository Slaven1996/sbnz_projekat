import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useIsAuthenticated } from '@/store/hooks';

export function ProtectedRoute() {
  const isAuthenticated = useIsAuthenticated();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return <Outlet />;
}
