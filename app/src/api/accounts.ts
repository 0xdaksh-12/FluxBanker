import { apiClient } from "./client";
import type { Account } from "../types";

export const getAccounts = async (): Promise<Account[]> => {
  const { data } = await apiClient.get<Account[]>("/accounts");
  return data;
};

export const createAccount = async (
  name: string,
  subtype: string,
): Promise<Account> => {
  const { data } = await apiClient.post<Account>("/accounts", {
    name,
    subtype,
  });
  return data;
};

export const depositFunds = async (
  accountId: string,
  amount: number,
): Promise<void> => {
  await apiClient.post(`/accounts/${accountId}/deposit`, { amount });
};

export const applyForLoan = async (
  principalAmount: number,
  termMonths: number,
): Promise<Account> => {
  const { data } = await apiClient.post<Account>("/accounts/apply/loan", {
    principalAmount,
    termMonths,
  });
  return data;
};

export const openCreditCard = async (creditLimit: number): Promise<Account> => {
  const { data } = await apiClient.post<Account>("/accounts/apply/credit", {
    creditLimit,
  });
  return data;
};
