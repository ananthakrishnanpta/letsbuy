import { api } from "@/shared/api/http";
import type { Category, Page, Product } from "@/features/catalog/types";

export const catalogApi = {
  categories() {
    return api<Category[]>("/api/v1/categories");
  },
  products(params: { category?: string; q?: string; page?: number }) {
    const search = new URLSearchParams();
    if (params.category) search.set("category", params.category);
    if (params.q) search.set("q", params.q);
    search.set("page", String(params.page ?? 0));
    return api<Page<Product>>(`/api/v1/products?${search.toString()}`);
  },
  product(slug: string) {
    return api<Product>(`/api/v1/products/${slug}`);
  },
};
