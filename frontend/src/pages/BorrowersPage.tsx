import { useState } from "react";
import { Button } from "../components/Button";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { BorrowerForm } from "../features/borrowers/BorrowerForm";
import { BorrowerList } from "../features/borrowers/BorrowerList";
import type {
  Borrower,
  CreateBorrowerRequest,
} from "../features/borrowers/borrowerTypes";
import {
  useBorrowers,
  useCreateBorrower,
  useDeleteBorrower,
  useUpdateBorrower,
} from "../features/borrowers/useBorrowers";

export function BorrowersPage() {
  const borrowersQuery = useBorrowers();
  const createBorrowerMutation = useCreateBorrower();
  const updateBorrowerMutation = useUpdateBorrower();
  const deleteBorrowerMutation = useDeleteBorrower();

  const [isCreateFormOpen, setIsCreateFormOpen] = useState(false);
  const [editingBorrower, setEditingBorrower] = useState<Borrower | null>(null);
  const [deletingBorrowerId, setDeletingBorrowerId] = useState<string | null>(
    null,
  );

  const borrowers = borrowersQuery.data ?? [];

  const isFormOpen = isCreateFormOpen || Boolean(editingBorrower);

  async function handleCreateBorrower(payload: CreateBorrowerRequest) {
    await createBorrowerMutation.mutateAsync(payload);
    setIsCreateFormOpen(false);
  }

  async function handleUpdateBorrower(payload: CreateBorrowerRequest) {
    if (!editingBorrower) {
      return;
    }

    await updateBorrowerMutation.mutateAsync({
      borrowerId: editingBorrower.id,
      payload,
    });

    setEditingBorrower(null);
  }

  async function handleDeleteBorrower(borrower: Borrower) {
    const shouldDelete = window.confirm(
      `Delete borrower ${borrower.name} (${borrower.phoneNumber})?`,
    );

    if (!shouldDelete) {
      return;
    }

    setDeletingBorrowerId(borrower.id);

    try {
      await deleteBorrowerMutation.mutateAsync(borrower.id);
    } finally {
      setDeletingBorrowerId(null);
    }
  }

  function closeForm() {
    setIsCreateFormOpen(false);
    setEditingBorrower(null);
    createBorrowerMutation.reset();
    updateBorrowerMutation.reset();
  }

  return (
    <section>
      <PageHeader
        title="Borrowers"
        description="Manage friends and contacts who borrow against your credit card transactions."
        action={
          !isFormOpen && (
            <Button type="button" onClick={() => setIsCreateFormOpen(true)}>
              Add borrower
            </Button>
          )
        }
      />

      <div className="space-y-6">
        {isCreateFormOpen && (
          <BorrowerForm
            mode="create"
            isSubmitting={createBorrowerMutation.isPending}
            errorMessage={getMutationErrorMessage(createBorrowerMutation.error)}
            onCancel={closeForm}
            onSubmit={handleCreateBorrower}
          />
        )}

        {editingBorrower && (
          <BorrowerForm
            key={editingBorrower.id}
            mode="edit"
            initialBorrower={editingBorrower}
            isSubmitting={updateBorrowerMutation.isPending}
            errorMessage={getMutationErrorMessage(updateBorrowerMutation.error)}
            onCancel={closeForm}
            onSubmit={handleUpdateBorrower}
          />
        )}

        {borrowersQuery.isLoading && (
          <LoadingState
            title="Loading borrowers"
            description="Fetching borrower contacts linked to your account."
          />
        )}

        {borrowersQuery.isError && (
          <ErrorState
            message={getQueryErrorMessage(borrowersQuery.error)}
            onRetry={() => void borrowersQuery.refetch()}
          />
        )}

        {borrowersQuery.isSuccess && borrowers.length === 0 && !isFormOpen && (
          <EmptyState
            title="No borrowers yet"
            description="Add borrower contacts so you can split credit card transactions and send WhatsApp repayment reminders."
            action={
              <Button
                type="button"
                onClick={() => setIsCreateFormOpen(true)}
              >
                Add borrower
              </Button>
            }
          />
        )}

        {borrowersQuery.isSuccess && borrowers.length > 0 && (
          <BorrowerList
            borrowers={borrowers}
            deletingBorrowerId={deletingBorrowerId}
            onEdit={(borrower) => {
              setIsCreateFormOpen(false);
              setEditingBorrower(borrower);
              createBorrowerMutation.reset();
              updateBorrowerMutation.reset();
            }}
            onDelete={handleDeleteBorrower}
          />
        )}

        {deleteBorrowerMutation.isError && (
          <ErrorState
            message={getMutationErrorMessage(deleteBorrowerMutation.error) ??
              "Failed to delete borrower. Please try again."
            }
          />
        )}
      </div>
    </section>
  );
}

function getQueryErrorMessage(error: unknown): string {
  return error instanceof Error
    ? error.message
    : "Unable to load borrowers.";
}

function getMutationErrorMessage(error: unknown): string | null {
  if (!error) {
    return null;
  }

  return error instanceof Error
    ? error.message
    : "Something went wrong. Please try again.";
}