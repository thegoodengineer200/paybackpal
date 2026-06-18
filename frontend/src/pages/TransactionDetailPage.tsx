import { Link, useParams } from "react-router";
import { Button } from "../components/Button";
import { Card, CardContent, CardHeader } from "../components/Card";
import { ErrorState } from "../components/ErrorState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { RepaymentStatusBadge } from "../features/transactions/RepaymentStatusBadge";
import { useTransaction } from "../features/transactions/useTransactions";
import { formatMoney } from "../utils/formatMoney";

export function TransactionDetailPage() {
  const { transactionId } = useParams();
  const transactionQuery = useTransaction(transactionId);

  if (transactionQuery.isLoading) {
    return (
      <LoadingState
        title="Loading transaction"
        description="Fetching transaction details and borrower splits."
      />
    );
  }

  if (transactionQuery.isError) {
    return (
      <ErrorState
        message={getErrorMessage(transactionQuery.error)}
        onRetry={() => void transactionQuery.refetch()}
      />
    );
  }

  const transaction = transactionQuery.data;

  if (!transaction) {
    return (
      <ErrorState message="Transaction not found or you do not have access." />
    );
  }

  return (
    <section>
      <PageHeader
        title={transaction.merchantName || "Transaction detail"}
        description={`${transaction.cardName} •••• ${transaction.lastFourDigits} · ${transaction.transactionDate}`}
        action={
          <Link to="/transactions">
            <Button type="button" variant="secondary">
              Back to transactions
            </Button>
          </Link>
        }
      />

      <div className="space-y-6">
        <Card>
          <CardHeader>
            <h2 className="text-base font-semibold text-gray-900">
              Summary
            </h2>
          </CardHeader>

          <CardContent>
            <div className="grid gap-5 md:grid-cols-4">
              <InfoItem
                label="Total amount"
                value={formatMoney(transaction.amount)}
              />
              <InfoItem
                label="Owner share"
                value={formatMoney(transaction.ownerShareAmount)}
              />
              <InfoItem
                label="Type"
                value={transaction.borrowed ? "Borrowed" : "Personal"}
              />
              <InfoItem label="Date" value={transaction.transactionDate} />
            </div>

            {(transaction.description || transaction.merchantName) && (
              <div className="mt-6 rounded-2xl border border-gray-200 bg-gray-50 p-4">
                {transaction.merchantName && (
                  <InfoItem
                    label="Merchant"
                    value={transaction.merchantName}
                  />
                )}

                {transaction.description && (
                  <div className="mt-4">
                    <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
                      Description
                    </p>
                    <p className="mt-1 text-sm text-gray-800">
                      {transaction.description}
                    </p>
                  </div>
                )}
              </div>
            )}
          </CardContent>
        </Card>

        {transaction.borrowed ? (
          <Card>
            <CardHeader>
              <h2 className="text-base font-semibold text-gray-900">
                Borrower splits
              </h2>
              <p className="mt-1 text-sm text-gray-500">
                Track borrower repayment status for this transaction.
              </p>
            </CardHeader>

            <CardContent>
              {transaction.splits.length === 0 ? (
                <p className="text-sm text-gray-500">
                  No borrower splits found.
                </p>
              ) : (
                <div className="overflow-hidden rounded-xl border border-gray-200">
                  <table className="min-w-full divide-y divide-gray-200 text-sm">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-3 text-left font-semibold text-gray-700">
                          Borrower
                        </th>
                        <th className="px-4 py-3 text-left font-semibold text-gray-700">
                          Phone
                        </th>
                        <th className="px-4 py-3 text-left font-semibold text-gray-700">
                          Percentage
                        </th>
                        <th className="px-4 py-3 text-left font-semibold text-gray-700">
                          Amount
                        </th>
                        <th className="px-4 py-3 text-left font-semibold text-gray-700">
                          Status
                        </th>
                      </tr>
                    </thead>

                    <tbody className="divide-y divide-gray-200 bg-white">
                      {transaction.splits.map((split) => (
                        <tr key={split.id}>
                          <td className="px-4 py-3 font-medium text-gray-900">
                            {split.borrowerName}
                          </td>
                          <td className="px-4 py-3 text-gray-600">
                            {split.borrowerPhoneNumber}
                          </td>
                          <td className="px-4 py-3 text-gray-600">
                            {Number(split.splitPercentage).toFixed(2)}%
                          </td>
                          <td className="px-4 py-3 font-medium text-gray-900">
                            {formatMoney(split.splitAmount)}
                          </td>
                          <td className="px-4 py-3">
                            <RepaymentStatusBadge
                              status={split.repaymentStatus}
                            />
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardContent>
              <p className="text-sm text-gray-600">
                This is a personal transaction, so there are no borrower splits
                or repayment statuses.
              </p>
            </CardContent>
          </Card>
        )}
      </div>
    </section>
  );
}

type InfoItemProps = {
  label: string;
  value: string;
};

function InfoItem({ label, value }: InfoItemProps) {
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
        {label}
      </p>
      <p className="mt-1 font-semibold text-gray-900">{value}</p>
    </div>
  );
}

function getErrorMessage(error: unknown): string {
  return error instanceof Error
    ? error.message
    : "Unable to load transaction.";
}