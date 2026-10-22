import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { ApiError } from "@/shared/api/http";
import { AddressForm } from "@/features/ordering/components/AddressForm";
import { orderingApi } from "@/features/ordering/api/orderingApi";
import { useAddresses } from "@/features/ordering/hooks/useOrders";
import { useCart } from "@/features/cart/hooks/useCart";
import { Price } from "@/shared/components/Price";

export function CheckoutPage() {
  const addresses = useAddresses();
  const cart = useCart();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState<string>("");
  const [error, setError] = useState<string | null>(null);
  const saveAddress = useMutation({
    mutationFn: orderingApi.createAddress,
    onSuccess: (address) => {
      void queryClient.invalidateQueries({ queryKey: ["addresses"] });
      if (address.id) setSelected(address.id);
    },
  });
  const checkout = useMutation({
    mutationFn: orderingApi.checkout,
    onSuccess: (order) => {
      void queryClient.invalidateQueries({ queryKey: ["cart"] });
      void queryClient.invalidateQueries({ queryKey: ["orders"] });
      navigate(`/orders/${order.id}`);
    },
  });

  const chosen = selected || addresses.data?.find((item) => item.defaultAddress)?.id || addresses.data?.[0]?.id || "";
  const subtotal = cart.data?.subtotalMinor ?? 0;
  const shipping = subtotal >= 99900 || subtotal === 0 ? 0 : 4900;

  async function place() {
    setError(null);
    try {
      await checkout.mutateAsync(chosen);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Checkout failed.");
    }
  }

  return (
    <div className="split">
      <section className="stack">
        <h1 className="page-title">Checkout</h1>
        <p className="meta">Cash on delivery. The payment row stays pending until the order is fulfilled. No card data is stored.</p>
        {addresses.data?.map((address) => (
          <label className="card panel" key={address.id ?? address.line1}>
            <input type="radio" name="address" checked={chosen === address.id} onChange={() => address.id && setSelected(address.id)} />
            <strong>{address.fullName}</strong>
            <span>{address.line1}, {address.city} {address.postalCode}</span>
            <span className="meta">{address.phone}</span>
          </label>
        ))}
        <h2>New address</h2>
        <AddressForm onSubmit={async (input) => { await saveAddress.mutateAsync(input); }} pending={saveAddress.isPending} />
      </section>
      <aside className="card panel">
        <h2>Pay on delivery</h2>
        <p>Items <Price minor={subtotal} currency="INR" /></p>
        <p>Shipping <Price minor={shipping} currency="INR" /></p>
        <p>Total <Price minor={subtotal + shipping} currency="INR" /></p>
        {error ? <p className="error">{error}</p> : null}
        <button className="button" type="button" disabled={!chosen || checkout.isPending || subtotal === 0} onClick={() => void place()}>
          Place order
        </button>
      </aside>
    </div>
  );
}
