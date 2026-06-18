import { Card, CardContent } from "../../components/Card";
import { formatMoney } from "../../utils/formatMoney";
import type { DashboardMetrics } from "./dashboardCalculations";

type DashboardSummaryCardsProps = {
  metrics: DashboardMetrics;
};

export function DashboardSummaryCards({ metrics }: DashboardSummaryCardsProps) {
  const cards = [
    {
      label: "Total spend",
      value: formatMoney(metrics.totalSpend),
      description: `${metrics.totalTransactions} transactions`,
    },
    {
      label: "Owner share",
      value: formatMoney(metrics.totalOwnerShare),
      description: "Your actual expense share",
    },
    {
      label: "Pending borrower amount",
      value: formatMoney(metrics.pendingBorrowerAmount),
      description: `${metrics.pendingRepaymentsCount} pending repayments`,
    },
    {
      label: "Reported amount",
      value: formatMoney(metrics.reportedAmount),
      description: "Waiting for your confirmation",
    },
    {
      label: "Confirmed amount",
      value: formatMoney(metrics.confirmedAmount),
      description: "Settled repayments",
    },
    {
      label: "Borrowed transactions",
      value: String(metrics.borrowedTransactions),
      description: "Shared credit card expenses",
    },
  ];

  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      {cards.map((card) => (
        <Card key={card.label}>
          <CardContent>
            <p className="text-sm font-medium text-gray-500">{card.label}</p>
            <p className="mt-3 text-2xl font-bold text-gray-900">
              {card.value}
            </p>
            <p className="mt-1 text-sm text-gray-500">{card.description}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}