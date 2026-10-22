import { api, setAccessToken } from "@/shared/api/http";
import type { AuthResponse } from "@/features/auth/types";

export type RegisterInput = {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
};

export const authApi = {
  register(input: RegisterInput) {
    return api<AuthResponse>("/api/v1/auth/register", { method: "POST", body: JSON.stringify(input) }).then(store);
  },
  login(email: string, password: string) {
    return api<AuthResponse>("/api/v1/auth/login", { method: "POST", body: JSON.stringify({ email, password }) }).then(store);
  },
  logout() {
    return api<void>("/api/v1/auth/logout", { method: "POST" }).finally(() => setAccessToken(null));
  },
  forgot(email: string) {
    return api<void>("/api/v1/auth/forgot-password", { method: "POST", body: JSON.stringify({ email }) });
  },
  reset(token: string, password: string) {
    return api<void>("/api/v1/auth/reset-password", { method: "POST", body: JSON.stringify({ token, password }) });
  },
  verify(token: string) {
    return api<void>("/api/v1/auth/verify-email", { method: "POST", body: JSON.stringify({ token }) });
  },
};

function store(session: AuthResponse) {
  setAccessToken(session.accessToken);
  return session;
}
