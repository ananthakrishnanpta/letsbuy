import { api } from "@/shared/api/http";
import type { Cart } from "@/features/cart/types";

export const cartApi = {
  get() {
    return api<Cart>("/api/v1/cart");
  },
  add(productId: string, quantity: number) {
    return api<Cart>("/api/v1/cart/items", { method: "POST", body: JSON.stringify({ productId, quantity }) });
  },
  update(itemId: string, quantity: number) {
    return api<Cart>(`/api/v1/cart/items/${itemId}`, { method: "PATCH", body: JSON.stringify({ quantity }) });
  },
  remove(itemId: string) {
    return api<Cart>(`/api/v1/cart/items/${itemId}`, { method: "DELETE" });
  },
};
