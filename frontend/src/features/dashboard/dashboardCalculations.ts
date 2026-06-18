import type { RepaymentSplitView } from "../repayments/repaymentTypes";
import type { TransactionResponse } from "../transactions/transactionTypes";

export type DashboardMetrics = {
  totalSpend: number;
  totalOwnerShare: number;
  pendingBorrowerAmount: number;
  reportedAmount: number;
  confirmedAmount: number;
  totalTransactions: number;
  borrowedTransactions: number;
  pendingRepaymentsCount: number;
};

export function calculateDashboardMetrics(
  transactions: TransactionResponse[],
  repaymentSplits: RepaymentSplitView[],
): DashboardMetrics {
  return {
    totalSpend: sum(transactions.map((transaction) => Number(transaction.amount))),
    totalOwnerShare: sum(
      transactions.map((transaction) => Number(transaction.ownerShareAmount)),
    ),
    pendingBorrowerAmount: sumByStatus(repaymentSplits, "PENDING"),
    reportedAmount: sumByStatus(repaymentSplits, "PAYMENT_REPORTED"),
    confirmedAmount: sumByStatus(repaymentSplits, "CONFIRMED"),
    totalTransactions: transactions.length,
    borrowedTransactions: transactions.filter((transaction) => transaction.borrowed)
      .length,
    pendingRepaymentsCount: repaymentSplits.filter(
      (split) => split.repaymentStatus === "PENDING",
    ).length,
  };
}

export function getRecentTransactions(
  transactions: TransactionResponse[],
  limit = 5,
): TransactionResponse[] {
  return [...transactions]
    .sort((a, b) => {
      const dateComparison =
        new Date(b.transactionDate).getTime() -
        new Date(a.transactionDate).getTime();

      if (dateComparison !== 0) {
        return dateComparison;
      }

      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    })
    .slice(0, limit);
}

export function getPendingRepayments(
  repaymentSplits: RepaymentSplitView[],
  limit = 5,
): RepaymentSplitView[] {
  return repaymentSplits
    .filter((split) => split.repaymentStatus === "PENDING")
    .sort((a, b) => {
      return (
        new Date(b.transactionDate).getTime() -
        new Date(a.transactionDate).getTime()
      );
    })
    .slice(0, limit);
}

function sum(values: number[]): number {
  return values.reduce((total, value) => total + value, 0);
}

function sumByStatus(
  repaymentSplits: RepaymentSplitView[],
  status: RepaymentSplitView["repaymentStatus"],
): number {
  return repaymentSplits
    .filter((split) => split.repaymentStatus === status)
    .reduce((total, split) => total + Number(split.splitAmount), 0);
}