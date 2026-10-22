import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useSession } from "@/features/auth/hooks/useSession";

export function ProtectedRoute({ children, admin = false }: { children: ReactNode; admin?: boolean }) {
  const { user, status } = useSession();
  const location = useLocation();
  if (status === "loading") {
    return <p className="meta">Checking your session…</p>;
  }
  if (!user) {
    return <Navigate to={`/login?next=${encodeURIComponent(location.pathname)}`} replace />;
  }
  if (admin && !user.roles.includes("ADMIN")) {
    return <Navigate to="/" replace />;
  }
  return children;
}
