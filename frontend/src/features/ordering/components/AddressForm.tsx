import { FormEvent, useState } from "react";
import { TextField } from "@/shared/components/TextField";
import type { AddressInput } from "@/features/ordering/types";

const empty: AddressInput = {
  fullName: "",
  line1: "",
  line2: "",
  city: "",
  region: "",
  postalCode: "",
  countryCode: "IN",
  phone: "",
  defaultAddress: true,
};

export function AddressForm({ onSubmit, pending }: { onSubmit: (input: AddressInput) => Promise<void>; pending: boolean }) {
  const [form, setForm] = useState(empty);

  function update(key: keyof AddressInput, value: string | boolean) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    await onSubmit(form);
    setForm(empty);
  }

  return (
    <form className="stack" onSubmit={(event) => void submit(event)}>
      <TextField label="Full name" value={form.fullName} onChange={(event) => update("fullName", event.target.value)} required />
      <TextField label="Address line" value={form.line1} onChange={(event) => update("line1", event.target.value)} required />
      <TextField label="City" value={form.city} onChange={(event) => update("city", event.target.value)} required />
      <TextField label="State" value={form.region} onChange={(event) => update("region", event.target.value)} required />
      <TextField label="Postal code" value={form.postalCode} onChange={(event) => update("postalCode", event.target.value)} required />
      <TextField label="Phone" value={form.phone} onChange={(event) => update("phone", event.target.value)} required />
      <button className="button" type="submit" disabled={pending}>Save address</button>
    </form>
  );
}
