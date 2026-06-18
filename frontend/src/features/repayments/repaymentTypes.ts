export type RepaymentStatus =
  | "PENDING"
  | "PAYMENT_REPORTED"
  | "CONFIRMED"
  | "CANCELLED";

export type RepaymentSplitView = {
  splitId: string;
  transactionId: string;
  transactionTitle: string;
  transactionDate: string;

  cardId: string;
  cardName: string;
  bankName: string;
  lastFourDigits: string;

  borrowerId: string;
  borrowerName: string;
  borrowerPhoneNumber: string;

  splitPercentage: number;
  splitAmount: number;
  repaymentStatus: RepaymentStatus;
};

export type RepaymentAction =
  | "report-paid"
  | "confirm"
  | "cancel"
  | "remind";

export type NotificationOutboxResponse = {
  id: string;
  transactionSplitId: string;
  channel: string;
  notificationType: string;
  recipientPhoneNumber: string;
  messageBody: string;
  status: string;
  scheduledAt: string;
  retryCount: number;
};