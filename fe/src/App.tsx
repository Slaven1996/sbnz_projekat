import { Navigate, Route, Routes } from 'react-router-dom';
import { MainLayout } from '@/layout/MainLayout';
import { ProtectedRoute } from '@/routes/ProtectedRoute';
import { RoleRoute } from '@/routes/RoleRoute';
import { LoginPage } from '@/pages/LoginPage';
import { LiveDashboardPage } from '@/pages/LiveDashboardPage';
import { HistoricalTrendsPage } from '@/pages/HistoricalTrendsPage';
import { ProfilePage } from '@/pages/ProfilePage';
import { DepartmentsPage } from '@/pages/DepartmentsPage';
import { ZonesPage } from '@/pages/ZonesPage';
import { LocationsPage } from '@/pages/LocationsPage';
import { SensorsPage } from '@/pages/SensorsPage';
import { TagUnitsPage } from '@/pages/TagUnitsPage';
import { ThresholdConfigsPage } from '@/pages/ThresholdConfigsPage';
import { TrendDataPage } from '@/pages/TrendDataPage';
import { UsersPage } from '@/pages/UsersPage';
import { NotFoundPage } from '@/pages/NotFoundPage';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route index element={<Navigate to="/live-dashboard" replace />} />
          <Route path="live-dashboard" element={<LiveDashboardPage />} />
          <Route path="historical-trends" element={<HistoricalTrendsPage />} />
          <Route path="zones" element={<ZonesPage />} />
          <Route path="locations" element={<LocationsPage />} />
          <Route path="sensors" element={<SensorsPage />} />
          <Route path="tag-units" element={<TagUnitsPage />} />
          <Route path="threshold-configs" element={<ThresholdConfigsPage />} />
          <Route path="trend-data" element={<TrendDataPage />} />
          <Route path="profile" element={<ProfilePage />} />

          <Route element={<RoleRoute allow={['ADMIN']} />}>
            <Route path="departments" element={<DepartmentsPage />} />
            <Route path="users" element={<UsersPage />} />
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  );
}
