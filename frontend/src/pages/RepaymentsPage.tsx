import { EmptyState } from "../components/EmptyState";
import { PageHeader } from "../components/PageHeader";

export function RepaymentsPage() {
  return (
    <section>
      <PageHeader
        title="Repayments"
        description="Track borrower split statuses, reminders, reports, and confirmations."
      />

      <EmptyState
        title="No repayments to track"
        description="Borrowed transaction splits will appear here once you start logging shared card expenses."
      />
    </section>
  );
}