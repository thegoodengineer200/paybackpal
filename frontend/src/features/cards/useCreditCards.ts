import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuthenticatedApi } from "../../api/useAuthenticatedApi";
import type {
  CreateCreditCardRequest,
  CreditCard,
  UpdateCreditCardRequest,
} from "./cardTypes";

const CREDIT_CARDS_QUERY_KEY = ["credit-cards"];

export function useCreditCards() {
  const { request } = useAuthenticatedApi();

  return useQuery({
    queryKey: CREDIT_CARDS_QUERY_KEY,
    queryFn: () => request<CreditCard[]>("/api/v1/cards"),
    retry: false,
  });
}

export function useCreateCreditCard() {
  const { request } = useAuthenticatedApi();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateCreditCardRequest) =>
      request<CreditCard>("/api/v1/cards", {
        method: "POST",
        body: payload,
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: CREDIT_CARDS_QUERY_KEY,
      });
    },
  });
}

export function useUpdateCreditCard() {
  const { request } = useAuthenticatedApi();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      cardId,
      payload,
    }: {
      cardId: string;
      payload: UpdateCreditCardRequest;
    }) =>
      request<CreditCard>(`/api/v1/cards/${cardId}`, {
        method: "PUT",
        body: payload,
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: CREDIT_CARDS_QUERY_KEY,
      });
    },
  });
}

export function useDeleteCreditCard() {
  const { request } = useAuthenticatedApi();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (cardId: string) =>
      request<void>(`/api/v1/cards/${cardId}`, {
        method: "DELETE",
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: CREDIT_CARDS_QUERY_KEY,
      });
    },
  });
}