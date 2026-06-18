import { Button } from "../components/Button";
import { EmptyState } from "../components/EmptyState";
import { PageHeader } from "../components/PageHeader";

export function CardsPage() {
  return (
    <section>
      <PageHeader
        title="Credit Cards"
        description="Manage the credit cards you use to log expenses."
        action={<Button type="button">Add card</Button>}
      />

      <EmptyState
        title="No cards yet"
        description="Add your first credit card to start logging personal and borrowed transactions."
        action={<Button type="button">Add card</Button>}
      />
    </section>
  );
}