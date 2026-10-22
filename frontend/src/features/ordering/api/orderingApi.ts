import { api } from "@/shared/api/http";
import type { Page } from "@/features/catalog/types";
import type { Address, AddressInput, Order } from "@/features/ordering/types";

export const orderingApi = {
  addresses() {
    return api<Address[]>("/api/v1/addresses");
  },
  createAddress(input: AddressInput) {
    return api<Address>("/api/v1/addresses", { method: "POST", body: JSON.stringify(input) });
  },
  checkout(addressId: string) {
    return api<Order>("/api/v1/checkout", {
      method: "POST",
      body: JSON.stringify({ addressId, paymentMethod: "COD" }),
    });
  },
  orders() {
    return api<Page<Order>>("/api/v1/orders");
  },
  order(id: string) {
    return api<Order>(`/api/v1/orders/${id}`);
  },
  cancel(id: string) {
    return api<Order>(`/api/v1/orders/${id}/cancel`, { method: "POST" });
  },
};
