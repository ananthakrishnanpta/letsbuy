import { useParams } from "react-router-dom";
import { ApiError } from "@/shared/api/http";
import { useCancelOrder, useOrder } from "@/features/ordering/hooks/useOrders";
import { Price } from "@/shared/components/Price";
import { useState } from "react";

export function OrderDetailPage() {
  const { id = "" } = useParams();
  const order = useOrder(id);
  const cancel = useCancelOrder();
  const [error, setError] = useState<string | null>(null);
  if (order.isLoading) return <p className="meta">Loading order…</p>;
  if (!order.data) return <p className="empty">Order not found.</p>;
  const data = order.data;
  const address = data.shippingAddress;

  async function onCancel() {
    setError(null);
    try {
      await cancel.mutateAsync(data.id);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Could not cancel.");
    }
  }

  return (
    <div className="stack">
      <h1 className="page-title">{data.orderNumber}</h1>
      <p className="meta">{data.status} · Payment {data.paymentStatus} via {data.paymentMethod}</p>
      {data.items.map((item) => (
        <div className="line" key={item.sku}>
          <div>
            <strong>{item.productName}</strong>
            <p className="meta">{item.quantity} × <Price minor={item.unitPriceMinor} currency={data.currency} /></p>
          </div>
          <Price minor={item.lineTotalMinor} currency={data.currency} />
        </div>
      ))}
      <p>Shipping <Price minor={data.shippingMinor} currency={data.currency} /></p>
      <p>Total <Price minor={data.totalMinor} currency={data.currency} /></p>
      <p>{address.fullName}, {address.line1}, {address.city} {address.postalCode}</p>
      {data.status === "PLACED" ? <button className="ghost" type="button" onClick={() => void onCancel()}>Cancel order</button> : null}
      {error ? <p className="error">{error}</p> : null}
    </div>
  );
}
