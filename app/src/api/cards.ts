import { apiClient } from "./client";
import type { Card } from "../types";

export const getUserCards = async (): Promise<Card[]> => {
  const { data } = await apiClient.get<Card[]>(`/cards`);
  return data;
};

export const issueCard = async (
  accountId: string,
  type: "PHYSICAL" | "VIRTUAL",
): Promise<Card> => {
  const { data } = await apiClient.post<Card>(`/cards/issue`, {
    accountId,
    type,
  });
  return data;
};

export const freezeCard = async (cardId: string): Promise<Card> => {
  const { data } = await apiClient.post<Card>(`/cards/${cardId}/freeze`);
  return data;
};

export const unfreezeCard = async (cardId: string): Promise<Card> => {
  const { data } = await apiClient.post<Card>(`/cards/${cardId}/unfreeze`);
  return data;
};

export const setPin = async (cardId: string, pin: string): Promise<void> => {
  await apiClient.post(`/cards/${cardId}/pin`, { pin });
};
