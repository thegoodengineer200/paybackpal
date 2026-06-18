import { useState, type FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router";
import { Button } from "../components/Button";
import { Card, CardContent } from "../components/Card";
import { ErrorState } from "../components/ErrorState";
import { Input } from "../components/Input";
import { login } from "../features/auth/authApi";
import { useAuth } from "../features/auth/AuthContext";

type LocationState = {
  from?: string;
};

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { loginWithToken } = useAuth();

  const locationState = location.state as LocationState | null;
  const redirectTo = locationState?.from ?? "/dashboard";

  const [email, setEmail] = useState("alice@example.com");
  const [password, setPassword] = useState("password123");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      const response = await login({ email, password });

      loginWithToken(response.token);
      navigate(redirectTo, { replace: true });
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Unable to login. Please try again.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <Card className="w-full max-w-md">
        <CardContent className="p-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Login to PayBackPal
            </h1>
            <p className="mt-2 text-sm text-gray-500">
              Track borrowed credit card expenses and repayments.
            </p>
          </div>

          {errorMessage && (
            <div className="mt-6">
              <ErrorState message={errorMessage} />
            </div>
          )}

          <form onSubmit={handleSubmit} className="mt-6 space-y-5">
            <Input
              label="Email"
              type="email"
              required
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="alice@example.com"
            />

            <Input
              label="Password"
              type="password"
              required
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="password123"
            />

            <Button
              type="submit"
              isLoading={isSubmitting}
              className="w-full"
            >
              Login
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-600">
            New to PayBackPal?{" "}
            <Link
              to="/register"
              className="font-semibold text-gray-900 hover:underline"
            >
              Create account
            </Link>
          </p>
        </CardContent>
      </Card>
    </main>
  );
}