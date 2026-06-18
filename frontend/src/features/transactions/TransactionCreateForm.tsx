import { useMemo, useState, type FormEvent } from "react";
import { Button } from "../../components/Button";
import { Card, CardContent, CardHeader } from "../../components/Card";
import { ErrorState } from "../../components/ErrorState";
import { Input } from "../../components/Input";
import type { Borrower } from "../borrowers/borrowerTypes";
import type { CreditCard } from "../cards/cardTypes";
import { formatMoney } from "../../utils/formatMoney";
import type {
  CreateTransactionRequest,
  SplitMode,
  TransactionFormSplit,
} from "./transactionTypes";

type TransactionCreateFormProps = {
  cards: CreditCard[];
  borrowers: Borrower[];
  isSubmitting: boolean;
  errorMessage?: string | null;
  onSubmit: (cardId: string, request: CreateTransactionRequest) => void;
};

const today = new Date().toISOString().slice(0, 10);

export function TransactionCreateForm({
  cards,
  borrowers,
  isSubmitting,
  errorMessage,
  onSubmit,
}: TransactionCreateFormProps) {
  const [selectedCardId, setSelectedCardId] = useState(cards[0]?.id ?? "");
  const [amount, setAmount] = useState("");
  const [merchantName, setMerchantName] = useState("");
  const [description, setDescription] = useState("");
  const [transactionDate, setTransactionDate] = useState(today);

  const [borrowed, setBorrowed] = useState(false);
  const [splitMode, setSplitMode] = useState<SplitMode>("equal");
  const [splits, setSplits] = useState<TransactionFormSplit[]>([
    createEmptySplit(),
  ]);

  const [validationError, setValidationError] = useState<string | null>(null);

  const amountValue = Number(amount || 0);

  const splitPreview = useMemo(
    () =>
      calculateSplitPreview({
        amount: amountValue,
        borrowed,
        splitMode,
        splits,
      }),
    [amountValue, borrowed, splitMode, splits],
  );

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const request = buildRequest();

    if (!request) {
      return;
    }

    onSubmit(selectedCardId, request);
  }

  function buildRequest(): CreateTransactionRequest | null {
    setValidationError(null);

    if (!selectedCardId) {
      setValidationError("Please select a credit card.");
      return null;
    }

    if (!amount || Number(amount) <= 0) {
      setValidationError("Amount must be greater than 0.");
      return null;
    }

    if (!transactionDate) {
      setValidationError("Transaction date is required.");
      return null;
    }

    if (!merchantName.trim() && !description.trim()) {
      setValidationError("Add either merchant name or description.");
      return null;
    }

    if (!borrowed) {
      return {
        amount: normalizeMoney(amount),
        merchantName: merchantName.trim() || undefined,
        description: description.trim() || undefined,
        transactionDate,
        borrowed: false,
      };
    }

    if (borrowers.length === 0) {
      setValidationError("Add at least one borrower before creating a borrowed transaction.");
      return null;
    }

    const selectedSplits = splits.filter((split) => split.borrowerId);

    if (selectedSplits.length === 0) {
      setValidationError("Select at least one borrower for the split.");
      return null;
    }

    const uniqueBorrowerIds = new Set(
      selectedSplits.map((split) => split.borrowerId),
    );

    if (uniqueBorrowerIds.size !== selectedSplits.length) {
      setValidationError("The same borrower cannot be selected more than once.");
      return null;
    }

    if (splitMode === "percentage") {
      for (const split of selectedSplits) {
        const percentage = Number(split.splitPercentage);

        if (!split.splitPercentage || percentage <= 0) {
          setValidationError("Every borrower must have a split percentage greater than 0.");
          return null;
        }

        if (percentage > 100) {
          setValidationError("Split percentage cannot be more than 100.");
          return null;
        }
      }

      if (splitPreview.totalBorrowerPercentage > 100) {
        setValidationError("Total borrower percentage cannot exceed 100%.");
        return null;
      }
    }

    return {
      amount: normalizeMoney(amount),
      merchantName: merchantName.trim() || undefined,
      description: description.trim() || undefined,
      transactionDate,
      borrowed: true,
      splits: selectedSplits.map((split) => ({
        borrowerId: split.borrowerId,
        splitPercentage:
          splitMode === "percentage"
            ? normalizePercentage(split.splitPercentage)
            : undefined,
      })),
    };
  }

  function addSplitRow() {
    setSplits((current) => [...current, createEmptySplit()]);
  }

  function removeSplitRow(rowId: string) {
    setSplits((current) => {
      if (current.length === 1) {
        return current;
      }

      return current.filter((split) => split.rowId !== rowId);
    });
  }

  function updateSplit(
    rowId: string,
    field: keyof Omit<TransactionFormSplit, "rowId">,
    value: string,
  ) {
    setSplits((current) =>
      current.map((split) =>
        split.rowId === rowId
          ? {
              ...split,
              [field]: value,
            }
          : split,
      ),
    );
  }

  return (
    <Card>
      <CardHeader>
        <h2 className="text-base font-semibold text-gray-900">
          Log transaction
        </h2>
        <p className="mt-1 text-sm text-gray-500">
          Create a personal card expense or split it with borrowers.
        </p>
      </CardHeader>

      <CardContent>
        {(validationError || errorMessage) && (
          <div className="mb-5">
            <ErrorState message={validationError ?? errorMessage ?? ""} />
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-8">
          <section className="grid gap-5 md:grid-cols-2">
            <div>
              <label
                htmlFor="cardId"
                className="block text-sm font-medium text-gray-700"
              >
                Credit card
              </label>

              <select
                id="cardId"
                value={selectedCardId}
                onChange={(event) => setSelectedCardId(event.target.value)}
                className="mt-2 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-gray-900 focus:ring-1 focus:ring-gray-900"
                required
              >
                <option value="">Select card</option>
                {cards.map((card) => (
                  <option key={card.id} value={card.id}>
                    {card.cardName} •••• {card.lastFourDigits}
                  </option>
                ))}
              </select>
            </div>

            <Input
              label="Amount"
              type="number"
              min="0.01"
              step="0.01"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
              placeholder="2500.00"
              required
            />

            <Input
              label="Merchant name"
              type="text"
              value={merchantName}
              onChange={(event) => setMerchantName(event.target.value)}
              placeholder="Pizza Express"
            />

            <Input
              label="Transaction date"
              type="date"
              value={transactionDate}
              onChange={(event) => setTransactionDate(event.target.value)}
              required
            />

            <div className="md:col-span-2">
              <label
                htmlFor="description"
                className="block text-sm font-medium text-gray-700"
              >
                Description
              </label>

              <textarea
                id="description"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                placeholder="Dinner with friends"
                rows={3}
                className="mt-2 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-gray-900 focus:ring-1 focus:ring-gray-900"
              />
            </div>
          </section>

          <section className="rounded-2xl border border-gray-200 bg-gray-50 p-5">
            <div className="flex items-start gap-3">
              <input
                id="borrowed"
                type="checkbox"
                checked={borrowed}
                onChange={(event) => setBorrowed(event.target.checked)}
                className="mt-1 h-4 w-4 rounded border-gray-300 text-gray-900"
              />

              <div>
                <label
                  htmlFor="borrowed"
                  className="text-sm font-semibold text-gray-900"
                >
                  This transaction is borrowed/shared
                </label>
                <p className="mt-1 text-sm text-gray-500">
                  Enable this to split the amount with borrowers and trigger repayment tracking.
                </p>
              </div>
            </div>

            {borrowed && (
              <div className="mt-6 space-y-6">
                <div>
                  <p className="text-sm font-medium text-gray-700">
                    Split mode
                  </p>

                  <div className="mt-2 flex flex-wrap gap-3">
                    <label className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm">
                      <input
                        type="radio"
                        name="splitMode"
                        value="equal"
                        checked={splitMode === "equal"}
                        onChange={() => setSplitMode("equal")}
                      />
                      Equal split with owner
                    </label>

                    <label className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm">
                      <input
                        type="radio"
                        name="splitMode"
                        value="percentage"
                        checked={splitMode === "percentage"}
                        onChange={() => setSplitMode("percentage")}
                      />
                      Percentage split
                    </label>
                  </div>
                </div>

                <div className="space-y-3">
                  {splits.map((split, index) => (
                    <div
                      key={split.rowId}
                      className="grid gap-3 rounded-xl border border-gray-200 bg-white p-4 md:grid-cols-[1fr_180px_auto]"
                    >
                      <div>
                        <label
                          htmlFor={`borrower-${split.rowId}`}
                          className="block text-sm font-medium text-gray-700"
                        >
                          Borrower {index + 1}
                        </label>

                        <select
                          id={`borrower-${split.rowId}`}
                          value={split.borrowerId}
                          onChange={(event) =>
                            updateSplit(
                              split.rowId,
                              "borrowerId",
                              event.target.value,
                            )
                          }
                          className="mt-2 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-gray-900 focus:ring-1 focus:ring-gray-900"
                        >
                          <option value="">Select borrower</option>
                          {borrowers.map((borrower) => (
                            <option key={borrower.id} value={borrower.id}>
                              {borrower.name} — {borrower.phoneNumber}
                            </option>
                          ))}
                        </select>
                      </div>

                      {splitMode === "percentage" && (
                        <Input
                          label="Percentage"
                          type="number"
                          min="0.01"
                          max="100"
                          step="0.01"
                          value={split.splitPercentage}
                          onChange={(event) =>
                            updateSplit(
                              split.rowId,
                              "splitPercentage",
                              event.target.value,
                            )
                          }
                          placeholder="25"
                        />
                      )}

                      <div className="flex items-end">
                        <Button
                          type="button"
                          variant="ghost"
                          onClick={() => removeSplitRow(split.rowId)}
                          disabled={splits.length === 1}
                        >
                          Remove
                        </Button>
                      </div>
                    </div>
                  ))}

                  <Button type="button" variant="secondary" onClick={addSplitRow}>
                    Add borrower
                  </Button>
                </div>

                <SplitPreview
                  amount={amountValue}
                  borrowed={borrowed}
                  splitMode={splitMode}
                  splits={splits}
                  borrowerCount={splitPreview.selectedBorrowerCount}
                  ownerShareAmount={splitPreview.ownerShareAmount}
                  borrowerShareAmount={splitPreview.equalBorrowerShareAmount}
                  totalBorrowerPercentage={splitPreview.totalBorrowerPercentage}
                />
              </div>
            )}
          </section>

          {!borrowed && (
            <SplitPreview
              amount={amountValue}
              borrowed={borrowed}
              splitMode={splitMode}
              splits={splits}
              borrowerCount={0}
              ownerShareAmount={amountValue}
              borrowerShareAmount={0}
              totalBorrowerPercentage={0}
            />
          )}

          <Button type="submit" isLoading={isSubmitting}>
            Save transaction
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

type SplitPreviewProps = {
  amount: number;
  borrowed: boolean;
  splitMode: SplitMode;
  splits: TransactionFormSplit[];
  borrowerCount: number;
  ownerShareAmount: number;
  borrowerShareAmount: number;
  totalBorrowerPercentage: number;
};

function SplitPreview({
  amount,
  borrowed,
  splitMode,
  borrowerCount,
  ownerShareAmount,
  borrowerShareAmount,
  totalBorrowerPercentage,
}: SplitPreviewProps) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5">
      <h3 className="text-sm font-semibold text-gray-900">
        Owner share preview
      </h3>

      {!borrowed ? (
        <p className="mt-2 text-sm text-gray-600">
          This is a personal transaction. Your expense will be{" "}
          <span className="font-semibold text-gray-900">
            {formatMoney(amount || 0)}
          </span>
          .
        </p>
      ) : splitMode === "equal" ? (
        <div className="mt-3 grid gap-3 text-sm md:grid-cols-3">
          <PreviewItem label="Total amount" value={formatMoney(amount || 0)} />
          <PreviewItem
            label="Each borrower"
            value={
              borrowerCount > 0
                ? formatMoney(borrowerShareAmount)
                : "Select borrowers"
            }
          />
          <PreviewItem
            label="Owner share"
            value={formatMoney(ownerShareAmount)}
          />
        </div>
      ) : (
        <div className="mt-3 grid gap-3 text-sm md:grid-cols-3">
          <PreviewItem label="Total amount" value={formatMoney(amount || 0)} />
          <PreviewItem
            label="Borrower percentage"
            value={`${totalBorrowerPercentage.toFixed(2)}%`}
          />
          <PreviewItem
            label="Owner share"
            value={formatMoney(ownerShareAmount)}
          />
        </div>
      )}
    </div>
  );
}

type PreviewItemProps = {
  label: string;
  value: string;
};

function PreviewItem({ label, value }: PreviewItemProps) {
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
        {label}
      </p>
      <p className="mt-1 font-semibold text-gray-900">{value}</p>
    </div>
  );
}

