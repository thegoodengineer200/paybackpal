import { useState } from "react";
import { Button } from "../components/Button";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { useBorrowers } from "../features/borrowers/useBorrowers";
import { useCreditCards } from "../features/cards/useCreditCards";
import { TransactionCreateForm } from "../features/transactions/TransactionCreateForm";
import { TransactionSuccessCard } from "../features/transactions/TransactionSuccessCard";
import type {
  CreateTransactionRequest,
  TransactionResponse,
} from "../features/transactions/transactionTypes";
import { useCreateTransaction } from "../features/transactions/useTransactions";

export function TransactionsPage() {
  const cardsQuery = useCreditCards();
  const borrowersQuery = useBorrowers();
  const createTransactionMutation = useCreateTransaction();

  const [lastCreatedTransaction, setLastCreatedTransaction] =
    useState<TransactionResponse | null>(null);

  async function handleSubmit(
    cardId: string,
    request: CreateTransactionRequest,
  ) {
    const transaction = await createTransactionMutation.mutateAsync({
      cardId,
      request,
    });

    setLastCreatedTransaction(transaction);
  }

  const cards = cardsQuery.data ?? [];
  const borrowers = borrowersQuery.data ?? [];

  const isLoading = cardsQuery.isLoading || borrowersQuery.isLoading;
  const isError = cardsQuery.isError || borrowersQuery.isError;

  return (
    <section>
      <PageHeader
        title="Transactions"
        description="Log personal expenses or borrowed credit card transactions with borrower split tracking."
      />

      <div className="space-y-6">
        {lastCreatedTransaction && (
          <TransactionSuccessCard transaction={lastCreatedTransaction} />
        )}

        {isLoading && (
          <LoadingState
            title="Loading transaction setup"
            description="Fetching your cards and borrowers."
          />
        )}

        {isError && (
          <ErrorState
            message={
              getQueryErrorMessage(cardsQuery.error) ??
              getQueryErrorMessage(borrowersQuery.error) ??
              "Unable to load transaction setup."
            }
            onRetry={() => {
              void cardsQuery.refetch();
              void borrowersQuery.refetch();
            }}
          />
        )}

        {!isLoading && !isError && cards.length === 0 && (
          <EmptyState
            title="Add a credit card first"
            description="You need at least one credit card before logging transactions."
          />
        )}

        {!isLoading && !isError && cards.length > 0 && (
          <TransactionCreateForm
            cards={cards}
            borrowers={borrowers}
            isSubmitting={createTransactionMutation.isPending}
            errorMessage={getMutationErrorMessage(
              createTransactionMutation.error,
            )}
            onSubmit={handleSubmit}
          />
        )}

        {!isLoading && !isError && cards.length > 0 && borrowers.length === 0 && (
          <div className="rounded-2xl border border-yellow-200 bg-yellow-50 px-5 py-4">
            <h2 className="text-sm font-semibold text-yellow-950">
              Borrowed transactions need borrowers
            </h2>
            <p className="mt-1 text-sm text-yellow-800">
              You can still log personal transactions. Add borrowers from the
              Borrowers page before creating shared transactions.
            </p>
          </div>
        )}

        {lastCreatedTransaction && (
          <Button
            type="button"
            variant="secondary"
            onClick={() => {
              setLastCreatedTransaction(null);
              createTransactionMutation.reset();
            }}
          >
            Create another transaction
          </Button>
        )}
      </div>
    </section>
  );
}

function getQueryErrorMessage(error: unknown): string | null {
  if (!error) {
    return null;
  }

  return error instanceof Error
    ? error.message
    : "Unable to load data.";
}

function getMutationErrorMessage(error: unknown): string | null {
  if (!error) {
    return null;
  }

  return error instanceof Error
    ? error.message
    : "Unable to create transaction.";
}