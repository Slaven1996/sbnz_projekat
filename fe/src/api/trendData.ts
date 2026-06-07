import { useQuery } from '@tanstack/react-query';
import { api } from './axios';
import type { PagedResponse, TrendDataResponse, TrendDataSearchParams } from './types';

function buildQuery(params: TrendDataSearchParams): Record<string, unknown> {
  const query: Record<string, unknown> = {};
  if (params.page != null) query.page = params.page;
  if (params.size != null) query.size = params.size;
  if (params.sort) query.sort = params.sort;
  if (params.locationCode) query.locationCode = params.locationCode;
  if (params.tagName) query.tagName = params.tagName;
  if (params.startDate) query.startDate = params.startDate;
  if (params.endDate) query.endDate = params.endDate;
  return query;
}

async function searchTrendData(
  params: TrendDataSearchParams,
): Promise<PagedResponse<TrendDataResponse>> {
  const { data } = await api.get<PagedResponse<TrendDataResponse>>('/api/trend-data', {
    params: buildQuery(params),
  });
  return data;
}

async function fetchAllTrendData(params: TrendDataSearchParams): Promise<TrendDataResponse[]> {
  const { data } = await api.get<TrendDataResponse[]>('/api/trend-data', {
    params: { ...buildQuery(params), paginated: false },
  });
  return data;
}

export function useTrendDataSearch(params: TrendDataSearchParams, enabled = true) {
  return useQuery({
    queryKey: ['trend-data', 'search', params],
    queryFn: () => searchTrendData(params),
    enabled,
  });
}

export function useTrendDataAll(params: TrendDataSearchParams, enabled = true) {
  return useQuery({
    queryKey: ['trend-data', 'all', params],
    queryFn: () => fetchAllTrendData(params),
    enabled,
  });
}
