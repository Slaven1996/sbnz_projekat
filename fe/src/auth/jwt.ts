
export interface JwtPayload {
  sub?: string;
  exp?: number;
  iat?: number;
  [key: string]: unknown;
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const payload = token.split('.')[1];
    if (!payload) return null;

    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    );
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

export function isTokenExpired(token: string): boolean {
  const payload = decodeJwt(token);
  if (!payload?.exp) return false;
  return payload.exp * 1000 <= Date.now();
}

export function emailFromToken(token: string): string | null {
  return decodeJwt(token)?.sub ?? null;
}
