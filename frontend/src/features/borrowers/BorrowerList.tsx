import { Button } from "../../components/Button";
import { Card, CardContent } from "../../components/Card";
import type { Borrower } from "./borrowerTypes";

type BorrowerListProps = {
  borrowers: Borrower[];
  deletingBorrowerId: string | null;
  onEdit: (borrower: Borrower) => void;
  onDelete: (borrower: Borrower) => void;
};

export function BorrowerList({
  borrowers,
  deletingBorrowerId,
  onEdit,
  onDelete,
}: BorrowerListProps) {
  return (
    <div className="grid gap-4 lg:grid-cols-2">
      {borrowers.map((borrower) => (
        <Card key={borrower.id}>
          <CardContent>
            <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <div className="flex items-center gap-3">
                  <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gray-900 text-white">
                    {getInitials(borrower.name)}
                  </div>

                  <div>
                    <h2 className="text-base font-semibold text-gray-900">
                      {borrower.name}
                    </h2>
                    <p className="text-sm text-gray-500">
                      WhatsApp: {borrower.phoneNumber}
                    </p>
                  </div>
                </div>

                <div className="mt-5 grid gap-3 text-sm text-gray-600 sm:grid-cols-2">
                  <InfoItem label="Phone" value={borrower.phoneNumber} />
                  <InfoItem
                    label="Status"
                    value={borrower.active === false ? "Inactive" : "Active"}
                  />
                </div>
              </div>

              <div className="flex gap-2">
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => onEdit(borrower)}
                >
                  Edit
                </Button>

                <Button
                  type="button"
                  variant="danger"
                  size="sm"
                  isLoading={deletingBorrowerId === borrower.id}
                  onClick={() => onDelete(borrower)}
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

function getInitials(name: string): string {
  const parts = name
    .trim()
    .split(/\s+/)
    .filter(Boolean);

  if (parts.length === 0) {
    return "?";
  }

  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase();
  }

  return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
}