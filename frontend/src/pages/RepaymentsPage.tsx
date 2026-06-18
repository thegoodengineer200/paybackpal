import { useMemo, useState } from "react";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { useCreditCards } from "../features/cards/useCreditCards";
import type {
  RepaymentAction,
  RepaymentSplitView,
  RepaymentStatus,
} from "../features/repayments/repaymentTypes";
import { RepaymentSplitTable } from "../features/repayments/RepaymentSplitTable";
import { RepaymentSummaryCards } from "../features/repayments/RepaymentSummaryCards";
import {
  useRepaymentActions,
  useRepaymentSplits,
} from "../features/repayments/useRepayments";

const statusFilterOptions: Array<{
  label: string;
  value: RepaymentStatus | "ALL";
}> = [
  { label: "All", value: "ALL" },
  { label: "Pending", value: "PENDING" },
  { label: "Payment reported", value: "PAYMENT_REPORTED" },
  { label: "Confirmed", value: "CONFIRMED" },
  { label: "Cancelled", value: "CANCELLED" },
];

export function RepaymentsPage() {
  const cardsQuery = useCreditCards();
  const cards = cardsQuery.data ?? [];

  const repaymentSplitsQuery = useRepaymentSplits(cards);
  const repaymentActions = useRepaymentActions();

  const [statusFilter, setStatusFilter] = useState<RepaymentStatus | "ALL">(
    "ALL",
  );
  const [activeActionKey, setActiveActionKey] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<string | null>(null);
  const [actionErrorMessage, setActionErrorMessage] = useState<string | null>(
    null,
  );

  const repaymentSplits = repaymentSplitsQuery.repaymentSplits;

  const filteredSplits = useMemo(() => {
    if (statusFilter === "ALL") {
      return repaymentSplits;
    }

    return repaymentSplits.filter(
      (split) => split.repaymentStatus === statusFilter,
    );
  }, [repaymentSplits, statusFilter]);

  async function handleAction(
    action: RepaymentAction,
    split: RepaymentSplitView,
  ) {
    const shouldContinue = confirmAction(action, split);

    if (!shouldContinue) {
      return;
    }

    const actionKey = `${action}:${split.splitId}`;

    setActiveActionKey(actionKey);
    setActionMessage(null);
    setActionErrorMessage(null);

    try {
      const mutation = repaymentActions.getMutationForAction(action);
      await mutation.mutateAsync(split.splitId);

      setActionMessage(getSuccessMessage(action, split));
    } catch (error) {
      setActionErrorMessage(getErrorMessage(error));
    } finally {
      setActiveActionKey(null);
    }
  }

  const isLoadingCards = cardsQuery.isLoading;
  const isLoadingSplits = repaymentSplitsQuery.isLoading;
  const isLoading = isLoadingCards || isLoadingSplits;

  const isError = cardsQuery.isError || repaymentSplitsQuery.isError;

  return (
    <section>
      <PageHeader
        title="Repayments"
        description="Track borrower repayment statuses, send reminders, and confirm payments."
      />

      <div className="space-y-6">
        {actionMessage && (
          <div className="rounded-2xl border border-green-200 bg-green-50 px-5 py-4 text-sm text-green-800">
            {actionMessage}
          </div>
        )}

        {actionErrorMessage && (
          <ErrorState message={actionErrorMessage} />
        )}

        {isLoading && (
          <LoadingState
            title="Loading repayments"
            description="Fetching cards, transactions, and borrower splits."
          />
        )}

        {isError && (
          <ErrorState
            message={
              getErrorMessage(cardsQuery.error) ??
              getErrorMessage(repaymentSplitsQuery.error) ??
              "Unable to load repayments."
            }
            onRetry={() => {
              void cardsQuery.refetch();
              void repaymentSplitsQuery.refetch();
            }}
          />
        )}

        {!isLoading && !isError && cards.length === 0 && (
          <EmptyState
            title="No cards found"
            description="Add a credit card first. Borrower repayment splits will appear here after you create borrowed transactions."
          />
        )}

        {!isLoading &&
          !isError &&
          cards.length > 0 &&
          repaymentSplits.length === 0 && (
            <EmptyState
              title="No repayments to track"
              description="Borrowed transaction splits will appear here once you log shared card expenses."
            />
          )}

        {!isLoading &&
          !isError &&
          repaymentSplits.length > 0 && (
            <>
              <RepaymentSummaryCards splits={repaymentSplits} />

              <div className="flex flex-col gap-3 rounded-2xl border border-gray-200 bg-white px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h2 className="text-sm font-semibold text-gray-900">
                    Borrower splits
                  </h2>
                  <p className="mt-1 text-sm text-gray-500">
                    Showing {filteredSplits.length} of {repaymentSplits.length} splits.
                  </p>
                </div>

                <select
                  value={statusFilter}
                  onChange={(event) =>
                    setStatusFilter(
                      event.target.value as RepaymentStatus | "ALL",
                    )
                  }
                  className="rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-gray-900 focus:ring-1 focus:ring-gray-900"
                >
                  {statusFilterOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              {filteredSplits.length === 0 ? (
                <EmptyState
                  title="No splits match this filter"
                  description="Try changing the repayment status filter."
                />
              ) : (
                <RepaymentSplitTable
                  splits={filteredSplits}
                  activeActionKey={activeActionKey}
                  onAction={handleAction}
                />
              )}
            </>
          )}
      </div>
    </section>
  );
}

function confirmAction(
  action: RepaymentAction,
  split: RepaymentSplitView,
): boolean {
  switch (action) {
    case "report-paid":
      return window.confirm(
        `Mark ${split.borrowerName}'s payment as reported for ${split.transactionTitle}?`,
      );

    case "confirm":
      return window.confirm(
        `Confirm ${split.borrowerName}'s payment of ${split.splitAmount}?`,
      );

    case "cancel":
      return window.confirm(
        `Cancel ${split.borrowerName}'s repayment split? This cannot be reported or confirmed later.`,
      );

    case "remind":
      return window.confirm(
        `Send a WhatsApp reminder to ${split.borrowerName}?`,
      );
  }
}

function getSuccessMessage(
  action: RepaymentAction,
  split: RepaymentSplitView,
): string {
  switch (action) {
    case "report-paid":
      return `${split.borrowerName}'s payment has been marked as reported.`;

    case "confirm":
      return `${split.borrowerName}'s payment has been confirmed.`;

    case "cancel":
      return `${split.borrowerName}'s repayment split has been cancelled.`;

    case "remind":
      return `Reminder queued for ${split.borrowerName}.`;
  }
}

function getErrorMessage(error: unknown): string | null {
  if (!error) {
    return null;
  }

  return error instanceof Error
    ? error.message
    : "Something went wrong. Please try again.";
}