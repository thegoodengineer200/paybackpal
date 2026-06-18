import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { useCreditCards } from "../features/cards/useCreditCards";
import {
  calculateDashboardMetrics,
  getPendingRepayments,
  getRecentTransactions,
} from "../features/dashboard/dashboardCalculations";
import { DashboardSummaryCards } from "../features/dashboard/DashboardSummaryCards";
import { PendingRepaymentsList } from "../features/dashboard/PendingRepaymentsList";
import { QuickLinksCard } from "../features/dashboard/QuickLinksCard";
import { RecentTransactionsList } from "../features/dashboard/RecentTransactionsList";
import { useRepaymentSplits } from "../features/repayments/useRepayments";

export function DashboardPage() {
  const cardsQuery = useCreditCards();
  const cards = cardsQuery.data ?? [];

  const repaymentData = useRepaymentSplits(cards);

  const transactions = repaymentData.transactions;
  const repaymentSplits = repaymentData.repaymentSplits;

  const metrics = calculateDashboardMetrics(transactions, repaymentSplits);
  const recentTransactions = getRecentTransactions(transactions, 5);
  const pendingRepayments = getPendingRepayments(repaymentSplits, 5);

  const isLoading = cardsQuery.isLoading || repaymentData.isLoading;
  const isError = cardsQuery.isError || repaymentData.isError;

  return (
    <section>
      <PageHeader
        title="Dashboard"
        description="A quick overview of your credit card spending, borrower repayments, and pending actions."
      />

      <div className="space-y-6">
        {isLoading && (
          <LoadingState
            title="Loading dashboard"
            description="Fetching your cards, transactions, and repayment status."
          />
        )}

        {isError && (
          <ErrorState
            message={
              getErrorMessage(cardsQuery.error) ??
              getErrorMessage(repaymentData.error) ??
              "Unable to load dashboard."
            }
            onRetry={() => {
              void cardsQuery.refetch();
              void repaymentData.refetch();
            }}
          />
        )}

        {!isLoading && !isError && cards.length === 0 && (
          <>
            <EmptyState
              title="Welcome to PayBackPal"
              description="Add your first credit card, then start logging personal and borrowed transactions."
            />

            <QuickLinksCard />
          </>
        )}

        {!isLoading && !isError && cards.length > 0 && (
          <>
            <DashboardSummaryCards metrics={metrics} />

            <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
              <RecentTransactionsList transactions={recentTransactions} />
              <PendingRepaymentsList repayments={pendingRepayments} />
            </div>

            <QuickLinksCard />
          </>
        )}
      </div>
    </section>
  );
}

function getErrorMessage(error: unknown): string | null {
  if (!error) {
    return null;
  }

  return error instanceof Error
    ? error.message
    : "Something went wrong. Please try again.";
}