import { useQuery } from '@tanstack/react-query';
import { api } from './axios';
import type { SimulationParams, SimulationResult } from './types';

async function runSimulation(params: SimulationParams): Promise<SimulationResult> {
  const { data } = await api.get<SimulationResult>('/api/simulation', {
    params: {
      startDate: params.startDate,
      endDate: params.endDate,
      step: params.step ?? 'HOUR',
    },
  });
  return data;
}

export function useSimulation(params: SimulationParams | null) {
  return useQuery({
    queryKey: ['simulation', params],
    queryFn: () => runSimulation(params as SimulationParams),
    enabled: params != null,
  });
}
