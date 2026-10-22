import { useSession } from "@/features/auth/hooks/useSession";

export function AccountPage() {
  const { user } = useSession();
  if (!user) return null;
  return (
    <div className="stack">
      <h1 className="page-title">{user.firstName} {user.lastName}</h1>
      <p>{user.email}</p>
      <p className="meta">Roles: {user.roles.join(", ")}</p>
      <p className="meta">{user.emailVerified ? "Email verified." : "Email not verified yet."}</p>
    </div>
  );
}
