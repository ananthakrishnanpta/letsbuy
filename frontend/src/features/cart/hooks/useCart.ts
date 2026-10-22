import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { cartApi } from "@/features/cart/api/cartApi";
import { useSession } from "@/features/auth/hooks/useSession";

export function useCart() {
  const { user } = useSession();
  return useQuery({ queryKey: ["cart"], queryFn: cartApi.get, enabled: Boolean(user) });
}

export function useCartMutations() {
  const queryClient = useQueryClient();
  const refresh = {
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["cart"] });
      void queryClient.invalidateQueries({ queryKey: ["products"] });
      void queryClient.invalidateQueries({ queryKey: ["product"] });
    },
  };
  return {
    add: useMutation({ mutationFn: (input: { productId: string; quantity: number }) => cartApi.add(input.productId, input.quantity), ...refresh }),
    update: useMutation({ mutationFn: (input: { itemId: string; quantity: number }) => cartApi.update(input.itemId, input.quantity), ...refresh }),
    remove: useMutation({ mutationFn: (itemId: string) => cartApi.remove(itemId), ...refresh }),
  };
}
