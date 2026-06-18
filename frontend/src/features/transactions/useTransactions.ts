import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuthenticatedApi } from "../../api/useAuthenticatedApi";
import type {
  CreateTransactionPayload,
  TransactionResponse,
} from "./transactionTypes";

export const TRANSACTIONS_QUERY_KEY = ["transactions"];

export function useCreateTransaction() {
  const { request } = useAuthenticatedApi();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ cardId, request: payload }: CreateTransactionPayload) =>
      request<TransactionResponse>(`/api/v1/cards/${cardId}/transactions`, {
        method: "POST",
        body: payload,
      }),
    onSuccess: async (_, variables) => {
      await queryClient.invalidateQueries({
        queryKey: TRANSACTIONS_QUERY_KEY,
      });

      await queryClient.invalidateQueries({
        queryKey: [...TRANSACTIONS_QUERY_KEY, variables.cardId],
      });
    },
  });
}

export function useTransactionsForCard(cardId: string | null) {
  const { request } = useAuthenticatedApi();

  return useQuery({
    queryKey: [...TRANSACTIONS_QUERY_KEY, cardId],
    queryFn: () =>
      request<TransactionResponse[]>(`/api/v1/cards/${cardId}/transactions`),
    enabled: Boolean(cardId),
    retry: false,
  });
}

export function useTransaction(transactionId: string | undefined) {
  const { request } = useAuthenticatedApi();

  return useQuery({
    queryKey: [...TRANSACTIONS_QUERY_KEY, "detail", transactionId],
    queryFn: () =>
      request<TransactionResponse>(`/api/v1/transactions/${transactionId}`),
    enabled: Boolean(transactionId),
    retry: false,
  });
}