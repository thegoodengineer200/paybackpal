export type CreditCard = {
  id: string;
  cardName: string;
  bankName: string;
  lastFourDigits: string;
  billingCycleDay: number;
  dueDay: number;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
};

export type CreditCardFormValues = {
  cardName: string;
  bankName: string;
  lastFourDigits: string;
  billingCycleDay: string;
  dueDay: string;
};

export type CreateCreditCardRequest = {
  cardName: string;
  bankName: string;
  lastFourDigits: string;
  billingCycleDay: number;
  dueDay: number;
};

export type UpdateCreditCardRequest = CreateCreditCardRequest;