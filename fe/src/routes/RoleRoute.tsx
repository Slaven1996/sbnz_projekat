import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/store/hooks';
import type { UserRole } from '@/api/types';

export function RoleRoute({ allow }: { allow: UserRole[] }) {
  const { role } = useAuth();
  if (!role || !allow.includes(role)) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
