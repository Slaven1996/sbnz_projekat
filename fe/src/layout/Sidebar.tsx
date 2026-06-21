import {
  Box,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/store/hooks';
import { useNavigationGuard } from '@/components/NavigationGuard';
import { visibleNavItems } from './navConfig';

export const DRAWER_WIDTH = 240;

interface SidebarProps {
  mobileOpen: boolean;
  onClose: () => void;
}

export function Sidebar({ mobileOpen, onClose }: SidebarProps) {
  const { role } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { attemptNavigate } = useNavigationGuard();
  const items = visibleNavItems(role);

  const content = (
    <Box>
      <Toolbar sx={{ gap: 1 }}>
        <WaterDropIcon color="primary" />
        <Typography variant="h6" fontWeight={700} noWrap>
          Hydro Monitor
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {items.map((item) => {
          const selected = location.pathname.startsWith(item.path);
          return (
            <ListItemButton
              key={item.path}
              selected={selected}
              onClick={() => {
                if (selected) {
                  onClose();
                  return;
                }
                attemptNavigate(() => {
                  navigate(item.path);
                  onClose();
                });
              }}
            >
              <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          );
        })}
      </List>
    </Box>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}
      aria-label="navigation"
    >
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onClose}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH },
        }}
      >
        {content}
      </Drawer>
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH },
        }}
        open
      >
        {content}
      </Drawer>
    </Box>
  );
}
