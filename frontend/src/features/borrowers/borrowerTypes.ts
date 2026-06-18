export type Borrower = {
  id: string;
  name: string;
  phoneNumber: string;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
};

export type BorrowerFormValues = {
  name: string;
  phoneNumber: string;
};

export type CreateBorrowerRequest = {
  name: string;
  phoneNumber: string;
};

export type UpdateBorrowerRequest = CreateBorrowerRequest;