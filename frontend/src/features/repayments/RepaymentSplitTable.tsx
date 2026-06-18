import { Link } from "react-router";
import { Button } from "../../components/Button";
import { Card, CardContent } from "../../components/Card";
import { RepaymentStatusBadge } from "../transactions/RepaymentStatusBadge";
import { formatMoney } from "../../utils/formatMoney";
import type {
  RepaymentAction,
  RepaymentSplitView,
} from "./repaymentTypes";
import {
  getActionLabel,
  getActionVariant,
  getAvailableActions,
} from "./repaymentActionRules";

type RepaymentSplitTableProps = {
  splits: RepaymentSplitView[];
  activeActionKey: string | null;
  onAction: (action: RepaymentAction, split: RepaymentSplitView) => void;
};

export function RepaymentSplitTable({
  splits,
  activeActionKey,
  onAction,
}: RepaymentSplitTableProps) {
  return (
    <Card>
      <CardContent className="p-0">
        <div className="overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50">
              <tr>
                <TableHeader>Borrower</TableHeader>
                <TableHeader>Transaction</TableHeader>
                <TableHeader>Card</TableHeader>
                <TableHeader>Amount</TableHeader>
                <TableHeader>Status</TableHeader>
                <TableHeader>Actions</TableHeader>
              </tr>
            </thead>

            <tbody className="divide-y divide-gray-200 bg-white">
              {splits.map((split) => (
                <tr key={split.splitId}>
                  <td className="px-4 py-4 align-top">
                    <p className="font-semibold text-gray-900">
                      {split.borrowerName}
                    </p>
                    <p className="mt-1 text-xs text-gray-500">
                      {split.borrowerPhoneNumber}
                    </p>
                  </td>

                  <td className="px-4 py-4 align-top">
                    <Link
                      to={`/transactions/${split.transactionId}`}
                      className="font-medium text-gray-900 hover:underline"
                    >
                      {split.transactionTitle}
                    </Link>
                    <p className="mt-1 text-xs text-gray-500">
                      {split.transactionDate}
                    </p>
                  </td>

                  <td className="px-4 py-4 align-top">
                    <p className="font-medium text-gray-900">
                      {split.cardName}
                    </p>
                    <p className="mt-1 text-xs text-gray-500">
                      •••• {split.lastFourDigits}
                    </p>
                  </td>

                  <td className="px-4 py-4 align-top">
                    <p className="font-semibold text-gray-900">
                      {formatMoney(split.splitAmount)}
                    </p>
                    <p className="mt-1 text-xs text-gray-500">
                      {Number(split.splitPercentage).toFixed(2)}%
                    </p>
                  </td>

                  <td className="px-4 py-4 align-top">
                    <RepaymentStatusBadge status={split.repaymentStatus} />
                  </td>

                  <td className="px-4 py-4 align-top">
                    <div className="flex flex-wrap gap-2">
                      {getAvailableActions(split).length === 0 ? (
                        <span className="text-xs text-gray-400">
                          No actions
                        </span>
                      ) : (
                        getAvailableActions(split).map((action) => {
                          const actionKey = `${action}:${split.splitId}`;

                          return (
                            <Button
                              key={action}
                              type="button"
                              size="sm"
                              variant={getActionVariant(action)}
                              isLoading={activeActionKey === actionKey}
                              disabled={
                                activeActionKey !== null &&
                                activeActionKey !== actionKey
                              }
                              onClick={() => onAction(action, split)}
                            >
                              {getActionLabel(action)}
                            </Button>
                          );
                        })
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  );
}

type TableHeaderProps = {
  children: React.ReactNode;
};

function TableHeader({ children }: TableHeaderProps) {
  return (
    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
      {children}
    </th>
  );
}