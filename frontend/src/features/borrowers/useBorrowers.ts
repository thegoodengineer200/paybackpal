import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuthenticatedApi } from "../../api/useAuthenticatedApi";
import type {
  Borrower,
  CreateBorrowerRequest,
  UpdateBorrowerRequest,
} from "./borrowerTypes";

const BORROWERS_QUERY_KEY = ["borrowers"];

export function useBorrowers() {
  const { request } = useAuthenticatedApi();

  return useQuery({
    queryKey: BORROWERS_QUERY_KEY,
    queryFn: () => request<Borrower[]>("/api/v1/borrowers"),
    retry: false,
  });
}

export function useCreateBorrower() {
  const { request } = useAuthenticatedApi();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateBorrowerRequest) =>
      request<Borrower>("/api/v1/borrowers", {
        method: "POST",
        body: payload,
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: BORROWERS_QUERY_KEY,
      });
    },
  });
}

export function useUpdateBorrower() {
  const { request } = useAuthenticatedApi();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      borrowerId,
      payload,
    }: {
      borrowerId: string;
      payload: UpdateBorrowerRequest;
    }) =>
      request<Borrower>(`/api/v1/borrowers/${borrowerId}`, {
        method: "PUT",
        body: payload,
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: BORROWERS_QUERY_KEY,
      });
    },
  });
}

export function useDeleteBorrower() {
  const { request } = useAuthenticatedApi();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (borrowerId: string) =>
      request<void>(`/api/v1/borrowers/${borrowerId}`, {
        method: "DELETE",
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: BORROWERS_QUERY_KEY,
      });
    },
  });
}