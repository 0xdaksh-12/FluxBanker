import { apiClient } from "./client";
import { Transaction, PaginatedResponse } from "../types";

export const getTransactions = async (
  accountId: string,
  page = 0,
  size = 20
): Promise<PaginatedResponse<Transaction>> => {
  const { data } = await apiClient.get<PaginatedResponse<Transaction>>(
    `/transactions/account/${accountId}?page=${page}&size=${size}`
  );
  return data;
};

export const getUserTransactions = async (
  page = 0,
  size = 20
): Promise<PaginatedResponse<Transaction>> => {
  const { data } = await apiClient.get<PaginatedResponse<Transaction>>(
    `/transactions/user?page=${page}&size=${size}`
  );
  return data;
};

export const transferMoney = async (
  sourceAccountId: string,
  destinationAccountId: string,
  amount: number,
): Promise<void> => {
  await apiClient.post("/transactions/transfer", {
    sourceAccountId,
    destinationAccountId,
    amount,
  });
};
