import type { AuthResponse, User } from "@/features/auth/types";

export class ApiError extends Error {
  status: number;
  code: string;
  fields: Record<string, string>;

  constructor(status: number, code: string, message: string, fields?: Record<string, string>) {
    super(message);
    this.status = status;
    this.code = code;
    this.fields = fields ?? {};
  }
}

let accessToken: string | null = null;
let refreshInFlight: Promise<User | null> | null = null;
let onSession: (user: User | null) => void = () => undefined;

export function setAccessToken(token: string | null) {
  accessToken = token;
}

export function bindSession(listener: (user: User | null) => void) {
  onSession = listener;
}

type Options = RequestInit & { retry?: boolean };

export async function api<T>(path: string, options: Options = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("X-Letsbuy-Request", "1");
  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }
  const response = await fetch(path, { ...options, headers, credentials: "include" });
  if (response.status === 401 && !options.retry && !isAuthEntry(path)) {
    const user = await refreshSession();
    if (user) {
      return api<T>(path, { ...options, retry: true });
    }
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  let payload: { data?: T; code?: string; message?: string; fields?: Record<string, string> } | null = null;
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = null;
    }
  }
  if (!response.ok) {
    throw new ApiError(response.status, payload?.code ?? "ERROR", payload?.message ?? "Request failed", payload?.fields);
  }
  return (payload?.data ?? undefined) as T;
}

export function refreshSession(): Promise<User | null> {
  if (!refreshInFlight) {
    refreshInFlight = loadSession().finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
}

async function loadSession(): Promise<User | null> {
  const response = await fetch("/api/v1/auth/refresh", {
    method: "POST",
    credentials: "include",
    headers: { "X-Letsbuy-Request": "1" },
  });
  if (!response.ok) {
    accessToken = null;
    onSession(null);
    return null;
  }
  const payload = (await response.json()) as { data: AuthResponse };
  accessToken = payload.data.accessToken;
  onSession(payload.data.user);
  return payload.data.user;
}

function isAuthEntry(path: string) {
  return ["/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh", "/api/v1/auth/logout"].some((entry) => path.startsWith(entry));
}
