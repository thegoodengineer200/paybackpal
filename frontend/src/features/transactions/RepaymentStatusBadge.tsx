import { cn } from "../../utils/cn";

type RepaymentStatusBadgeProps = {
  status: string;
};

const statusClasses: Record<string, string> = {
  PENDING: "bg-yellow-50 text-yellow-800 ring-yellow-200",
  PAYMENT_REPORTED: "bg-blue-50 text-blue-800 ring-blue-200",
  CONFIRMED: "bg-green-50 text-green-800 ring-green-200",
  CANCELLED: "bg-gray-100 text-gray-700 ring-gray-300",
};

const statusLabels: Record<string, string> = {
  PENDING: "Pending",
  PAYMENT_REPORTED: "Payment reported",
  CONFIRMED: "Confirmed",
  CANCELLED: "Cancelled",
};

export function RepaymentStatusBadge({ status }: RepaymentStatusBadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset",
        statusClasses[status] ?? "bg-gray-100 text-gray-700 ring-gray-300",
      )}
    >
      {statusLabels[status] ?? status}
    </span>
  );
}