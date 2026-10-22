import { api } from "@/shared/api/http";
import type { Page, Product } from "@/features/catalog/types";

export type ProductInput = {
  categoryId: string;
  sku: string;
  name: string;
  description: string;
  price: string;
  stockQuantity: number;
  status: string;
  imageUrl?: string;
};

export const adminApi = {
  products() {
    return api<Page<Product>>("/api/v1/admin/products?size=48");
  },
  create(input: ProductInput) {
    return api<Product>("/api/v1/admin/products", { method: "POST", body: JSON.stringify(input) });
  },
  update(id: string, input: ProductInput) {
    return api<Product>(`/api/v1/admin/products/${id}`, { method: "PUT", body: JSON.stringify(input) });
  },
  fulfill(orderId: string) {
    return api<unknown>(`/api/v1/admin/orders/${orderId}/fulfill`, { method: "POST" });
  },
};
