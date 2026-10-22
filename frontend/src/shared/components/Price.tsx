import { formatMoney } from "@/shared/lib/money";

export function Price({ minor, currency }: { minor: number; currency: string }) {
  return <span className="price">{formatMoney(minor, currency)}</span>;
}
