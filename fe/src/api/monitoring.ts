import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './axios';
import type { MonitoringStatus } from './types';

const STATUS_KEY = ['monitoring', 'status'] as const;

export function useMonitoringStatus() {
  return useQuery({
    queryKey: STATUS_KEY,
    queryFn: async () => (await api.get<MonitoringStatus>('/api/monitoring/status')).data,
  });
}

export function useStartMonitoring() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (cepEnabled: boolean) =>
      (await api.post<MonitoringStatus>('/api/monitoring/start', { cepEnabled })).data,
    onSuccess: (data) => qc.setQueryData(STATUS_KEY, data),
  });
}

export function useStopMonitoring() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async () => (await api.post<MonitoringStatus>('/api/monitoring/stop')).data,
    onSuccess: (data) => qc.setQueryData(STATUS_KEY, data),
  });
}
