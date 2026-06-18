import { Navigate } from "react-router";
import { useAuth } from "../features/auth/AuthContext";

type PublicOnlyRouteProps = {
  children: React.ReactNode;
};

export function PublicOnlyRoute({ children }: PublicOnlyRouteProps) {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}