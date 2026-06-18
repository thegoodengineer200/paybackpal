import { useMutation, useQueries, useQueryClient } from "@tanstack/react-query";
import { useAuthenticatedApi } from "../../api/useAuthenticatedApi";
import type { CreditCard } from "../cards/cardTypes";
import { TRANSACTIONS_QUERY_KEY } from "../transactions/useTransactions";
import type {
  TransactionResponse,
  TransactionSplitResponse,
} from "../transactions/transactionTypes";
import type {
  NotificationOutboxResponse,
  RepaymentAction,
  RepaymentSplitView,
} from "./repaymentTypes";

export const REPAYMENTS_QUERY_KEY = ["repayments"];

export function useRepaymentSplits(cards: CreditCard[]) {
  const { request } = useAuthenticatedApi();

  const transactionQueries = useQueries({
    queries: cards.map((card) => ({
      queryKey: [...TRANSACTIONS_QUERY_KEY, card.id],
      queryFn: () =>
        request<TransactionResponse[]>(`/api/v1/cards/${card.id}/transactions`),
      enabled: cards.length > 0,
      retry: false,
    })),
  });

  const isLoading = transactionQueries.some((query) => query.isLoading);
  const isError = transactionQueries.some((query) => query.isError);
  const error = transactionQueries.find((query) => query.error)?.error ?? null;

  const transactions = transactionQueries.flatMap(
    (query) => query.data ?? [],
  );

  const repaymentSplits = transactions.flatMap(toRepaymentSplitViews);

  const refetch = async () => {
    await Promise.all(transactionQueries.map((query) => query.refetch()));
  };

  return {
    isLoading,
    isError,
    error,
    transactions,
    repaymentSplits,
    refetch,
  };
}

export function useRepaymentActions() {
  const { request } = useAuthenticatedApi();
  const queryClient = useQueryClient();

  const invalidateRepaymentData = async () => {
    await queryClient.invalidateQueries({
      queryKey: TRANSACTIONS_QUERY_KEY,
    });

    await queryClient.invalidateQueries({
      queryKey: REPAYMENTS_QUERY_KEY,
    });
  };

  const reportPaidMutation = useMutation({
    mutationFn: (splitId: string) =>
      request<TransactionSplitResponse>(
        `/api/v1/transaction-splits/${splitId}/report-paid`,
        {
          method: "POST",
        },
      ),
    onSuccess: invalidateRepaymentData,
  });

  const confirmPaymentMutation = useMutation({
    mutationFn: (splitId: string) =>
      request<TransactionSplitResponse>(
        `/api/v1/transaction-splits/${splitId}/confirm`,
        {
          method: "POST",
        },
      ),
    onSuccess: invalidateRepaymentData,
  });

  const cancelSplitMutation = useMutation({
    mutationFn: (splitId: string) =>
      request<TransactionSplitResponse>(
        `/api/v1/transaction-splits/${splitId}/cancel`,
        {
          method: "POST",
        },
      ),
    onSuccess: invalidateRepaymentData,
  });

  const remindBorrowerMutation = useMutation({
    mutationFn: (splitId: string) =>
      request<NotificationOutboxResponse>(
        `/api/v1/transaction-splits/${splitId}/remind`,
        {
          method: "POST",
        },
      ),
    onSuccess: invalidateRepaymentData,
  });

  function getMutationForAction(action: RepaymentAction) {
    switch (action) {
      case "report-paid":
        return reportPaidMutation;
      case "confirm":
        return confirmPaymentMutation;
      case "cancel":
        return cancelSplitMutation;
      case "remind":
        return remindBorrowerMutation;
    }
  }

  return {
    reportPaidMutation,
    confirmPaymentMutation,
    cancelSplitMutation,
    remindBorrowerMutation,
    getMutationForAction,
  };
}

function toRepaymentSplitViews(
  transaction: TransactionResponse,
): RepaymentSplitView[] {
  if (!transaction.borrowed || transaction.splits.length === 0) {
    return [];
  }

  return transaction.splits.map((split) => ({
    splitId: split.id,
    transactionId: transaction.id,
    transactionTitle:
      transaction.merchantName ||
      transaction.description ||
      "Untitled transaction",
    transactionDate: transaction.transactionDate,

    cardId: transaction.cardId,
    cardName: transaction.cardName,
    bankName: transaction.bankName,
    lastFourDigits: transaction.lastFourDigits,

    borrowerId: split.borrowerId,
    borrowerName: split.borrowerName,
    borrowerPhoneNumber: split.borrowerPhoneNumber,

    splitPercentage: Number(split.splitPercentage),
    splitAmount: Number(split.splitAmount),
    repaymentStatus: split.repaymentStatus as RepaymentSplitView["repaymentStatus"],
  }));
}