import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { getAllUsers, getAllTransactions } from "../api/admin";
import { useAuthStore } from "../store/authStore";

export const useAllUsers = (page = 0, size = 10) => {
  const { user, isAuthenticated } = useAuthStore();
  const isAdmin = isAuthenticated && user?.role === "ADMIN";

  return useQuery({
    queryKey: ["admin", "users", page, size],
    queryFn: () => getAllUsers(page, size),
    enabled: isAdmin,
    placeholderData: keepPreviousData,
  });
};

export const useAllTransactions = (page = 0, size = 50) => {
  const { user, isAuthenticated } = useAuthStore();
  const isAdmin = isAuthenticated && user?.role === "ADMIN";

  return useQuery({
    queryKey: ["admin", "transactions", page, size],
    queryFn: () => getAllTransactions(page, size),
    enabled: isAdmin,
    placeholderData: keepPreviousData,
  });
};
