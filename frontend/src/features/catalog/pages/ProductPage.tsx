import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ApiError } from "@/shared/api/http";
import { useSession } from "@/features/auth/hooks/useSession";
import { useCartMutations } from "@/features/cart/hooks/useCart";
import { swatch } from "@/features/catalog/components/ProductCard";
import { useProduct } from "@/features/catalog/hooks/useCatalog";
import { Price } from "@/shared/components/Price";

export function ProductPage() {
  const { slug = "" } = useParams();
  const product = useProduct(slug);
  const { user } = useSession();
  const navigate = useNavigate();
  const { add } = useCartMutations();
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState<string | null>(null);

  if (product.isLoading) return <p className="meta">Loading…</p>;
  if (product.isError || !product.data) return <p className="empty">That product is not in the shop.</p>;
  const item = product.data;
  const max = Math.min(99, item.stockQuantity);

  async function addToCart() {
    if (!user) {
      navigate(`/login?next=${encodeURIComponent(`/products/${item.slug}`)}`);
      return;
    }
    setMessage(null);
    try {
      await add.mutateAsync({ productId: item.id, quantity });
      setMessage("Added to your cart.");
    } catch (error) {
      setMessage(error instanceof ApiError ? error.message : "Could not add this item.");
    }
  }

  return (
    <article className="product-layout">
      <div className="swatch large" style={{ background: swatch(item.slug) }} />
      <div className="stack">
        <Link className="text-link" to={`/?category=${item.categorySlug}`}>{item.categoryName}</Link>
        <h1 className="page-title">{item.name}</h1>
        <Price minor={item.priceMinor} currency={item.currency} />
        <p>{item.description}</p>
        <p className="meta">{item.stockQuantity > 0 ? `${item.stockQuantity} in stock` : "Out of stock"} · {item.sku}</p>
        <div className="qty">
          <button className="ghost" type="button" onClick={() => setQuantity((value) => Math.max(1, value - 1))}>-</button>
          <span>{quantity}</span>
          <button className="ghost" type="button" onClick={() => setQuantity((value) => Math.min(max || 1, value + 1))}>+</button>
        </div>
        <button className="button" type="button" disabled={item.stockQuantity < 1 || add.isPending} onClick={() => void addToCart()}>
          Add to cart
        </button>
        {message ? <p className="meta">{message}</p> : null}
      </div>
    </article>
  );
}
