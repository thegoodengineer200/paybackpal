export type SplitMode = "equal" | "percentage";

export type TransactionSplitRequest = {
  borrowerId: string;
  splitPercentage?: string;
};

export type CreateTransactionRequest = {
  amount: string;
  description?: string;
  merchantName?: string;
  transactionDate: string;
  borrowed: boolean;
  splits?: TransactionSplitRequest[];
};

export type CreateTransactionPayload = {
  cardId: string;
  request: CreateTransactionRequest;
};

export type TransactionSplitResponse = {
  id: string;
  borrowerId: string;
  borrowerName: string;
  borrowerPhoneNumber: string;
  splitPercentage: number;
  splitAmount: number;
  repaymentStatus: string;
};

export type TransactionResponse = {
  id: string;
  cardId: string;
  cardName: string;
  bankName: string;
  lastFourDigits: string;
  amount: number;
  description?: string | null;
  merchantName?: string | null;
  transactionDate: string;
  borrowed: boolean;
  ownerShareAmount: number;
  splits: TransactionSplitResponse[];
  createdAt: string;
  updatedAt: string;
};

export type TransactionFormSplit = {
  rowId: string;
  borrowerId: string;
  splitPercentage: string;
};