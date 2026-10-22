import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/features/admin/api/adminApi";
import { ProductForm } from "@/features/admin/components/ProductForm";
import { useCategories } from "@/features/catalog/hooks/useCatalog";
import { Price } from "@/shared/components/Price";
import { ApiError } from "@/shared/api/http";

export function AdminProductsPage() {
  const products = useQuery({ queryKey: ["admin-products"], queryFn: adminApi.products });
  const categories = useCategories();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const create = useMutation({
    mutationFn: adminApi.create,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["admin-products"] });
      void queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });

  return (
    <div className="split">
      <section>
        <h1 className="page-title">Studio</h1>
        <table className="table">
          <thead>
            <tr><th>Product</th><th>SKU</th><th>Stock</th><th>Price</th><th>Status</th></tr>
          </thead>
          <tbody>
            {products.data?.items.map((product) => (
              <tr key={product.id}>
                <td>{product.name}</td>
                <td>{product.sku}</td>
                <td>{product.stockQuantity}</td>
                <td><Price minor={product.priceMinor} currency={product.currency} /></td>
                <td>{product.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <aside className="card panel">
        <h2>New product</h2>
        {error ? <p className="error">{error}</p> : null}
        <ProductForm
          categories={categories.data ?? []}
          pending={create.isPending}
          onSubmit={async (input) => {
            setError(null);
            try {
              await create.mutateAsync(input);
            } catch (caught) {
              setError(caught instanceof ApiError ? caught.message : "Could not save the product.");
              throw caught;
            }
          }}
        />
      </aside>
    </div>
  );
}
