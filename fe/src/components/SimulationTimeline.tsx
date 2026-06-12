import { Box, Chip, Divider, Paper, Stack, Typography } from '@mui/material';
import type { ChipProps } from '@mui/material';
import Timeline from '@mui/lab/Timeline';
import TimelineItem, { timelineItemClasses } from '@mui/lab/TimelineItem';
import TimelineSeparator from '@mui/lab/TimelineSeparator';
import TimelineConnector from '@mui/lab/TimelineConnector';
import TimelineContent from '@mui/lab/TimelineContent';
import TimelineDot from '@mui/lab/TimelineDot';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import WavesIcon from '@mui/icons-material/Waves';
import PrecisionManufacturingIcon from '@mui/icons-material/PrecisionManufacturing';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import RecommendIcon from '@mui/icons-material/Recommend';
import ArrowRightAltIcon from '@mui/icons-material/ArrowRightAlt';
import type { SimulationLocationState, SimulationTimelineEvent } from '@/api/types';

type ChipColor = ChipProps['color'];

function alertHex(level: string | null | undefined): string {
  switch (level) {
    case 'GREEN':
      return '#2e7d32';
    case 'YELLOW':
      return '#f9a825';
    case 'ORANGE':
      return '#ed6c02';
    case 'RED':
      return '#d32f2f';
    default:
      return '#9e9e9e';
  }
}

function severityColor(value: string | null | undefined): ChipColor {
  switch (value) {
    case 'NORMAL':
    case 'LOW':
    case 'FULL':
    case 'GREEN':
      return 'success';
    case 'ELEVATED':
    case 'MODERATE':
    case 'REDUCED':
    case 'MONITOR':
    case 'YELLOW':
      return 'info';
    case 'HIGH':
    case 'MINIMAL':
    case 'PREPARE':
    case 'ORANGE':
      return 'warning';
    case 'CRITICAL':
    case 'EXTREME':
    case 'OFFLINE':
    case 'ACTIVATE':
    case 'EVACUATE':
    case 'RED':
      return 'error';
    default:
      return 'default';
  }
}

function fmt(value: number | null | undefined): string {
  if (value == null) return '';
  return Number.isInteger(value) ? String(value) : value.toFixed(1);
}

function LocationCard({ s }: { s: SimulationLocationState }) {
  return (
    <Box
      sx={{
        p: 1.25,
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 1,
        bgcolor: 'background.default',
      }}
    >
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 0.75 }}>
        <Typography variant="subtitle2" fontWeight={700}>
          {s.locationCode}
        </Typography>
        <Chip size="small" variant="outlined" label={s.locationType} />
        {s.zoneCode && <Chip size="small" variant="outlined" label={s.zoneCode} />}
      </Stack>

      <Stack direction="row" spacing={0.75} flexWrap="wrap" useFlexGap>
        {s.waterLevel && (
          <Chip
            size="small"
            icon={<WaterDropIcon />}
            color={severityColor(s.waterLevel)}
            label={`Water ${s.waterLevel}${s.waterValue != null ? ` (${fmt(s.waterValue)})` : ''}`}
          />
        )}
        {s.flowLevel && (
          <Chip
            size="small"
            icon={<WavesIcon />}
            color={severityColor(s.flowLevel)}
            label={`Flow ${s.flowLevel}${s.flowValue != null ? ` (${fmt(s.flowValue)})` : ''}`}
          />
        )}
        {s.capacityLevel && (
          <Chip
            size="small"
            icon={<PrecisionManufacturingIcon />}
            color={severityColor(s.capacityLevel)}
            label={`Pumps ${s.capacityLevel}${
              s.activePumps != null && s.totalPumps != null
                ? ` (${s.activePumps}/${s.totalPumps})`
                : ''
            }`}
          />
        )}
        {s.riskLevel && (
          <Chip
            size="small"
            icon={<WarningAmberIcon />}
            color={severityColor(s.riskLevel)}
            label={`Risk ${s.riskLevel}`}
          />
        )}
        {s.recommendation && (
          <Chip
            size="small"
            icon={<RecommendIcon />}
            variant="outlined"
            color={severityColor(s.recommendation)}
            label={s.recommendation}
          />
        )}
      </Stack>
    </Box>
  );
}

function EventContent({ event }: { event: SimulationTimelineEvent }) {
  const hex = alertHex(event.systemAlertLevel);
  const when = new Date(event.stepTime).toLocaleString(undefined, {
    weekday: 'short',
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <Paper variant="outlined" sx={{ p: 1.5 }}>
      <Stack
        direction="row"
        justifyContent="space-between"
        alignItems="center"
        flexWrap="wrap"
        sx={{ mb: 1 }}
      >
        <Typography variant="subtitle1" fontWeight={700}>
          {when}
        </Typography>
        <Stack direction="row" spacing={1} alignItems="center">
          <Typography variant="caption" color="text.secondary">
            {event.firedRules} rules fired
          </Typography>
          <Chip
            size="small"
            label={`ALERT: ${event.systemAlertLevel ?? '-'}`}
            sx={{ bgcolor: hex, color: '#fff', fontWeight: 700 }}
          />
        </Stack>
      </Stack>

      {event.changes.length > 0 && (
        <Box sx={{ mb: 1 }}>
          {event.changes.map((c, i) => (
            <Stack key={i} direction="row" spacing={0.5} alignItems="center">
              <ArrowRightAltIcon fontSize="small" color="action" />
              <Typography variant="body2" fontWeight={600}>
                {c}
              </Typography>
            </Stack>
          ))}
        </Box>
      )}

      <Stack spacing={1}>
        {event.locationStates.map((s) => (
          <LocationCard key={s.locationCode} s={s} />
        ))}
      </Stack>

      {event.appliedReadings.length > 0 && (
        <>
          <Divider sx={{ my: 1 }} />
          <Typography variant="caption" color="text.secondary" fontWeight={600}>
            Readings applied
          </Typography>
          <Stack component="ul" sx={{ m: 0, mt: 0.5, pl: 2.5 }} spacing={0.25}>
            {event.appliedReadings.map((r, i) => (
              <Typography key={i} component="li" variant="caption" color="text.secondary">
                {r}
              </Typography>
            ))}
          </Stack>
        </>
      )}
    </Paper>
  );
}

export function SimulationTimeline({ events }: { events: SimulationTimelineEvent[] }) {
  if (events.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No timeline steps were produced for this period.
      </Typography>
    );
  }
  return (
    <Timeline
      sx={{
        p: 0,
        m: 0,
        [`& .${timelineItemClasses.root}:before`]: { flex: 0, padding: 0 },
      }}
    >
      {events.map((event, i) => {
        const hex = alertHex(event.systemAlertLevel);
        return (
          <TimelineItem key={event.stepTime + i}>
            <TimelineSeparator>
              <TimelineDot sx={{ bgcolor: hex, borderColor: hex }} />
              {i < events.length - 1 && <TimelineConnector />}
            </TimelineSeparator>
            <TimelineContent sx={{ pb: 3 }}>
              <EventContent event={event} />
            </TimelineContent>
          </TimelineItem>
        );
      })}
    </Timeline>
  );
}
