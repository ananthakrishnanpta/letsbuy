import { useQuery } from "@tanstack/react-query";
import { catalogApi } from "@/features/catalog/api/catalogApi";

export function useCategories() {
  return useQuery({ queryKey: ["categories"], queryFn: catalogApi.categories });
}

export function useProducts(category: string, query: string) {
  return useQuery({
    queryKey: ["products", category, query],
    queryFn: () => catalogApi.products({ category, q: query }),
  });
}

export function useProduct(slug: string) {
  return useQuery({ queryKey: ["product", slug], queryFn: () => catalogApi.product(slug) });
}
