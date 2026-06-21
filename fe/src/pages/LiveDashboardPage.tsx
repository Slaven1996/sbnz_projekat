import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  FormControlLabel,
  Grid,
  List,
  ListItem,
  ListItemText,
  Stack,
  Switch,
  Typography,
} from '@mui/material';
import { keyframes } from '@mui/system';
import CircleIcon from '@mui/icons-material/Circle';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import StopIcon from '@mui/icons-material/Stop';
import {
  useMonitoringStatus,
  useStartMonitoring,
  useStopMonitoring,
} from '@/api/monitoring';
import { locationsResource, zonesResource } from '@/api/resources';
import { MonitoringMap, SEVERITY_COLOR, type MapPoint } from '@/components/monitoring/MonitoringMap';
import { useNavigationBlocker } from '@/components/NavigationGuard';
import { useMonitoringSocket } from '@/hooks/useMonitoringSocket';
import { PageHeader } from '@/components/PageHeader';
import { useAuth } from '@/store/hooks';

const pulse = keyframes`
  0% { opacity: 1; }
  50% { opacity: 0.25; }
  100% { opacity: 1; }
`;

const ALERT_CHIP_COLOR: Record<string, 'success' | 'warning' | 'error' | 'default'> = {
  GREEN: 'success',
  YELLOW: 'warning',
  ORANGE: 'warning',
  RED: 'error',
};

