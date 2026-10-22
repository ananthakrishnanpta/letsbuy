import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { authApi } from "@/features/auth/api/authApi";
import { AuthForm, EmailPasswordFields } from "@/features/auth/components/AuthForm";
import { useSession } from "@/features/auth/hooks/useSession";

export function LoginPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const { setUser } = useSession();
  return (
    <AuthForm
      title="Sign in"
      submitLabel="Sign in"
      onSubmit={async (form) => {
        const session = await authApi.login(String(form.get("email")), String(form.get("password")));
        setUser(session.user);
        navigate(params.get("next") || "/");
      }}
    >
      <EmailPasswordFields />
      <p className="meta"><Link to="/forgot-password">Forgot password</Link> · <Link to="/register">Create an account</Link></p>
    </AuthForm>
  );
}
