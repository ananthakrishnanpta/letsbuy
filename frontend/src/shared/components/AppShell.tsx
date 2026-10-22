import { Link, Outlet, useNavigate, useSearchParams } from "react-router-dom";
import { FormEvent, useState } from "react";
import { useSession } from "@/features/auth/hooks/useSession";
import { authApi } from "@/features/auth/api/authApi";
import { useCart } from "@/features/cart/hooks/useCart";

export function AppShell() {
  const { user, status, setUser } = useSession();
  const cart = useCart();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [query, setQuery] = useState(params.get("q") ?? "");
  const count = cart.data?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;

  function onSearch(event: FormEvent) {
    event.preventDefault();
    const next = query.trim();
    navigate(next ? `/?q=${encodeURIComponent(next)}` : "/");
  }

  async function logout() {
    await authApi.logout();
    setUser(null);
    navigate("/");
  }

  return (
    <div className="shell">
      <div className="announcement">Complimentary shipping on orders over ₹999</div>
      <header className="header">
        <Link className="wordmark" to="/">Letsbuy</Link>
        <form className="search" onSubmit={onSearch}>
          <input
            aria-label="Search products"
            placeholder="Search the shop"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </form>
        <nav className="nav">
          <Link to="/cart">Cart{count ? ` (${count})` : ""}</Link>
          {status === "loading" ? <span className="meta">…</span> : null}
          {status !== "loading" && user ? <Link to="/orders">Orders</Link> : null}
          {user?.roles.includes("ADMIN") ? <Link to="/admin/products">Studio</Link> : null}
          {status !== "loading" && user ? <Link to="/account">{user.firstName}</Link> : null}
          {status !== "loading" && !user ? <Link to="/login">Sign in</Link> : null}
          {status !== "loading" && user ? <button className="ghost" onClick={() => void logout()}>Log out</button> : null}
        </nav>
      </header>
      <main className="main">
        <Outlet />
      </main>
      <p className="footer-note">Letsbuy keeps prices in paise, sessions in rotating refresh tokens, and card numbers out of this system.</p>
    </div>
  );
}
