import { Link, useNavigate } from "react-router-dom";
import { authApi } from "@/features/auth/api/authApi";
import { AuthForm, EmailPasswordFields } from "@/features/auth/components/AuthForm";
import { useSession } from "@/features/auth/hooks/useSession";

export function RegisterPage() {
  const navigate = useNavigate();
  const { setUser } = useSession();
  return (
    <AuthForm
      title="Create an account"
      submitLabel="Create account"
      onSubmit={async (form) => {
        const session = await authApi.register({
          email: String(form.get("email")),
          password: String(form.get("password")),
          firstName: String(form.get("firstName")),
          lastName: String(form.get("lastName")),
        });
        if (!session.accessToken) {
          navigate("/verify-email");
          return;
        }
        setUser(session.user);
        navigate("/");
      }}
    >
      <EmailPasswordFields includeName />
      <p className="meta">Use at least 10 characters, with a letter and a number. <Link to="/login">Already have an account?</Link></p>
    </AuthForm>
  );
}
