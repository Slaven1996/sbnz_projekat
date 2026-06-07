import { useDispatch, useSelector, type TypedUseSelectorHook } from 'react-redux';
import type { AppDispatch, RootState } from './index';

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

export const useAuth = () => useAppSelector((s) => s.auth);
export const useIsAdmin = () => useAppSelector((s) => s.auth.role === 'ADMIN');
export const useIsAuthenticated = () => useAppSelector((s) => Boolean(s.auth.token));
