import { Card, CardContent, CardHeader } from "../../components/Card";
import { formatMoney } from "../../utils/formatMoney";
import type { TransactionResponse } from "./transactionTypes";

type TransactionSuccessCardProps = {
  transaction: TransactionResponse;
};

export function TransactionSuccessCard({
  transaction,
}: TransactionSuccessCardProps) {
  return (
    <Card className="border-green-200 bg-green-50">
      <CardHeader className="border-green-200">
        <h2 className="text-base font-semibold text-green-950">
          Transaction created
        </h2>
        <p className="mt-1 text-sm text-green-800">
          {transaction.borrowed
            ? "Borrower repayment tracking has been created."
            : "Personal transaction has been logged."}
        </p>
      </CardHeader>

      <CardContent>
        <div className="grid gap-4 text-sm md:grid-cols-4">
          <InfoItem label="Amount" value={formatMoney(transaction.amount)} />
          <InfoItem
            label="Owner share"
            value={formatMoney(transaction.ownerShareAmount)}
          />
          <InfoItem
            label="Card"
            value={`${transaction.cardName} •••• ${transaction.lastFourDigits}`}
          />
          <InfoItem label="Date" value={transaction.transactionDate} />
        </div>

        {transaction.borrowed && transaction.splits.length > 0 && (
          <div className="mt-5 overflow-hidden rounded-xl border border-green-200 bg-white">
            <table className="min-w-full divide-y divide-green-100 text-sm">
              <thead className="bg-green-50">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-green-950">
                    Borrower
                  </th>
                  <th className="px-4 py-3 text-left font-semibold text-green-950">
                    Percentage
                  </th>
                  <th className="px-4 py-3 text-left font-semibold text-green-950">
                    Amount
                  </th>
                  <th className="px-4 py-3 text-left font-semibold text-green-950">
                    Status
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-green-100">
                {transaction.splits.map((split) => (
                  <tr key={split.id}>
                    <td className="px-4 py-3 text-gray-800">
                      {split.borrowerName}
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      {Number(split.splitPercentage).toFixed(2)}%
                    </td>
                    <td className="px-4 py-3 text-gray-800">
                      {formatMoney(split.splitAmount)}
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      {split.repaymentStatus}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

type InfoItemProps = {
  label: string;
  value: string;
};

function InfoItem({ label, value }: InfoItemProps) {
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-wide text-green-700">
        {label}
      </p>
      <p className="mt-1 font-semibold text-green-950">{value}</p>
    </div>
  );
}