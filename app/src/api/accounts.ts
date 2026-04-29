import { apiClient } from "./client";
import { Account } from "../types";

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
