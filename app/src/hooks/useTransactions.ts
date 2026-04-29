import { useQuery, useMutation, useQueryClient, keepPreviousData } from "@tanstack/react-query";
import { getTransactions, transferMoney } from "../api/transactions";
import { useAuthStore } from "../store/authStore";

/**
 * Custom hook to fetch the transaction history for a specific account.
 * Automatically handles caching and refetching via React Query.
 *
 * @param {string | null} accountId - The unique identifier of the account to fetch transactions for.
 * @param {number} page - The current page number.
 * @param {number} size - The number of transactions per page.
 * @returns {UseQueryResult} The React Query result containing the transaction data and loading state.
 */
export const useTransactions = (accountId: string | null, page = 0, size = 20) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ["transactions", accountId, page, size],
    queryFn: () => getTransactions(accountId!, page, size),
    enabled: isAuthenticated && !!accountId,
    placeholderData: keepPreviousData,
  });
};

/**
 * Custom hook providing a mutation function to execute a fund transfer.
 * On success, it automatically invalidates relevant React Query caches to refresh
 * the UI's transaction lists and account balances.
 *
 * @returns {UseMutationResult} The React Query mutation result.
 */
export const useTransfer = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      sourceAccountId,
      destinationAccountId,
      amount,
    }: {
      sourceAccountId: string;
      destinationAccountId: string;
      amount: number;
    }) => transferMoney(sourceAccountId, destinationAccountId, amount),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
      queryClient.invalidateQueries({ queryKey: ["transactions"] });
    },
  });
};
