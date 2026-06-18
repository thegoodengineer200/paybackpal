import { useEffect, useState } from "react";
import { Button } from "../components/Button";
import { Card, CardContent, CardHeader } from "../components/Card";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { useBorrowers } from "../features/borrowers/useBorrowers";
import { useCreditCards } from "../features/cards/useCreditCards";
import { TransactionCreateForm } from "../features/transactions/TransactionCreateForm";
import { TransactionList } from "../features/transactions/TransactionList";
import { TransactionSuccessCard } from "../features/transactions/TransactionSuccessCard";
import type {
  CreateTransactionRequest,
  TransactionResponse,
} from "../features/transactions/transactionTypes";
import {
  useCreateTransaction,
  useTransactionsForCard,
} from "../features/transactions/useTransactions";

export function TransactionsPage() {
  const cardsQuery = useCreditCards();
  const borrowersQuery = useBorrowers();

  const [selectedListCardId, setSelectedListCardId] = useState<string | null>(
    null,
  );

  const transactionsQuery = useTransactionsForCard(selectedListCardId);
  const createTransactionMutation = useCreateTransaction();

  const [lastCreatedTransaction, setLastCreatedTransaction] =
    useState<TransactionResponse | null>(null);

  const cards = cardsQuery.data ?? [];
  const borrowers = borrowersQuery.data ?? [];
  const transactions = transactionsQuery.data ?? [];

  useEffect(() => {
    if (!selectedListCardId && cards.length > 0) {
      setSelectedListCardId(cards[0].id);
    }
  }, [cards, selectedListCardId]);

  async function handleSubmit(
    cardId: string,
    request: CreateTransactionRequest,
  ) {
    const transaction = await createTransactionMutation.mutateAsync({
      cardId,
      request,
    });

    setLastCreatedTransaction(transaction);
    setSelectedListCardId(cardId);
  }

  const isSetupLoading = cardsQuery.isLoading || borrowersQuery.isLoading;
  const isSetupError = cardsQuery.isError || borrowersQuery.isError;

  return (
    <section>
      <PageHeader
        title="Transactions"
        description="Log personal expenses or borrowed credit card transactions with borrower split tracking."
      />

      <div className="space-y-8">
        {lastCreatedTransaction && (
          <TransactionSuccessCard transaction={lastCreatedTransaction} />
        )}

        {isSetupLoading && (
          <LoadingState
            title="Loading transaction setup"
            description="Fetching your cards and borrowers."
          />
        )}

        {isSetupError && (
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

        {!isSetupLoading && !isSetupError && cards.length === 0 && (
          <EmptyState
            title="Add a credit card first"
            description="You need at least one credit card before logging transactions."
          />
        )}

        {!isSetupLoading && !isSetupError && cards.length > 0 && (
          <>
            <TransactionCreateForm
              cards={cards}
              borrowers={borrowers}
              isSubmitting={createTransactionMutation.isPending}
              errorMessage={getMutationErrorMessage(
                createTransactionMutation.error,
              )}
              onSubmit={handleSubmit}
            />

            {!isSetupLoading &&
              !isSetupError &&
              cards.length > 0 &&
              borrowers.length === 0 && (
                <div className="rounded-2xl border border-yellow-200 bg-yellow-50 px-5 py-4">
                  <h2 className="text-sm font-semibold text-yellow-950">
                    Borrowed transactions need borrowers
                  </h2>
                  <p className="mt-1 text-sm text-yellow-800">
                    You can still log personal transactions. Add borrowers from
                    the Borrowers page before creating shared transactions.
                  </p>
                </div>
              )}

            <TransactionHistorySection
              cards={cards}
              selectedCardId={selectedListCardId}
              onSelectedCardChange={setSelectedListCardId}
              isLoading={transactionsQuery.isLoading}
              isError={transactionsQuery.isError}
              errorMessage={getQueryErrorMessage(transactionsQuery.error)}
              onRetry={() => void transactionsQuery.refetch()}
              transactions={transactions}
            />
          </>
        )}
      </div>
    </section>
  );
}

type TransactionHistorySectionProps = {
  cards: Array<{
    id: string;
    cardName: string;
    lastFourDigits: string;
  }>;
  selectedCardId: string | null;
  onSelectedCardChange: (cardId: string) => void;
  isLoading: boolean;
  isError: boolean;
  errorMessage: string | null;
  onRetry: () => void;
  transactions: TransactionResponse[];
};

function TransactionHistorySection({
  cards,
  selectedCardId,
  onSelectedCardChange,
  isLoading,
  isError,
  errorMessage,
  onRetry,
  transactions,
}: TransactionHistorySectionProps) {
  return (
    <Card>
      <CardHeader>
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-base font-semibold text-gray-900">
              Transaction history
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Select a card to view its logged transactions.
            </p>
          </div>

          <select
            value={selectedCardId ?? ""}
            onChange={(event) => onSelectedCardChange(event.target.value)}
            className="rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-gray-900 focus:ring-1 focus:ring-gray-900"
          >
            {cards.map((card) => (
              <option key={card.id} value={card.id}>
                {card.cardName} •••• {card.lastFourDigits}
              </option>
            ))}
          </select>
        </div>
      </CardHeader>

      <CardContent>
        {isLoading && (
          <LoadingState
            title="Loading transactions"
            description="Fetching transactions for the selected card."
          />
        )}

        {isError && (
          <ErrorState
            message={errorMessage ?? "Unable to load transactions."}
            onRetry={onRetry}
          />
        )}

        {!isLoading && !isError && transactions.length === 0 && (
          <EmptyState
            title="No transactions for this card"
            description="Create a transaction above and it will appear here."
          />
        )}

        {!isLoading && !isError && transactions.length > 0 && (
          <TransactionList transactions={transactions} />
        )}
      </CardContent>
    </Card>
  );
}

function getQueryErrorMessage(error: unknown): string | null {
  if (!error) {
    return null;
  }

  return error instanceof Error ? error.message : "Unable to load data.";
}

function getMutationErrorMessage(error: unknown): string | null {
  if (!error) {
    return null;
  }

  return error instanceof Error
    ? error.message
    : "Unable to create transaction.";
}