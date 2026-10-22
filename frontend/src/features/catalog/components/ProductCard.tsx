import { Link } from "react-router-dom";
import type { Product } from "@/features/catalog/types";
import { Price } from "@/shared/components/Price";
import { hueFrom } from "@/shared/lib/money";

export function ProductCard({ product }: { product: Product }) {
  return (
    <Link className="card" to={`/products/${product.slug}`}>
      <div className="swatch" style={{ background: swatch(product.slug) }} />
      <div className="card-body">
        <p className="meta">{product.categoryName}</p>
        <h2>{product.name}</h2>
        <Price minor={product.priceMinor} currency={product.currency} />
      </div>
    </Link>
  );
}

export function swatch(seed: string) {
  const hue = hueFrom(seed);
  return `linear-gradient(155deg, hsl(${hue} 42% 46%), hsl(${(hue + 28) % 360} 28% 22%))`;
}
