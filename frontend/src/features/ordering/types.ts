export type Address = {
  id: string | null;
  fullName: string;
  line1: string;
  line2: string | null;
  city: string;
  region: string;
  postalCode: string;
  countryCode: string;
  phone: string;
  defaultAddress: boolean;
};

export type AddressInput = {
  fullName: string;
  line1: string;
  line2?: string;
  city: string;
  region: string;
  postalCode: string;
  countryCode: string;
  phone: string;
  defaultAddress: boolean;
};

export type OrderItem = {
  productId: string;
  sku: string;
  productName: string;
  unitPriceMinor: number;
  quantity: number;
  lineTotalMinor: number;
};

export type Order = {
  id: string;
  orderNumber: string;
  status: string;
  currency: string;
  subtotalMinor: number;
  shippingMinor: number;
  totalMinor: number;
  paymentMethod: string | null;
  paymentStatus: string | null;
  shippingAddress: Address;
  items: OrderItem[];
  createdAt: string;
};
