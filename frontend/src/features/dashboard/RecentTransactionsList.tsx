import { Link } from "react-router";
import { Card, CardContent, CardHeader } from "../../components/Card";
import { EmptyState } from "../../components/EmptyState";
import { formatMoney } from "../../utils/formatMoney";
import type { TransactionResponse } from "../transactions/transactionTypes";

type RecentTransactionsListProps = {
  transactions: TransactionResponse[];
};

export function RecentTransactionsList({
  transactions,
}: RecentTransactionsListProps) {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between gap-4">
          <div>
            <h2 className="text-base font-semibold text-gray-900">
              Recent transactions
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Latest card expenses across all cards.
            </p>
          </div>

          <Link
            to="/transactions"
            className="text-sm font-semibold text-gray-900 hover:underline"
          >
            View all
          </Link>
        </div>
      </CardHeader>

      <CardContent>
        {transactions.length === 0 ? (
          <EmptyState
            title="No transactions yet"
            description="Log your first card transaction to see recent activity here."
          />
        ) : (
          <div className="space-y-4">
            {transactions.map((transaction) => (
              <Link
                key={transaction.id}
                to={`/transactions/${transaction.id}`}
                className="block rounded-xl border border-gray-200 p-4 hover:bg-gray-50"
              >
                <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="font-semibold text-gray-900">
                        {getTransactionTitle(transaction)}
                      </p>

                      {transaction.borrowed ? (
                        <span className="rounded-full bg-purple-50 px-2 py-0.5 text-xs font-semibold text-purple-700 ring-1 ring-purple-200">
                          Borrowed
                        </span>
                      ) : (
                        <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs font-semibold text-gray-700 ring-1 ring-gray-200">
                          Personal
                        </span>
                      )}
                    </div>

                    <p className="mt-1 text-sm text-gray-500">
                      {transaction.cardName} •••• {transaction.lastFourDigits} ·{" "}
                      {transaction.transactionDate}
                    </p>
                  </div>

                  <div className="text-left sm:text-right">
                    <p className="font-semibold text-gray-900">
                      {formatMoney(transaction.amount)}
                    </p>
                    <p className="mt-1 text-xs text-gray-500">
                      Owner: {formatMoney(transaction.ownerShareAmount)}
                    </p>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function getTransactionTitle(transaction: TransactionResponse): string {
  return (
    transaction.merchantName ||
    transaction.description ||
    "Untitled transaction"
  );
}