export type CartLine = {
  itemId: string;
  productId: string;
  name: string;
  slug: string;
  imageUrl: string | null;
  unitPriceMinor: number;
  currency: string;
  quantity: number;
  stockQuantity: number;
  available: boolean;
  lineTotalMinor: number;
};

export type Cart = {
  items: CartLine[];
  subtotalMinor: number;
  currency: string;
};
