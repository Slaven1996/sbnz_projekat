import {
  Avatar,
  Box,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Divider,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import BadgeIcon from '@mui/icons-material/Badge';
import EmailIcon from '@mui/icons-material/Email';
import ShieldIcon from '@mui/icons-material/Shield';
import ScheduleIcon from '@mui/icons-material/Schedule';
import PersonIcon from '@mui/icons-material/Person';
import ApartmentIcon from '@mui/icons-material/Apartment';
import ToggleOnIcon from '@mui/icons-material/ToggleOn';
import { useQuery } from '@tanstack/react-query';
import { usersResource } from '@/api/resources';
import { PageHeader } from '@/components/PageHeader';
import { ChangePasswordCard } from '@/components/ChangePasswordCard';
import { useAuth } from '@/store/hooks';

function InfoRow({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <Stack direction="row" spacing={2} alignItems="center">
      <Box sx={{ color: 'primary.main', display: 'flex' }}>{icon}</Box>
      <Box>
        <Typography variant="caption" color="text.secondary">
          {label}
        </Typography>
        <Typography variant="body1">{value}</Typography>
      </Box>
    </Stack>
  );
}

export function ProfilePage() {
  const { userId, email, role, expiresAt } = useAuth();
  const { data: me, isLoading } = useQuery({
    queryKey: ['users', userId],
    queryFn: () => usersResource.api.get(userId as number),
    enabled: userId != null,
  });

  const fullName = [me?.name, me?.lastName].filter(Boolean).join(' ') || '-';

  return (
    <Box>
      <PageHeader title="My Profile" subtitle="Your account and current session" />
      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card variant="outlined">
            <CardContent sx={{ textAlign: 'center', py: 4 }}>
              <Avatar
                sx={{ width: 80, height: 80, bgcolor: 'primary.main', mx: 'auto', mb: 2, fontSize: 32 }}
              >
                {(me?.name?.[0] ?? email?.[0] ?? '?').toUpperCase()}
              </Avatar>
              <Typography variant="h6">{fullName !== '-' ? fullName : email}</Typography>
              <Typography variant="body2" color="text.secondary">
                {email}
              </Typography>
              <Chip
                label={role ?? 'UNKNOWN'}
                color={role === 'ADMIN' ? 'primary' : 'default'}
                size="small"
                sx={{ mt: 1 }}
              />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={8}>
          <Card variant="outlined">
            <CardContent>
              <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
                Account details
              </Typography>
              {isLoading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                  <CircularProgress size={28} />
                </Box>
              ) : (
                <Stack spacing={2} divider={<Divider flexItem />}>
                  <InfoRow icon={<PersonIcon />} label="First name" value={me?.name || '-'} />
                  <InfoRow icon={<PersonIcon />} label="Last name" value={me?.lastName || '-'} />
                  <InfoRow icon={<EmailIcon />} label="E-mail" value={me?.email ?? email ?? '-'} />
                  <InfoRow icon={<ShieldIcon />} label="Role" value={me?.role ?? role ?? '-'} />
                  <InfoRow
                    icon={<ApartmentIcon />}
                    label="Department"
                    value={me?.departmentName || me?.departmentCode || '-'}
                  />
                  <InfoRow
                    icon={<ToggleOnIcon />}
                    label="Status"
                    value={me?.active ? 'Active' : 'Inactive'}
                  />
                  <InfoRow
                    icon={<BadgeIcon />}
                    label="Access level"
                    value={role === 'ADMIN' ? 'Full management (create/edit/delete)' : 'Read-only'}
                  />
                  <InfoRow
                    icon={<ScheduleIcon />}
                    label="Session expires"
                    value={expiresAt ? new Date(expiresAt).toLocaleString() : '-'}
                  />
                </Stack>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12}>
          <ChangePasswordCard />
        </Grid>
      </Grid>
    </Box>
  );
}
