import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { UserRole, UserTokenState } from '@/api/types';
import { decodeJwt, isTokenExpired } from '@/auth/jwt';

const STORAGE_KEY = 'hydro.auth';

export interface AuthState {
  token: string | null;
  userId: number | null;
  email: string | null;
  role: UserRole | null;
  expiresAt: number | null; // ms
}

interface PersistedAuth {
  token: string;
  role: UserRole;
  userId: number;
}

function loadInitialState(): AuthState {
  const empty: AuthState = { token: null, userId: null, email: null, role: null, expiresAt: null };
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return empty;
    const persisted = JSON.parse(raw) as PersistedAuth;
    if (!persisted.token || isTokenExpired(persisted.token)) {
      localStorage.removeItem(STORAGE_KEY);
      return empty;
    }
    const payload = decodeJwt(persisted.token);
    return {
      token: persisted.token,
      userId: persisted.userId ?? null,
      role: persisted.role,
      email: payload?.sub ?? null,
      expiresAt: payload?.exp ? payload.exp * 1000 : null,
    };
  } catch {
    return empty;
  }
}

function persist(token: string, role: UserRole, userId: number) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ token, role, userId } satisfies PersistedAuth));
}

const authSlice = createSlice({
  name: 'auth',
  initialState: loadInitialState(),
  reducers: {
    loginSuccess(state, action: PayloadAction<UserTokenState>) {
      const { access_token, authority, userId } = action.payload;
      const role = (authority as UserRole) ?? null;
      const payload = decodeJwt(access_token);
      state.token = access_token;
      state.userId = userId ?? null;
      state.role = role;
      state.email = payload?.sub ?? null;
      state.expiresAt = payload?.exp ? payload.exp * 1000 : null;
      if (role && userId != null) persist(access_token, role, userId);
    },
    logout(state) {
      state.token = null;
      state.userId = null;
      state.email = null;
      state.role = null;
      state.expiresAt = null;
      localStorage.removeItem(STORAGE_KEY);
    },
  },
});

export const { loginSuccess, logout } = authSlice.actions;
export default authSlice.reducer;
