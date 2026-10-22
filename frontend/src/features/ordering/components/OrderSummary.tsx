import { Link } from "react-router-dom";
import type { Order } from "@/features/ordering/types";
import { Price } from "@/shared/components/Price";

export function OrderSummary({ order }: { order: Order }) {
  return (
    <article className="card panel">
      <Link to={`/orders/${order.id}`}><h2>{order.orderNumber}</h2></Link>
      <p className="meta">{order.status} · {order.paymentMethod} {order.paymentStatus}</p>
      <Price minor={order.totalMinor} currency={order.currency} />
    </article>
  );
}
