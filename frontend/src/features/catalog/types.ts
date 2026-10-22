export type Category = {
  id: string;
  name: string;
  slug: string;
  description: string | null;
};

export type Product = {
  id: string;
  sku: string;
  name: string;
  slug: string;
  description: string;
  priceMinor: number;
  currency: string;
  stockQuantity: number;
  status: string;
  imageUrl: string | null;
  categorySlug: string;
  categoryName: string;
};

export type Page<T> = {
  items: T[];
  page: number;
  size: number;
  total: number;
  hasNext: boolean;
};
