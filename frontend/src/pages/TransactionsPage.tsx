import { Button } from "../components/Button";
import { EmptyState } from "../components/EmptyState";
import { PageHeader } from "../components/PageHeader";

export function TransactionsPage() {
  return (
    <section>
      <PageHeader
        title="Transactions"
        description="Log personal card expenses and borrowed transactions with split tracking."
        action={<Button type="button">Log transaction</Button>}
      />

      <EmptyState
        title="No transactions yet"
        description="Once you add a card, you can start logging expenses and borrower splits."
        action={<Button type="button">Log transaction</Button>}
      />
    </section>
  );
}