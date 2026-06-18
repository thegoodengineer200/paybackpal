import { useState, type FormEvent } from "react";
import { Button } from "../../components/Button";
import { Card, CardContent, CardHeader } from "../../components/Card";
import { ErrorState } from "../../components/ErrorState";
import { Input } from "../../components/Input";
import type {
  CreateCreditCardRequest,
  CreditCard,
  CreditCardFormValues,
} from "./cardTypes";

type CreditCardFormProps = {
  mode: "create" | "edit";
  initialCard?: CreditCard | null;
  isSubmitting: boolean;
  errorMessage?: string | null;
  onCancel: () => void;
  onSubmit: (payload: CreateCreditCardRequest) => void;
};

const emptyFormValues: CreditCardFormValues = {
  cardName: "",
  bankName: "",
  lastFourDigits: "",
  billingCycleDay: "1",
  dueDay: "1",
};

export function CreditCardForm({
  mode,
  initialCard,
  isSubmitting,
  errorMessage,
  onCancel,
  onSubmit,
}: CreditCardFormProps) {
  const [formValues, setFormValues] = useState<CreditCardFormValues>(() => {
    if (!initialCard) {
      return emptyFormValues;
    }

    return {
      cardName: initialCard.cardName,
      bankName: initialCard.bankName,
      lastFourDigits: initialCard.lastFourDigits,
      billingCycleDay: String(initialCard.billingCycleDay),
      dueDay: String(initialCard.dueDay),
    };
  });

  const [validationError, setValidationError] = useState<string | null>(null);

  function updateField(
    field: keyof CreditCardFormValues,
    value: string,
  ) {
    setFormValues((current) => ({
      ...current,
      [field]: value,
    }));
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const payload = buildPayload(formValues);

    if (!payload) {
      return;
    }

    onSubmit(payload);
  }

  function buildPayload(
    values: CreditCardFormValues,
  ): CreateCreditCardRequest | null {
    setValidationError(null);

    const cardName = values.cardName.trim();
    const bankName = values.bankName.trim();
    const lastFourDigits = values.lastFourDigits.trim();
    const billingCycleDay = Number(values.billingCycleDay);
    const dueDay = Number(values.dueDay);

    if (!cardName) {
      setValidationError("Card name is required.");
      return null;
    }

    if (!bankName) {
      setValidationError("Bank name is required.");
      return null;
    }

    if (!/^\d{4}$/.test(lastFourDigits)) {
      setValidationError("Last four digits must be exactly 4 digits.");
      return null;
    }

    if (!isValidDay(billingCycleDay)) {
      setValidationError("Billing cycle day must be between 1 and 31.");
      return null;
    }

    if (!isValidDay(dueDay)) {
      setValidationError("Due day must be between 1 and 31.");
      return null;
    }

    return {
      cardName,
      bankName,
      lastFourDigits,
      billingCycleDay,
      dueDay,
    };
  }

  return (
    <Card>
      <CardHeader>
        <h2 className="text-base font-semibold text-gray-900">
          {mode === "create" ? "Add credit card" : "Edit credit card"}
        </h2>
        <p className="mt-1 text-sm text-gray-500">
          Add billing details so transactions can be tracked card-wise.
        </p>
      </CardHeader>

      <CardContent>
        {(validationError || errorMessage) && (
          <div className="mb-5">
            <ErrorState message={validationError ?? errorMessage ?? ""} />
          </div>
        )}

        <form onSubmit={handleSubmit} className="grid gap-5 md:grid-cols-2">
          <Input
            label="Card name"
            type="text"
            value={formValues.cardName}
            onChange={(event) => updateField("cardName", event.target.value)}
            placeholder="HDFC Millennia"
            required
          />

          <Input
            label="Bank name"
            type="text"
            value={formValues.bankName}
            onChange={(event) => updateField("bankName", event.target.value)}
            placeholder="HDFC Bank"
            required
          />

          <Input
            label="Last four digits"
            type="text"
            inputMode="numeric"
            maxLength={4}
            value={formValues.lastFourDigits}
            onChange={(event) =>
              updateField(
                "lastFourDigits",
                event.target.value.replace(/\D/g, "").slice(0, 4),
              )
            }
            placeholder="1234"
            required
          />

          <Input
            label="Billing cycle day"
            type="number"
            min={1}
            max={31}
            value={formValues.billingCycleDay}
            onChange={(event) =>
              updateField("billingCycleDay", event.target.value)
            }
            required
          />

          <Input
            label="Due day"
            type="number"
            min={1}
            max={31}
            value={formValues.dueDay}
            onChange={(event) => updateField("dueDay", event.target.value)}
            required
          />

          <div className="flex items-end gap-3 md:col-span-2">
            <Button type="submit" isLoading={isSubmitting}>
              {mode === "create" ? "Add card" : "Save changes"}
            </Button>

            <Button
              type="button"
              variant="secondary"
              onClick={onCancel}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}

function isValidDay(day: number): boolean {
  return Number.isInteger(day) && day >= 1 && day <= 31;
}