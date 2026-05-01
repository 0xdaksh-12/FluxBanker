import { apiClient } from "./client";
import type { User } from "../types";

export const updateProfile = async (data: Partial<User>): Promise<User> => {
  const { data: userData } = await apiClient.patch<User>("/users/me", data);
  return userData;
};

export const verifyEmail = async (): Promise<User> => {
  const { data: userData } = await apiClient.post<User>(
    "/users/me/verify-email",
  );
  return userData;
};

export const requestPasswordReset = async (): Promise<void> => {
  await apiClient.post("/users/me/password-reset");
};

export const getMe = async (): Promise<User> => {
  const { data } = await apiClient.get<User>("/users/me");
  return data;
};