function createEmptySplit(): TransactionFormSplit {
  return {
    rowId: crypto.randomUUID(),
    borrowerId: "",
    splitPercentage: "",
  };
}

function normalizeMoney(value: string): string {
  return Number(value).toFixed(2);
}

function normalizePercentage(value: string): string {
  return Number(value).toFixed(2);
}

function calculateSplitPreview({
  amount,
  borrowed,
  splitMode,
  splits,
}: {
  amount: number;
  borrowed: boolean;
  splitMode: SplitMode;
  splits: TransactionFormSplit[];
}) {
  const selectedBorrowerCount = splits.filter((split) => split.borrowerId).length;

  if (!borrowed || amount <= 0) {
    return {
      selectedBorrowerCount,
      equalBorrowerShareAmount: 0,
      ownerShareAmount: amount || 0,
      totalBorrowerPercentage: 0,
    };
  }

  if (splitMode === "equal") {
    const totalPeople = selectedBorrowerCount + 1;
    const share = totalPeople > 0 ? amount / totalPeople : 0;

    return {
      selectedBorrowerCount,
      equalBorrowerShareAmount: selectedBorrowerCount > 0 ? share : 0,
      ownerShareAmount: selectedBorrowerCount > 0 ? share : amount,
      totalBorrowerPercentage: 0,
    };
  }

  const totalBorrowerPercentage = splits.reduce((sum, split) => {
    if (!split.borrowerId) {
      return sum;
    }

    return sum + Number(split.splitPercentage || 0);
  }, 0);

  const ownerPercentage = Math.max(0, 100 - totalBorrowerPercentage);
  const ownerShareAmount = (amount * ownerPercentage) / 100;

  return {
    selectedBorrowerCount,
    equalBorrowerShareAmount: 0,
    ownerShareAmount,
    totalBorrowerPercentage,
  };
}