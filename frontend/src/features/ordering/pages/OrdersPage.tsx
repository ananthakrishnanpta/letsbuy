import { OrderSummary } from "@/features/ordering/components/OrderSummary";
import { useOrders } from "@/features/ordering/hooks/useOrders";

export function OrdersPage() {
  const orders = useOrders();
  if (orders.isLoading) return <p className="meta">Loading orders…</p>;
  return (
    <div className="stack">
      <h1 className="page-title">Orders</h1>
      {orders.data?.items.length === 0 ? <p className="empty">You have not placed an order yet.</p> : null}
      {orders.data?.items.map((order) => <OrderSummary key={order.id} order={order} />)}
    </div>
  );
}
