import { Link } from "react-router";
import { Card, CardContent } from "../../components/Card";
import { formatMoney } from "../../utils/formatMoney";
import type { TransactionResponse } from "./transactionTypes";
import { RepaymentStatusBadge } from "./RepaymentStatusBadge";

type TransactionListProps = {
  transactions: TransactionResponse[];
};

export function TransactionList({ transactions }: TransactionListProps) {
  return (
    <div className="space-y-4">
      {transactions.map((transaction) => (
        <Card key={transaction.id}>
          <CardContent>
            <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <h2 className="text-base font-semibold text-gray-900">
                    {getTransactionTitle(transaction)}
                  </h2>

                  {transaction.borrowed ? (
                    <span className="rounded-full bg-purple-50 px-2.5 py-1 text-xs font-semibold text-purple-700 ring-1 ring-purple-200">
                      Borrowed
                    </span>
                  ) : (
                    <span className="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-semibold text-gray-700 ring-1 ring-gray-200">
                      Personal
                    </span>
                  )}
                </div>

                <p className="mt-1 text-sm text-gray-500">
                  {transaction.cardName} •••• {transaction.lastFourDigits} ·{" "}
                  {transaction.transactionDate}
                </p>

                <div className="mt-5 grid gap-4 text-sm sm:grid-cols-3">
                  <InfoItem
                    label="Amount"
                    value={formatMoney(transaction.amount)}
                  />
                  <InfoItem
                    label="Owner share"
                    value={formatMoney(transaction.ownerShareAmount)}
                  />
                  <InfoItem
                    label="Borrowers"
                    value={
                      transaction.borrowed
                        ? String(transaction.splits.length)
                        : "None"
                    }
                  />
                </div>

                {transaction.borrowed && transaction.splits.length > 0 && (
                  <div className="mt-5 flex flex-wrap gap-2">
                    {transaction.splits.map((split) => (
                      <div
                        key={split.id}
                        className="rounded-xl border border-gray-200 bg-gray-50 px-3 py-2"
                      >
                        <p className="text-xs font-medium text-gray-900">
                          {split.borrowerName}
                        </p>
                        <div className="mt-1">
                          <RepaymentStatusBadge
                            status={split.repaymentStatus}
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <Link
                to={`/transactions/${transaction.id}`}
                className="inline-flex items-center justify-center rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50"
              >
                View details
              </Link>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
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

function getTransactionTitle(transaction: TransactionResponse): string {
  return (
    transaction.merchantName ||
    transaction.description ||
    "Untitled transaction"
  );
}