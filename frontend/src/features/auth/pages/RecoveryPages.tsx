import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { authApi } from "@/features/auth/api/authApi";
import { AuthForm } from "@/features/auth/components/AuthForm";
import { TextField } from "@/shared/components/TextField";

export function ForgotPasswordPage() {
  const [sent, setSent] = useState(false);
  if (sent) return <p className="banner">If that email has an account, a reset link is on its way. In local mode the link is printed in the API log.</p>;
  return (
    <AuthForm title="Reset password" submitLabel="Send link" onSubmit={async (form) => {
      await authApi.forgot(String(form.get("email")));
      setSent(true);
    }}>
      <TextField label="Email" name="email" type="email" required />
    </AuthForm>
  );
}

export function ResetPasswordPage() {
  const [params] = useSearchParams();
  const [done, setDone] = useState(false);
  if (done) return <p className="banner">Password updated. <Link to="/login">Sign in</Link></p>;
  return (
    <AuthForm title="Choose a new password" submitLabel="Update password" onSubmit={async (form) => {
      await authApi.reset(params.get("token") ?? "", String(form.get("password")));
      setDone(true);
    }}>
      <TextField label="New password" name="password" type="password" minLength={10} required />
    </AuthForm>
  );
}

export function VerifyEmailPage() {
  const [params] = useSearchParams();
  const token = params.get("token");
  const [done, setDone] = useState(false);
  const [error, setError] = useState<string | null>(null);
  if (!token) return <p className="banner">Check your inbox for a verification link. In local mode it is printed in the API log.</p>;
  if (done) return <p className="banner">Email verified. <Link to="/login">Sign in</Link></p>;
  return (
    <div className="stack">
      <h1 className="page-title">Verify email</h1>
      {error ? <p className="error">{error}</p> : null}
      <button className="button" type="button" onClick={() => {
        authApi.verify(token).then(() => setDone(true)).catch(() => setError("This link is invalid or expired."));
      }}>Confirm email</button>
    </div>
  );
}
