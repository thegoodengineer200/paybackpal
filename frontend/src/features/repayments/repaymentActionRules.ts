import type {
  RepaymentAction,
  RepaymentSplitView,
} from "./repaymentTypes";

export function getAvailableActions(
  split: RepaymentSplitView,
): RepaymentAction[] {
  switch (split.repaymentStatus) {
    case "PENDING":
      return ["report-paid", "remind", "cancel"];

    case "PAYMENT_REPORTED":
      return ["confirm", "remind", "cancel"];

    case "CONFIRMED":
      return [];

    case "CANCELLED":
      return [];
  }
}

export function getActionLabel(action: RepaymentAction): string {
  switch (action) {
    case "report-paid":
      return "Report paid";
    case "confirm":
      return "Confirm";
    case "cancel":
      return "Cancel";
    case "remind":
      return "Remind";
  }
}

export function getActionVariant(
  action: RepaymentAction,
): "primary" | "secondary" | "danger" | "ghost" {
  switch (action) {
    case "report-paid":
      return "secondary";
    case "confirm":
      return "primary";
    case "cancel":
      return "danger";
    case "remind":
      return "secondary";
  }
}