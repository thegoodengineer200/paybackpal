export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  userId: string;
  name: string;
  email: string;
};

export type RegisterRequest = {
  name: string;
  email: string;
  phoneNumber: string;
  upiId?: string;
  password: string;
};

export type RegisterResponse = {
  id: string;
  name: string;
  email: string;
  phoneNumber: string;
  upiId?: string | null;
};