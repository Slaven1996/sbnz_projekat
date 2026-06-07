import axios, { AxiosError } from 'axios';
import { store } from '@/store';
import { logout } from '@/store/authSlice';
import type { ApiError } from './types';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8090';

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});


api.interceptors.request.use((config) => {
  const token = store.getState().auth.token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      const url = error.config?.url ?? '';
      if (!url.includes('/auth/login')) {
        store.dispatch(logout());
      }
    }
    return Promise.reject(error);
  },
);

export function extractErrorMessage(error: unknown, fallback = 'Something went wrong'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiError | undefined;
    if (data?.fieldErrors && Object.keys(data.fieldErrors).length) {
      return Object.entries(data.fieldErrors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join('; ');
    }
    return data?.message || error.message || fallback;
  }
  if (error instanceof Error) return error.message;
  return fallback;
}
