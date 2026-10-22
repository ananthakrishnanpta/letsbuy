import { useSearchParams } from "react-router-dom";
import { ProductCard } from "@/features/catalog/components/ProductCard";
import { useCategories, useProducts } from "@/features/catalog/hooks/useCatalog";
import { useDebouncedValue } from "@/shared/hooks/useDebouncedValue";

export function CatalogPage() {
  const [params, setParams] = useSearchParams();
  const category = params.get("category") ?? "";
  const query = useDebouncedValue(params.get("q") ?? "");
  const categories = useCategories();
  const products = useProducts(category, query);

  function choose(slug: string) {
    const next = new URLSearchParams(params);
    if (slug) next.set("category", slug);
    else next.delete("category");
    setParams(next);
  }

  return (
    <>
      <section className="hero">
        <h1>Goods for a slower house.</h1>
        <p className="lede">Kitchen, home, and weekend pieces. Prices include nothing you did not ask for, and shipping drops off at ₹999.</p>
      </section>
      <div className="chips">
        <button className={category ? "chip" : "chip active"} onClick={() => choose("")}>All</button>
        {categories.data?.map((item) => (
          <button key={item.id} className={category === item.slug ? "chip active" : "chip"} onClick={() => choose(item.slug)}>
            {item.name}
          </button>
        ))}
      </div>
      {products.isLoading ? <p className="meta">Loading the shop…</p> : null}
      {products.isError ? <p className="error">The catalogue could not be loaded.</p> : null}
      {products.data && products.data.items.length === 0 ? <p className="empty">Nothing matches that search.</p> : null}
      <div className="grid">
        {products.data?.items.map((product) => <ProductCard key={product.id} product={product} />)}
      </div>
    </>
  );
}
