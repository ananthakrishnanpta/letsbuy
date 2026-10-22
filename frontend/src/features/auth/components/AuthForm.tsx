import { FormEvent, useState, type ReactNode } from "react";
import { ApiError } from "@/shared/api/http";
import { TextField } from "@/shared/components/TextField";

export function AuthForm({
  title,
  submitLabel,
  children,
  onSubmit,
}: {
  title: string;
  submitLabel: string;
  children?: ReactNode;
  onSubmit: (form: FormData) => Promise<void>;
}) {
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setPending(true);
    try {
      await onSubmit(new FormData(event.currentTarget));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Something went wrong.");
    } finally {
      setPending(false);
    }
  }

  return (
    <form className="card panel stack" onSubmit={(event) => void submit(event)} style={{ maxWidth: 440 }}>
      <h1 className="page-title" style={{ fontSize: "2.4rem" }}>{title}</h1>
      {children}
      {error ? <p className="banner">{error}</p> : null}
      <button className="button" type="submit" disabled={pending}>{submitLabel}</button>
    </form>
  );
}

export function EmailPasswordFields({ includeName = false }: { includeName?: boolean }) {
  return (
    <>
      {includeName ? (
        <>
          <TextField label="First name" name="firstName" required />
          <TextField label="Last name" name="lastName" required />
        </>
      ) : null}
      <TextField label="Email" name="email" type="email" autoComplete="email" required />
      <TextField label="Password" name="password" type="password" autoComplete={includeName ? "new-password" : "current-password"} required minLength={10} />
    </>
  );
}
