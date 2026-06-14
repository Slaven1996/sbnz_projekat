import { ReactNode } from 'react';
import DashboardIcon from '@mui/icons-material/Dashboard';
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart';
import TimelineIcon from '@mui/icons-material/Timeline';
import PlaceIcon from '@mui/icons-material/Place';
import MapIcon from '@mui/icons-material/Map';
import SensorsIcon from '@mui/icons-material/Sensors';
import StraightenIcon from '@mui/icons-material/Straighten';
import TuneIcon from '@mui/icons-material/Tune';
import ShowChartIcon from '@mui/icons-material/ShowChart';
import GroupIcon from '@mui/icons-material/Group';
import PersonIcon from '@mui/icons-material/Person';
import type { UserRole } from '@/api/types';

export interface NavItem {
  label: string;
  path: string;
  icon: ReactNode;
  roles?: UserRole[];
}

export const NAV_ITEMS: NavItem[] = [
  { label: 'Live Dashboard', path: '/live-dashboard', icon: <MonitorHeartIcon /> },
  { label: 'Historical Trends', path: '/historical-trends', icon: <TimelineIcon /> },
  { label: 'Departments', path: '/departments', icon: <DashboardIcon />, roles: ['ADMIN'] },
  { label: 'Zones', path: '/zones', icon: <MapIcon /> },
  { label: 'Locations', path: '/locations', icon: <PlaceIcon /> },
  { label: 'Sensors', path: '/sensors', icon: <SensorsIcon /> },
  { label: 'Tag Units', path: '/tag-units', icon: <StraightenIcon /> },
  { label: 'Threshold Configs', path: '/threshold-configs', icon: <TuneIcon /> },
  { label: 'Trend Data', path: '/trend-data', icon: <ShowChartIcon /> },

  { label: 'Users', path: '/users', icon: <GroupIcon />, roles: ['ADMIN'] },

  { label: 'My Profile', path: '/profile', icon: <PersonIcon /> },
];

export function visibleNavItems(role: UserRole | null): NavItem[] {
  return NAV_ITEMS.filter((item) => !item.roles || (role && item.roles.includes(role)));
}
