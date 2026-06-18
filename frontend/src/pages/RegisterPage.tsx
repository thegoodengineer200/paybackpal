import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { Button } from "../components/Button";
import { Card, CardContent } from "../components/Card";
import { ErrorState } from "../components/ErrorState";
import { Input } from "../components/Input";
import { register } from "../features/auth/authApi";

export function RegisterPage() {
  const navigate = useNavigate();

  const [name, setName] = useState("Alice Bob");
  const [email, setEmail] = useState("alice@example.com");
  const [phoneNumber, setPhoneNumber] = useState("9876543210");
  const [upiId, setUpiId] = useState("alice@upi");
  const [password, setPassword] = useState("password123");

  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      await register({
        name,
        email,
        phoneNumber,
        upiId: upiId.trim() || undefined,
        password,
      });

      navigate("/login", { replace: true });
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Unable to create account. Please try again.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-50 px-4 py-10">
      <Card className="w-full max-w-md">
        <CardContent className="p-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Create PayBackPal account
            </h1>
            <p className="mt-2 text-sm text-gray-500">
              Add your profile details to start tracking repayments.
            </p>
          </div>

          {errorMessage && (
            <div className="mt-6">
              <ErrorState message={errorMessage} />
            </div>
          )}

          <form onSubmit={handleSubmit} className="mt-6 space-y-5">
            <Input
              label="Name"
              type="text"
              required
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Alice Bob"
            />

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
              label="Phone number"
              type="tel"
              required
              value={phoneNumber}
              onChange={(event) => setPhoneNumber(event.target.value)}
              placeholder="9876543210"
            />

            <Input
              label="UPI ID"
              type="text"
              value={upiId}
              onChange={(event) => setUpiId(event.target.value)}
              placeholder="alice@upi"
            />

            <Input
              label="Password"
              type="password"
              required
              autoComplete="new-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="password123"
            />

            <Button
              type="submit"
              isLoading={isSubmitting}
              className="w-full"
            >
              Create account
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-gray-600">
            Already have an account?{" "}
            <Link
              to="/login"
              className="font-semibold text-gray-900 hover:underline"
            >
              Login
            </Link>
          </p>
        </CardContent>
      </Card>
    </main>
  );
}