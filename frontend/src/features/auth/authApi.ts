import { httpClient } from "../../api/httpClient";
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from "./authTypes";

export function login(request: LoginRequest): Promise<LoginResponse> {
  return httpClient<LoginResponse>("/api/v1/auth/login", {
    method: "POST",
    body: request,
  });
}

export function register(
  request: RegisterRequest,
): Promise<RegisterResponse> {
  return httpClient<RegisterResponse>("/api/v1/auth/register", {
    method: "POST",
    body: request,
  });
}