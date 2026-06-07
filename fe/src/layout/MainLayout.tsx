import { useState } from 'react';
import {
  AppBar,
  Avatar,
  Box,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Toolbar,
  Typography,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import { Outlet, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAuth } from '@/store/hooks';
import { logout } from '@/store/authSlice';
import { DRAWER_WIDTH, Sidebar } from './Sidebar';

export function MainLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { email, role } = useAuth();
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const handleLogout = () => {
    setAnchorEl(null);
    dispatch(logout());
    navigate('/login', { replace: true });
  };

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <AppBar
        position="fixed"
        color="inherit"
        elevation={0}
        sx={{
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { md: `${DRAWER_WIDTH}px` },
          borderBottom: 1,
          borderColor: 'divider',
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            edge="start"
            onClick={() => setMobileOpen(true)}
            sx={{ mr: 2, display: { md: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Box sx={{ flexGrow: 1 }} />
          {role && (
            <Chip
              label={role}
              size="small"
              color={role === 'ADMIN' ? 'primary' : 'default'}
              sx={{ mr: 2 }}
            />
          )}
          <Typography variant="body2" sx={{ mr: 1, display: { xs: 'none', sm: 'block' } }}>
            {email}
          </Typography>
          <IconButton onClick={(e) => setAnchorEl(e.currentTarget)} size="small">
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
              {(email?.[0] ?? '?').toUpperCase()}
            </Avatar>
          </IconButton>
          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
            <MenuItem
              onClick={() => {
                setAnchorEl(null);
                navigate('/profile');
              }}
            >
              My Profile
            </MenuItem>
            <MenuItem onClick={handleLogout}>
              <LogoutIcon fontSize="small" sx={{ mr: 1 }} /> Logout
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      <Sidebar mobileOpen={mobileOpen} onClose={() => setMobileOpen(false)} />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          p: { xs: 2, sm: 3 },
        }}
      >
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
}
