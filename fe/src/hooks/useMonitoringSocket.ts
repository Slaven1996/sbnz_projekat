import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { MonitoringEvent, MonitoringTick } from '@/api/types';

const WS_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8090';
const MAX_FEED = 120;

export interface MonitoringSocketState {
  connected: boolean;
  latest: MonitoringTick | null;
  feed: MonitoringEvent[];
}

export function useMonitoringSocket(enabled = true): MonitoringSocketState {
  const [connected, setConnected] = useState(false);
  const [latest, setLatest] = useState<MonitoringTick | null>(null);
  const [feed, setFeed] = useState<MonitoringEvent[]>([]);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!enabled) return undefined;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE}/ws`),
      reconnectDelay: 4000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/monitoring', (message) => {
          try {
            const tick = JSON.parse(message.body) as MonitoringTick;
            setLatest(tick);
            if (tick.events && tick.events.length > 0) {
              setFeed((prev) => [...tick.events, ...prev].slice(0, MAX_FEED));
            }
          } catch (e) {
            console.error('Failed to parse monitoring tick:', e);
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      void client.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [enabled]);

  return { connected, latest, feed };
}
