import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getAccounts,
  createAccount,
  depositFunds,
  applyForLoan,
  openCreditCard,
} from "../api/accounts";
import { useAuthStore } from "../store/authStore";
import type { Account } from "../types";

/**
 * Custom hook to fetch the bank accounts for the currently authenticated user.
 *
 * @returns {UseQueryResult} The React Query result containing the list of accounts.
 */
export const useAccounts = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ["accounts"],
    queryFn: getAccounts,
    enabled: isAuthenticated,
  });
};

/**
 * Custom hook providing a mutation function to provision a new bank account.
 * Automatically invalidates the 'accounts' query cache upon success to ensure
 * the UI reflects the newly created account.
 *
 * @returns {UseMutationResult} The React Query mutation result.
 */
export const useCreateAccount = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ name, subtype }: Pick<Account, "name" | "subtype">) =>
      createAccount(name, subtype),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
    },
  });
};

export const useApplyForLoan = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      principal,
      termMonths,
    }: {
      principal: number;
      termMonths: number;
    }) => applyForLoan(principal, termMonths),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
    },
  });
};

export const useOpenCreditCard = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ creditLimit }: { creditLimit: number }) =>
      openCreditCard(creditLimit),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
    },
  });
};

/**
 * Custom hook providing a mutation function to simulate a deposit.
 * Invalidate accounts and transactions to update balances and history.
 */
export const useDeposit = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      accountId,
      amount,
    }: {
      accountId: Account["id"];
      amount: number;
    }) => depositFunds(accountId, amount),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
      queryClient.invalidateQueries({ queryKey: ["transactions"] });
    },
  });
};
