import { useState, type FormEvent } from "react";
import { Button } from "../../components/Button";
import { Card, CardContent, CardHeader } from "../../components/Card";
import { ErrorState } from "../../components/ErrorState";
import { Input } from "../../components/Input";
import type {
  Borrower,
  BorrowerFormValues,
  CreateBorrowerRequest,
} from "./borrowerTypes";

type BorrowerFormProps = {
  mode: "create" | "edit";
  initialBorrower?: Borrower | null;
  isSubmitting: boolean;
  errorMessage?: string | null;
  onCancel: () => void;
  onSubmit: (payload: CreateBorrowerRequest) => void;
};

const emptyFormValues: BorrowerFormValues = {
  name: "",
  phoneNumber: "",
};

export function BorrowerForm({
  mode,
  initialBorrower,
  isSubmitting,
  errorMessage,
  onCancel,
  onSubmit,
}: BorrowerFormProps) {
  const [formValues, setFormValues] = useState<BorrowerFormValues>(() => {
    if (!initialBorrower) {
      return emptyFormValues;
    }

    return {
      name: initialBorrower.name,
      phoneNumber: initialBorrower.phoneNumber,
    };
  });

  const [validationError, setValidationError] = useState<string | null>(null);

  function updateField(field: keyof BorrowerFormValues, value: string) {
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
    values: BorrowerFormValues,
  ): CreateBorrowerRequest | null {
    setValidationError(null);

    const name = values.name.trim();
    const phoneNumber = values.phoneNumber.trim();

    if (!name) {
      setValidationError("Borrower name is required.");
      return null;
    }

    if (!/^\d{10}$/.test(phoneNumber)) {
      setValidationError("Phone number must be exactly 10 digits.");
      return null;
    }

    return {
      name,
      phoneNumber,
    };
  }

  return (
    <Card>
      <CardHeader>
        <h2 className="text-base font-semibold text-gray-900">
          {mode === "create" ? "Add borrower" : "Edit borrower"}
        </h2>
        <p className="mt-1 text-sm text-gray-500">
          Add borrower contact details for WhatsApp repayment reminders.
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
            label="Borrower name"
            type="text"
            value={formValues.name}
            onChange={(event) => updateField("name", event.target.value)}
            placeholder="Alex"
            required
          />

          <Input
            label="Phone number"
            type="tel"
            inputMode="numeric"
            maxLength={10}
            value={formValues.phoneNumber}
            onChange={(event) =>
              updateField(
                "phoneNumber",
                event.target.value.replace(/\D/g, "").slice(0, 10),
              )
            }
            placeholder="9876500000"
            required
          />

          <div className="flex items-end gap-3 md:col-span-2">
            <Button type="submit" isLoading={isSubmitting}>
              {mode === "create" ? "Add borrower" : "Save changes"}
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