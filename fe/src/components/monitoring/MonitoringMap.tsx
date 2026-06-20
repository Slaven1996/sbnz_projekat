import type { ReactNode } from 'react';
import { CircleMarker, MapContainer, Popup, TileLayer, Tooltip } from 'react-leaflet';
import { Box, Divider, Stack, Typography } from '@mui/material';
import 'leaflet/dist/leaflet.css';
import type { MonitoringLocationState, MonitoringSeverity } from '@/api/types';

export const SEVERITY_COLOR: Record<string, string> = {
  NORMAL: '#2e7d32',
  WARNING: '#f9a825',
  DANGER: '#ef6c00',
  CRITICAL: '#c62828',
};

export interface MapPoint {
  code: string;
  name: string;
  lat: number;
  lng: number;
  state?: MonitoringLocationState;
}

const CENTER: [number, number] = [45.45, 19.9];

function radiusFor(severity: MonitoringSeverity | string): number {
  switch (severity) {
    case 'CRITICAL': return 16;
    case 'DANGER': return 13;
    case 'WARNING': return 11;
    default: return 9;
  }
}

function Row({ label, value }: { label: string; value: ReactNode }) {
  if (value === null || value === undefined || value === '') return null;
  return (
    <Stack direction="row" justifyContent="space-between" spacing={2}>
      <Typography variant="caption" color="text.secondary">{label}</Typography>
      <Typography variant="caption" fontWeight={600}>{value}</Typography>
    </Stack>
  );
}

export function MonitoringMap({ points }: { points: MapPoint[] }) {
  return (
    <MapContainer
      center={CENTER}
      zoom={8}
      scrollWheelZoom
      style={{ height: '100%', width: '100%', minHeight: 540, borderRadius: 8 }}
    >
      <TileLayer
        attribution='&copy; OpenStreetMap contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {points.map((p) => {
        const severity = p.state?.severity ?? 'NORMAL';
        const color = SEVERITY_COLOR[severity] ?? '#607d8b';
        const s = p.state;
        return (
          <CircleMarker
            key={p.code}
            center={[p.lat, p.lng]}
            radius={radiusFor(severity)}
            pathOptions={{ color: '#ffffff', weight: 2, fillColor: color, fillOpacity: 0.85 }}
          >
            <Tooltip direction="top" offset={[0, -4]}>
              {p.name} - {severity}
            </Tooltip>
            <Popup>
              <Box sx={{ minWidth: 200 }}>
                <Typography variant="subtitle2" gutterBottom>{p.name}</Typography>
                <Typography variant="caption" color="text.secondary">{p.code}</Typography>
                <Divider sx={{ my: 0.5 }} />
                {s ? (
                  <Stack spacing={0.25}>
                    <Row label="Severity" value={severity} />
                    <Row
                      label="Water level"
                      value={s.waterLevel ? `${s.waterLevel}${s.waterValue != null ? ` (${s.waterValue} cm)` : ''}` : null}
                    />
                    <Row
                      label="Flow rate"
                      value={s.flowLevel ? `${s.flowLevel}${s.flowValue != null ? ` (${s.flowValue} m³/s)` : ''}` : null}
                    />
                    <Row
                      label="Pumps"
                      value={s.capacityLevel ? `${s.capacityLevel}${s.totalPumps ? ` (${s.activePumps ?? 0}/${s.totalPumps})` : ''}` : null}
                    />
                    <Row label="Flood risk" value={s.riskLevel} />
                    <Row label="Action" value={s.recommendation} />
                  </Stack>
                ) : (
                  <Typography variant="caption" color="text.secondary">
                    No live data yet - start monitoring.
                  </Typography>
                )}
              </Box>
            </Popup>
          </CircleMarker>
        );
      })}
    </MapContainer>
  );
}
