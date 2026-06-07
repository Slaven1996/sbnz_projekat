import {
  Avatar,
  Box,
  Card,
  CardContent,
  Chip,
  Divider,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import BadgeIcon from '@mui/icons-material/Badge';
import EmailIcon from '@mui/icons-material/Email';
import ShieldIcon from '@mui/icons-material/Shield';
import ScheduleIcon from '@mui/icons-material/Schedule';
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
  const { email, role, expiresAt } = useAuth();

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
                {(email?.[0] ?? '?').toUpperCase()}
              </Avatar>
              <Typography variant="h6">{email}</Typography>
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
              <Stack spacing={2} divider={<Divider flexItem />}>
                <InfoRow icon={<EmailIcon />} label="E-mail" value={email ?? '—'} />
                <InfoRow icon={<ShieldIcon />} label="Role" value={role ?? '—'} />
                <InfoRow
                  icon={<BadgeIcon />}
                  label="Access level"
                  value={role === 'ADMIN' ? 'Full management (create/edit/delete)' : 'Read-only'}
                />
                <InfoRow
                  icon={<ScheduleIcon />}
                  label="Session expires"
                  value={expiresAt ? new Date(expiresAt).toLocaleString() : '—'}
                />
              </Stack>
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
