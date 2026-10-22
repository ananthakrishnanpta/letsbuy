import type { InputHTMLAttributes, TextareaHTMLAttributes } from "react";

type Props = {
  label: string;
  error?: string;
} & InputHTMLAttributes<HTMLInputElement>;

export function TextField({ label, error, ...props }: Props) {
  return (
    <label className="field">
      <span>{label}</span>
      <input {...props} />
      {error ? <small className="error">{error}</small> : null}
    </label>
  );
}

export function TextArea({ label, error, ...props }: { label: string; error?: string } & TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <label className="field">
      <span>{label}</span>
      <textarea {...props} rows={props.rows ?? 4} />
      {error ? <small className="error">{error}</small> : null}
    </label>
  );
}
