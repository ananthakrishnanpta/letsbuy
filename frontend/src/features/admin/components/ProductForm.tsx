import { FormEvent, useState } from "react";
import type { Category } from "@/features/catalog/types";
import type { ProductInput } from "@/features/admin/api/adminApi";
import { TextArea, TextField } from "@/shared/components/TextField";

export function ProductForm({ categories, onSubmit, pending }: { categories: Category[]; onSubmit: (input: ProductInput) => Promise<void>; pending: boolean }) {
  const [status, setStatus] = useState("ACTIVE");
  const [categoryId, setCategoryId] = useState(categories[0]?.id ?? "");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await onSubmit({
      categoryId,
      sku: String(form.get("sku")),
      name: String(form.get("name")),
      description: String(form.get("description")),
      price: String(form.get("price")),
      stockQuantity: Number(form.get("stockQuantity")),
      status,
    });
    event.currentTarget.reset();
  }

  return (
    <form className="stack" onSubmit={(event) => void submit(event)}>
      <TextField label="Name" name="name" required />
      <TextField label="SKU" name="sku" required />
      <TextArea label="Description" name="description" required />
      <TextField label="Price in rupees" name="price" inputMode="decimal" required />
      <TextField label="Stock" name="stockQuantity" type="number" min={0} required />
      <label className="field">
        <span>Category</span>
        <select value={categoryId} onChange={(event) => setCategoryId(event.target.value)}>
          {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
        </select>
      </label>
      <label className="field">
        <span>Status</span>
        <select value={status} onChange={(event) => setStatus(event.target.value)}>
          <option>ACTIVE</option>
          <option>DRAFT</option>
          <option>ARCHIVED</option>
        </select>
      </label>
      <button className="button" type="submit" disabled={pending || !categoryId}>Save product</button>
    </form>
  );
}
