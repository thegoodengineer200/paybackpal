import { Button } from "../components/Button";
import { EmptyState } from "../components/EmptyState";
import { PageHeader } from "../components/PageHeader";

export function BorrowersPage() {
  return (
    <section>
      <PageHeader
        title="Borrowers"
        description="Manage friends and contacts who borrow against your card transactions."
        action={<Button type="button">Add borrower</Button>}
      />

      <EmptyState
        title="No borrowers yet"
        description="Add borrower contacts so you can split credit card transactions with them."
        action={<Button type="button">Add borrower</Button>}
      />
    </section>
  );
}