import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  updateProfile,
  verifyEmail,
  requestPasswordReset,
  getMe,
} from "../api/users";
import { useAuthStore } from "../store/authStore";
import type { User } from "../types";

export const useUpdateProfile = () => {
  const queryClient = useQueryClient();
  const setAuth = useAuthStore((state) => state.setAuth);
  const accessToken = useAuthStore((state) => state.accessToken);

  return useMutation({
    mutationFn: (data: Partial<User>) => updateProfile(data),
    onSuccess: (updatedUser) => {
      queryClient.invalidateQueries({ queryKey: ["user", "me"] });
      // Update local store with new user data
      if (accessToken) {
        setAuth(updatedUser, accessToken);
      }
    },
  });
};

export const useVerifyEmail = () => {
  const queryClient = useQueryClient();
  const setAuth = useAuthStore((state) => state.setAuth);
  const accessToken = useAuthStore((state) => state.accessToken);

  return useMutation({
    mutationFn: () => verifyEmail(),
    onSuccess: (updatedUser) => {
      queryClient.invalidateQueries({ queryKey: ["user", "me"] });
      if (accessToken) {
        setAuth(updatedUser, accessToken);
      }
    },
  });
};

export const useRequestPasswordReset = () => {
  return useMutation({
    mutationFn: () => requestPasswordReset(),
  });
};

export const useMe = (enabled = true) => {
  return useQuery({
    queryKey: ["user", "me"],
    queryFn: getMe,
    enabled,
  });
};
