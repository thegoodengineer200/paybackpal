import { useState } from "react";
import { Button } from "../components/Button";
import { EmptyState } from "../components/EmptyState";
import { ErrorState } from "../components/ErrorState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { CreditCardForm } from "../features/cards/CreditCardForm";
import { CreditCardList } from "../features/cards/CreditCardList";
import type {
  CreateCreditCardRequest,
  CreditCard,
} from "../features/cards/cardTypes";
import {
  useCreateCreditCard,
  useCreditCards,
  useDeleteCreditCard,
  useUpdateCreditCard,
} from "../features/cards/useCreditCards";

export function CardsPage() {
  const cardsQuery = useCreditCards();
  const createCardMutation = useCreateCreditCard();
  const updateCardMutation = useUpdateCreditCard();
  const deleteCardMutation = useDeleteCreditCard();

  const [isCreateFormOpen, setIsCreateFormOpen] = useState(false);
  const [editingCard, setEditingCard] = useState<CreditCard | null>(null);
  const [deletingCardId, setDeletingCardId] = useState<string | null>(null);

  const cards = cardsQuery.data ?? [];

  const isFormOpen = isCreateFormOpen || Boolean(editingCard);

  async function handleCreateCard(payload: CreateCreditCardRequest) {
    await createCardMutation.mutateAsync(payload);
    setIsCreateFormOpen(false);
  }

  async function handleUpdateCard(payload: CreateCreditCardRequest) {
    if (!editingCard) {
      return;
    }

    await updateCardMutation.mutateAsync({
      cardId: editingCard.id,
      payload,
    });

    setEditingCard(null);
  }

  async function handleDeleteCard(card: CreditCard) {
    const shouldDelete = window.confirm(
      `Delete ${card.cardName} ending with ${card.lastFourDigits}?`,
    );

    if (!shouldDelete) {
      return;
    }

    setDeletingCardId(card.id);

    try {
      await deleteCardMutation.mutateAsync(card.id);
    } finally {
      setDeletingCardId(null);
    }
  }

  function closeForm() {
    setIsCreateFormOpen(false);
    setEditingCard(null);
    createCardMutation.reset();
    updateCardMutation.reset();
  }

  return (
    <section>
      <PageHeader
        title="Credit Cards"
        description="Manage the credit cards you use to log expenses and borrower splits."
        action={
          !isFormOpen && (
            <Button type="button" onClick={() => setIsCreateFormOpen(true)}>
              Add card
            </Button>
          )
        }
      />

      <div className="space-y-6">
        {isCreateFormOpen && (
          <CreditCardForm
            mode="create"
            isSubmitting={createCardMutation.isPending}
            errorMessage={getMutationErrorMessage(createCardMutation.error)}
            onCancel={closeForm}
            onSubmit={handleCreateCard}
          />
        )}

        {editingCard && (
          <CreditCardForm
            key={editingCard.id}
            mode="edit"
            initialCard={editingCard}
            isSubmitting={updateCardMutation.isPending}
            errorMessage={getMutationErrorMessage(updateCardMutation.error)}
            onCancel={closeForm}
            onSubmit={handleUpdateCard}
          />
        )}

        {cardsQuery.isLoading && (
          <LoadingState
            title="Loading cards"
            description="Fetching your saved credit cards."
          />
        )}

        {cardsQuery.isError && (
          <ErrorState
            message={getQueryErrorMessage(cardsQuery.error)}
            onRetry={() => void cardsQuery.refetch()}
          />
        )}

        {cardsQuery.isSuccess && cards.length === 0 && !isFormOpen && (
          <EmptyState
            title="No cards yet"
            description="Add your first credit card to start logging personal and borrowed transactions."
            action={
              <Button type="button" onClick={() => setIsCreateFormOpen(true)}>
                Add card
              </Button>
            }
          />
        )}

        {cardsQuery.isSuccess && cards.length > 0 && (
          <CreditCardList
            cards={cards}
            deletingCardId={deletingCardId}
            onEdit={(card) => {
              setIsCreateFormOpen(false);
              setEditingCard(card);
              createCardMutation.reset();
              updateCardMutation.reset();
            }}
            onDelete={handleDeleteCard}
          />
        )}

        {deleteCardMutation.isError && (
          <ErrorState
            message={
              getMutationErrorMessage(deleteCardMutation.error) ??
              "Unable to delete credit card."
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
    : "Unable to load credit cards.";
}

function getMutationErrorMessage(error: unknown): string | null {
  if (!error) {
    return null;
  }

  return error instanceof Error
    ? error.message
    : "Something went wrong. Please try again.";
}
