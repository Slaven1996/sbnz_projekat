import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseQueryOptions,
} from '@tanstack/react-query';
import { api } from '@/api/axios';
import type { PageParams, PagedResponse } from '@/api/types';

function toPageQuery(params: PageParams = {}): Record<string, unknown> {
  const q: Record<string, unknown> = {};
  if (params.page != null) q.page = params.page;
  if (params.size != null) q.size = params.size;
  if (params.sort) q.sort = params.sort;
  return q;
}

export function createCrudResource<TResponse, TRequest>(opts: {
  key: string;
  path?: string;
}) {
  const key = opts.key;
  const basePath = `/api/${opts.path ?? opts.key}`;

  const apiClient = {
    list: async (params?: PageParams) => {
      const { data } = await api.get<PagedResponse<TResponse>>(basePath, {
        params: toPageQuery(params),
      });
      return data;
    },
    get: async (id: number) => {
      const { data } = await api.get<TResponse>(`${basePath}/${id}`);
      return data;
    },
    create: async (body: TRequest) => {
      const { data } = await api.post<TResponse>(basePath, body);
      return data;
    },
    update: async (id: number, body: TRequest) => {
      const { data } = await api.put<TResponse>(`${basePath}/${id}`, body);
      return data;
    },
    remove: async (id: number) => {
      await api.delete(`${basePath}/${id}`);
    },
  };

  const useList = (
    params: PageParams,
    options?: Omit<UseQueryOptions<PagedResponse<TResponse>>, 'queryKey' | 'queryFn'>,
  ) =>
    useQuery({
      queryKey: [key, 'list', params],
      queryFn: () => apiClient.list(params),
      ...options,
    });

  const useOptions = () =>
    useQuery({
      queryKey: [key, 'options'],
      queryFn: () => apiClient.list({ page: 0, size: 1000, sort: undefined }),
      select: (data) => data.content,
      staleTime: 60_000,
    });

  const useCreate = () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: (body: TRequest) => apiClient.create(body),
      onSuccess: () => qc.invalidateQueries({ queryKey: [key] }),
    });
  };

  const useUpdate = () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: ({ id, body }: { id: number; body: TRequest }) => apiClient.update(id, body),
      onSuccess: () => qc.invalidateQueries({ queryKey: [key] }),
    });
  };

  const useRemove = () => {
    const qc = useQueryClient();
    return useMutation({
      mutationFn: (id: number) => apiClient.remove(id),
      onSuccess: () => qc.invalidateQueries({ queryKey: [key] }),
    });
  };

  return { key, basePath, api: apiClient, useList, useOptions, useCreate, useUpdate, useRemove };
}
