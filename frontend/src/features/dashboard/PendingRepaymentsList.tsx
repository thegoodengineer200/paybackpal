import { Link } from "react-router";
import { Card, CardContent, CardHeader } from "../../components/Card";
import { EmptyState } from "../../components/EmptyState";
import { formatMoney } from "../../utils/formatMoney";
import { RepaymentStatusBadge } from "../transactions/RepaymentStatusBadge";
import type { RepaymentSplitView } from "../repayments/repaymentTypes";

type PendingRepaymentsListProps = {
  repayments: RepaymentSplitView[];
};

export function PendingRepaymentsList({
  repayments,
}: PendingRepaymentsListProps) {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between gap-4">
          <div>
            <h2 className="text-base font-semibold text-gray-900">
              Pending repayments
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Borrowers who still need to settle.
            </p>
          </div>

          <Link
            to="/repayments"
            className="text-sm font-semibold text-gray-900 hover:underline"
          >
            View all
          </Link>
        </div>
      </CardHeader>

      <CardContent>
        {repayments.length === 0 ? (
          <EmptyState
            title="No pending repayments"
            description="You are all caught up. Pending borrower splits will appear here."
          />
        ) : (
          <div className="space-y-4">
            {repayments.map((split) => (
              <div
                key={split.splitId}
                className="rounded-xl border border-gray-200 p-4"
              >
                <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <p className="font-semibold text-gray-900">
                      {split.borrowerName}
                    </p>

                    <p className="mt-1 text-sm text-gray-500">
                      {split.transactionTitle} · {split.transactionDate}
                    </p>

                    <p className="mt-1 text-xs text-gray-500">
                      {split.cardName} •••• {split.lastFourDigits}
                    </p>
                  </div>

                  <div className="text-left sm:text-right">
                    <p className="font-semibold text-gray-900">
                      {formatMoney(split.splitAmount)}
                    </p>

                    <div className="mt-2">
                      <RepaymentStatusBadge status={split.repaymentStatus} />
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}