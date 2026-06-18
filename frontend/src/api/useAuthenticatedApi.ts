import { useCallback } from "react";
import { useNavigate } from "react-router";
import { httpClient } from "./httpClient";
import { useAuth } from "../features/auth/AuthContext";

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

type AuthenticatedRequestOptions = {
  method?: HttpMethod;
  body?: unknown;
};

export function useAuthenticatedApi() {
  const { token, logout } = useAuth();
  const navigate = useNavigate();

  const request = useCallback(
    async function request<T>(
      path: string,
      options: AuthenticatedRequestOptions = {},
    ): Promise<T> {
      if (!token) {
        logout();
        navigate("/login", { replace: true });
        throw new Error("You are not logged in");
      }

      try {
        return await httpClient<T>(path, {
          ...options,
          token,
        });
      } catch (error) {
        if (
          error instanceof Error &&
          (error.message.includes("status 401") ||
            error.message.includes("status 403"))
        ) {
          logout();
          navigate("/login", { replace: true });
        }

        throw error;
      }
    },
    [token, logout, navigate],
  );

  return { request };
}