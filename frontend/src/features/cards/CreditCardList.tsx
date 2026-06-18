import { Button } from "../../components/Button";
import { Card, CardContent } from "../../components/Card";
import type { CreditCard } from "./cardTypes";

type CreditCardListProps = {
  cards: CreditCard[];
  deletingCardId: string | null;
  onEdit: (card: CreditCard) => void;
  onDelete: (card: CreditCard) => void;
};

export function CreditCardList({
  cards,
  deletingCardId,
  onEdit,
  onDelete,
}: CreditCardListProps) {
  return (
    <div className="grid gap-4 lg:grid-cols-2">
      {cards.map((card) => (
        <Card key={card.id}>
          <CardContent>
            <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <div className="flex items-center gap-3">
                  <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gray-900 text-white">
                    💳
                  </div>

                  <div>
                    <h2 className="text-base font-semibold text-gray-900">
                      {card.cardName}
                    </h2>
                    <p className="text-sm text-gray-500">
                      {card.bankName}
                    </p>
                  </div>
                </div>

                <div className="mt-5 grid gap-3 text-sm text-gray-600 sm:grid-cols-3">
                  <InfoItem label="Card" value={`•••• ${card.lastFourDigits}`} />
                  <InfoItem
                    label="Billing day"
                    value={`Day ${card.billingCycleDay}`}
                  />
                  <InfoItem label="Due day" value={`Day ${card.dueDay}`} />
                </div>
              </div>

              <div className="flex gap-2">
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => onEdit(card)}
                >
                  Edit
                </Button>

                <Button
                  type="button"
                  variant="danger"
                  size="sm"
                  isLoading={deletingCardId === card.id}
                  onClick={() => onDelete(card)}
                >
                  Delete
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

type InfoItemProps = {
  label: string;
  value: string;
};

function InfoItem({ label, value }: InfoItemProps) {
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
        {label}
      </p>
      <p className="mt-1 font-medium text-gray-800">{value}</p>
    </div>
  );
}