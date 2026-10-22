import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { orderingApi } from "@/features/ordering/api/orderingApi";
import { useSession } from "@/features/auth/hooks/useSession";

export function useAddresses() {
  const { user } = useSession();
  return useQuery({ queryKey: ["addresses"], queryFn: orderingApi.addresses, enabled: Boolean(user) });
}

export function useOrders() {
  const { user } = useSession();
  return useQuery({ queryKey: ["orders"], queryFn: orderingApi.orders, enabled: Boolean(user) });
}

export function useOrder(id: string) {
  return useQuery({ queryKey: ["order", id], queryFn: () => orderingApi.order(id) });
}

export function useCancelOrder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: orderingApi.cancel,
    onSuccess: (order) => {
      void queryClient.invalidateQueries({ queryKey: ["orders"] });
      void queryClient.invalidateQueries({ queryKey: ["order", order.id] });
      void queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
}