export function LiveDashboardPage() {
  const { role } = useAuth();
  const canControl = role === 'ADMIN' || role === 'OPERATOR';

  const { data: status } = useMonitoringStatus();
  const startMutation = useStartMonitoring();
  const stopMutation = useStopMonitoring();
  const { connected, latest, feed, reset } = useMonitoringSocket(true);

  const [cep, setCep] = useState(false);
  
  useEffect(() => {
    if (status) setCep(status.cepEnabled);
  }, [status?.cepEnabled]);

  const isActive = status?.active ?? false;

  const { data: locations } = locationsResource.useOptions();
  const { data: zones } = zonesResource.useOptions();

  const zoneNameById = useMemo(() => {
    const map = new Map<number, string>();
    (zones ?? []).forEach((z) => map.set(z.id, z.name || z.code));
    return map;
  }, [zones]);

  const stateByCode = useMemo(() => {
    const map = new Map<string, NonNullable<typeof latest>['locations'][number]>();
    latest?.locations.forEach((l) => map.set(l.locationCode, l));
    return map;
  }, [latest]);

  const points: MapPoint[] = useMemo(() => {
    const base = (locations ?? []).filter((l) => l.posX != null && l.posY != null);
    return base.map((l) => ({
      code: l.code,
      name: l.displayCode || l.code,
      displayCode: l.displayCode,
      type: l.type,
      zoneName: l.zoneId != null ? zoneNameById.get(l.zoneId) ?? l.zoneCode : l.zoneCode,
      lat: l.posX as number,
      lng: l.posY as number,
      state: stateByCode.get(l.code),
    }));
  }, [locations, stateByCode, zoneNameById]);

  const handleToggle = () => {
    if (isActive) {
      stopMutation.mutate();
    } else {
      reset();
      startMutation.mutate(cep);
    }
  };

  const busy = startMutation.isPending || stopMutation.isPending;
  const alertLevel = latest?.systemAlertLevel ?? null;

  useNavigationBlocker({
    when: isActive,
    title: 'Stop monitoring?',
    message: 'Live monitoring is running. Leaving this page will stop it. Do you want to continue?',
    confirmLabel: 'Leave & stop',
    cancelLabel: 'Stay',
    onConfirm: () => {
      stopMutation.mutate();
    },
  });

  return (
    <Box>
      <PageHeader
        title="Live Monitoring Dashboard"
      />
      <Stack
        direction="row"
        spacing={2}
        alignItems="center"
        flexWrap="wrap"
        useFlexGap
        sx={{ mb: 2 }}
      >
        <Chip
          icon={<CircleIcon sx={{ fontSize: 12, animation: isActive ? `${pulse} 1.4s ease-in-out infinite` : 'none' }} />}
          label={isActive ? 'MONITORING ACTIVE' : 'STOPPED'}
          color={isActive ? 'success' : 'default'}
          variant={isActive ? 'filled' : 'outlined'}
        />
        <Chip
          size="small"
          label={connected ? 'WebSocket connected' : 'WebSocket offline'}
          color={connected ? 'info' : 'default'}
          variant="outlined"
        />
        {latest && (
          <>
            <Chip size="small" variant="outlined" label={`Sim clock: ${latest.pseudoTime?.replace('T', ' ')}`} />
            <Chip size="small" variant="outlined" label={`Tick #${latest.tick}`} />
            <Chip size="small" variant="outlined" label={`${latest.firedRules} rules fired`} />
            <Chip
              size="small"
              variant="outlined"
              color={latest.cepEnabled ? 'secondary' : 'default'}
              label={latest.cepEnabled ? 'CEP ON' : 'CEP OFF'}
            />
          </>
        )}
        {alertLevel && (
          <Chip
            size="small"
            color={ALERT_CHIP_COLOR[alertLevel] ?? 'default'}
            label={`SYSTEM ALERT: ${alertLevel}`}
          />
        )}
      </Stack>

      <Grid container spacing={2}>
        <Grid item xs={12} md={8}>
          <Card sx={{ height: '100%' }}>
            <Box sx={{ height: 560, p: 1 }}>
              <MonitoringMap points={points} />
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Stack spacing={2}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>Control Panel</Typography>
                <FormControlLabel
                  control={
                    <Switch
                      checked={cep}
                      onChange={(e) => setCep(e.target.checked)}
                      disabled={isActive || !canControl}
                    />
                  }
                  label="Enable CEP (time-based) analysis"
                />

                <Button
                  fullWidth
                  variant="contained"
                  color={isActive ? 'error' : 'success'}
                  startIcon={isActive ? <StopIcon /> : <PlayArrowIcon />}
                  onClick={handleToggle}
                  disabled={busy || !canControl}
                >
                  {isActive ? 'Stop Monitoring' : 'Start Monitoring'}
                </Button>

                {!canControl && (
                  <Alert severity="info" sx={{ mt: 2 }}>
                    You can watch the live feed; starting/stopping requires a dispatcher or admin role.
                  </Alert>
                )}

                {status && (
                  <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 2 }}>
                    Tick interval {status.tickIntervalSeconds}s · pseudo-clock step {status.pseudoStepMinutes} min ·
                    {' '}{status.locationCount} sites
                  </Typography>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardContent sx={{ pb: 1 }}>
                <Stack direction="row" justifyContent="space-between" alignItems="center">
                  <Typography variant="h6">Live Event Feed</Typography>
                  <Chip size="small" label={feed.length} />
                </Stack>
              </CardContent>
              <Divider />
              <List dense sx={{ maxHeight: 420, overflowY: 'auto' }}>
                {feed.length === 0 && (
                  <ListItem>
                    <ListItemText
                      primary="No events yet"
                    />
                  </ListItem>
                )}
                {feed.map((ev, i) => (
                  <ListItem key={`${ev.time}-${i}`} alignItems="flex-start">
                    <CircleIcon
                      sx={{
                        fontSize: 12,
                        mt: 0.6,
                        mr: 1,
                        color: SEVERITY_COLOR[ev.severity] ?? '#607d8b',
                      }}
                    />
                    <ListItemText
                      primary={ev.message}
                      secondary={`${ev.locationCode ? `${ev.locationCode} · ` : ''}${ev.time?.replace('T', ' ')}`}
                      primaryTypographyProps={{ variant: 'body2' }}
                    />
                  </ListItem>
                ))}
              </List>
            </Card>
          </Stack>
        </Grid>
      </Grid>
    </Box>
  );
}
