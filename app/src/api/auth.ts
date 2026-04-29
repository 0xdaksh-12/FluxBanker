import { apiClient } from "./client";
import { AuthResponse, User } from "../types";

export const login = async (
  email: string,
  password: string,
): Promise<AuthResponse> => {
  const { data: authData } = await apiClient.post<{
    token: string;
    name?: string;
  }>("/auth/login", {
    email,
    password,
  });

  const { data: userData } = await apiClient.get<User>("/users/me", {
    headers: { Authorization: `Bearer ${authData.token}` },
  });

  return {
    accessToken: authData.token,
    user: userData,
  };
};

export const register = async (
  firstName: string,
  lastName: string,
  email: string,
  password: string,
  dateOfBirth: string,
  aadhaar: string,
  address1: string,
  city: string,
  state: string,
  pinCode: string,
): Promise<AuthResponse> => {
  const { data: authData } = await apiClient.post<{ token: string }>(
    "/auth/register",
    {
      firstName,
      lastName,
      email,
      password,
      dateOfBirth,
      aadhaar,
      address1,
      city,
      state,
      pinCode,
    },
  );

  const { data: userData } = await apiClient.get<User>("/users/me", {
    headers: { Authorization: `Bearer ${authData.token}` },
  });

  return {
    accessToken: authData.token,
    user: userData,
  };
};

export const refresh = async (): Promise<AuthResponse | null> => {
  try {
    const { data: authData } = await apiClient.post<{ token: string }>(
      "/auth/refresh",
    );

    if (!authData.token) return null;

    const { data: userData } = await apiClient.get<User>("/users/me", {
      headers: { Authorization: `Bearer ${authData.token}` },
    });

    return {
      accessToken: authData.token,
      user: userData,
    };
  } catch {
    return null;
  }
};

export const logout = async (): Promise<void> => {
  await apiClient.post("/auth/logout");
};
