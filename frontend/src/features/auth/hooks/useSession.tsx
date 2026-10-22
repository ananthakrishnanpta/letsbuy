import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { bindSession, refreshSession } from "@/shared/api/http";
import type { User } from "@/features/auth/types";

type Status = "loading" | "anonymous" | "authenticated";

type SessionContextValue = {
  user: User | null;
  status: Status;
  setUser: (user: User | null) => void;
};

const SessionContext = createContext<SessionContextValue | null>(null);

export function SessionProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [status, setStatus] = useState<Status>("loading");

  useEffect(() => {
    bindSession((next) => {
      setUser(next);
      setStatus(next ? "authenticated" : "anonymous");
    });
    let active = true;
    refreshSession().then((next) => {
      if (!active) return;
      setUser(next);
      setStatus(next ? "authenticated" : "anonymous");
    });
    return () => {
      active = false;
    };
  }, []);

  const value = useMemo(() => ({ user, status, setUser }), [user, status]);
  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession() {
  const value = useContext(SessionContext);
  if (!value) {
    throw new Error("useSession must be used inside SessionProvider");
  }
  return value;
}
