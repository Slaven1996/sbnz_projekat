import { useQuery } from '@tanstack/react-query';
import { api } from './axios';
import type { PagedResponse, TrendDataResponse, TrendDataSearchParams } from './types';

async function searchTrendData(
  params: TrendDataSearchParams,
): Promise<PagedResponse<TrendDataResponse>> {
  const query: Record<string, unknown> = {};
  if (params.page != null) query.page = params.page;
  if (params.size != null) query.size = params.size;
  if (params.sort) query.sort = params.sort;
  if (params.locationCode) query.locationCode = params.locationCode;
  if (params.tagName) query.tagName = params.tagName;
  if (params.startDate) query.startDate = params.startDate;
  if (params.endDate) query.endDate = params.endDate;

  const { data } = await api.get<PagedResponse<TrendDataResponse>>('/api/trend-data', {
    params: query,
  });
  return data;
}

export function useTrendDataSearch(params: TrendDataSearchParams) {
  return useQuery({
    queryKey: ['trend-data', 'search', params],
    queryFn: () => searchTrendData(params),
  });
}
