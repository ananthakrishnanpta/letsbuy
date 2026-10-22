import { Link } from "react-router-dom";
import { ApiError } from "@/shared/api/http";
import { CartLine } from "@/features/cart/components/CartLine";
import { useCart, useCartMutations } from "@/features/cart/hooks/useCart";
import { Price } from "@/shared/components/Price";
import { useState } from "react";

export function CartPage() {
  const cart = useCart();
  const mutations = useCartMutations();
  const [error, setError] = useState<string | null>(null);

  async function run(action: Promise<unknown>) {
    setError(null);
    try {
      await action;
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Could not update the cart.");
    }
  }

  if (cart.isLoading) return <p className="meta">Loading your cart…</p>;
  const data = cart.data;
  if (!data || data.items.length === 0) {
    return (
      <div className="stack">
        <h1 className="page-title">Your cart is empty.</h1>
        <Link className="button" to="/">Browse the shop</Link>
      </div>
    );
  }

  return (
    <div className="split">
      <section>
        <h1 className="page-title">Cart</h1>
        {data.items.map((line) => (
          <CartLine
            key={line.itemId}
            line={line}
            onQuantity={(quantity) => void run(mutations.update.mutateAsync({ itemId: line.itemId, quantity }))}
            onRemove={() => void run(mutations.remove.mutateAsync(line.itemId))}
          />
        ))}
        {error ? <p className="error">{error}</p> : null}
      </section>
      <aside className="card panel">
        <h2>Summary</h2>
        <p>Subtotal <Price minor={data.subtotalMinor} currency={data.currency} /></p>
        <p className="meta">{data.subtotalMinor >= 99900 ? "Shipping is complimentary." : "Shipping is ₹49, unless the order reaches ₹999."}</p>
        <Link className="button" to="/checkout">Checkout</Link>
      </aside>
    </div>
  );
}
