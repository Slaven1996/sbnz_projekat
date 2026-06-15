import { useEffect } from 'react';
import { CircleMarker, MapContainer, TileLayer, Tooltip, useMap, useMapEvents } from 'react-leaflet';
import { Box, Stack, Typography } from '@mui/material';
import PlaceIcon from '@mui/icons-material/Place';
import 'leaflet/dist/leaflet.css';

const DEFAULT_CENTER: [number, number] = [45.45, 19.9];

function round(v: number): number {
  return Math.round(v * 1e6) / 1e6;
}

function ClickHandler({ onPick }: { onPick: (lat: number, lng: number) => void }) {
  useMapEvents({
    click(e) {
      onPick(round(e.latlng.lat), round(e.latlng.lng));
    },
  });
  return null;
}

function InvalidateSize() {
  const map = useMap();
  useEffect(() => {
    const t = setTimeout(() => map.invalidateSize(), 300);
    return () => clearTimeout(t);
  }, [map]);
  return null;
}

export interface LocationPickerProps {
  lat: number | null | undefined;
  lng: number | null | undefined;
  onChange: (lat: number, lng: number) => void;
  height?: number;
}

export function LocationPicker({ lat, lng, onChange, height = 300 }: LocationPickerProps) {
  const hasPoint = lat != null && lng != null;
  const center: [number, number] = hasPoint ? [lat as number, lng as number] : DEFAULT_CENTER;

  return (
    <Stack spacing={1}>
      <Stack direction="row" spacing={0.5} alignItems="center" color="text.secondary">
        <PlaceIcon sx={{ fontSize: 16 }} />
        <Typography variant="caption">
          Click on the map to set the position — Position X and Y fill automatically.
        </Typography>
      </Stack>
      <Box sx={{ height, borderRadius: 1, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}>
        <MapContainer
          center={center}
          zoom={hasPoint ? 12 : 8}
          scrollWheelZoom
          style={{ height: '100%', width: '100%' }}
        >
          <TileLayer
            attribution="&copy; OpenStreetMap contributors"
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <InvalidateSize />
          <ClickHandler onPick={onChange} />
          {hasPoint && (
            <CircleMarker
              center={[lat as number, lng as number]}
              radius={9}
              pathOptions={{ color: '#ffffff', weight: 2, fillColor: '#1976d2', fillOpacity: 0.9 }}
            >
              <Tooltip direction="top" offset={[0, -6]}>
                {(lat as number).toFixed(5)}, {(lng as number).toFixed(5)}
              </Tooltip>
            </CircleMarker>
          )}
        </MapContainer>
      </Box>
    </Stack>
  );
}
