import { apiClient } from "./client";
import { User, Transaction, PaginatedResponse } from "../types";

export const getAllUsers = async (
  page = 0,
  size = 10
): Promise<PaginatedResponse<User> | User[]> => {
  const { data } = await apiClient.get<PaginatedResponse<User> | User[]>(
    `/admin/users?page=${page}&size=${size}`
  );
  return data;
};

export const getAllTransactions = async (
  page = 0,
  size = 50
): Promise<PaginatedResponse<Transaction> | Transaction[]> => {
  const { data } = await apiClient.get<PaginatedResponse<Transaction> | Transaction[]>(
    `/admin/transactions?page=${page}&size=${size}`
  );
  return data;
};
