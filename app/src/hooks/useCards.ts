import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getUserCards,
  issueCard,
  freezeCard,
  unfreezeCard,
  setPin,
} from "../api/cards";
import { useAuthStore } from "../store/authStore";

export const useCards = () => {
  const user = useAuthStore((state) => state.user);

  return useQuery({
    queryKey: ["cards", user?.id],
    queryFn: () => getUserCards(),
    enabled: !!user?.id,
  });
};

export const useIssueCard = () => {
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);

  return useMutation({
    mutationFn: ({
      accountId,
      type,
    }: {
      accountId: string;
      type: "PHYSICAL" | "VIRTUAL";
    }) => issueCard(accountId, type),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cards", user?.id] });
    },
  });
};

export const useFreezeCard = () => {
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);

  return useMutation({
    mutationFn: (cardId: string) => freezeCard(cardId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cards", user?.id] });
    },
  });
};

export const useUnfreezeCard = () => {
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);

  return useMutation({
    mutationFn: (cardId: string) => unfreezeCard(cardId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cards", user?.id] });
    },
  });
};

export const useSetPin = () => {
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);

  return useMutation({
    mutationFn: ({ cardId, pin }: { cardId: string; pin: string }) =>
      setPin(cardId, pin),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cards", user?.id] });
    },
  });
};
