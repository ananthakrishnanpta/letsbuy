import { Link } from "react-router-dom";
import type { CartLine as Line } from "@/features/cart/types";
import { Price } from "@/shared/components/Price";

export function CartLine({
  line,
  onQuantity,
  onRemove,
}: {
  line: Line;
  onQuantity: (quantity: number) => void;
  onRemove: () => void;
}) {
  return (
    <div className="line">
      <div>
        <Link to={`/products/${line.slug}`}><strong>{line.name}</strong></Link>
        <p className="meta">{line.available ? <Price minor={line.unitPriceMinor} currency={line.currency} /> : "Unavailable"}</p>
        <div className="qty">
          <button className="ghost" type="button" onClick={() => onQuantity(line.quantity - 1)} disabled={line.quantity <= 1}>-</button>
          <span>{line.quantity}</span>
          <button className="ghost" type="button" onClick={() => onQuantity(line.quantity + 1)} disabled={line.quantity >= Math.min(99, line.stockQuantity)}>+</button>
          <button className="text-link" type="button" onClick={onRemove}>Remove</button>
        </div>
      </div>
      <Price minor={line.lineTotalMinor} currency={line.currency} />
    </div>
  );
}
