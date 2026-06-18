import { Card, CardContent } from "../../components/Card";
import { formatMoney } from "../../utils/formatMoney";
import type { RepaymentSplitView } from "./repaymentTypes";

type RepaymentSummaryCardsProps = {
  splits: RepaymentSplitView[];
};

export function RepaymentSummaryCards({ splits }: RepaymentSummaryCardsProps) {
  const pendingAmount = sumByStatus(splits, "PENDING");
  const reportedAmount = sumByStatus(splits, "PAYMENT_REPORTED");
  const confirmedAmount = sumByStatus(splits, "CONFIRMED");
  const cancelledAmount = sumByStatus(splits, "CANCELLED");

  const cards = [
    {
      label: "Pending",
      value: formatMoney(pendingAmount),
      description: "Borrowers still need to pay",
    },
    {
      label: "Reported",
      value: formatMoney(reportedAmount),
      description: "Borrowers reported payment",
    },
    {
      label: "Confirmed",
      value: formatMoney(confirmedAmount),
      description: "Payments confirmed by you",
    },
    {
      label: "Cancelled",
      value: formatMoney(cancelledAmount),
      description: "Cancelled borrower splits",
    },
  ];

  return (
    <div className="grid gap-4 md:grid-cols-4">
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

function sumByStatus(
  splits: RepaymentSplitView[],
  status: RepaymentSplitView["repaymentStatus"],
): number {
  return splits
    .filter((split) => split.repaymentStatus === status)
    .reduce((sum, split) => sum + Number(split.splitAmount), 0);
}