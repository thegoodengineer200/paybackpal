import { Card, CardContent } from "../components/Card";
import { PageHeader } from "../components/PageHeader";

const summaryCards = [
  {
    label: "Total spend",
    value: "₹0.00",
    description: "Across your credit cards",
  },
  {
    label: "Pending repayments",
    value: "₹0.00",
    description: "Borrower amounts not settled",
  },
  {
    label: "Confirmed repayments",
    value: "₹0.00",
    description: "Payments confirmed by you",
  },
];

export function DashboardPage() {
  return (
    <section>
      <PageHeader
        title="Dashboard"
        description="Track your credit card spending, borrowed splits, and repayment status."
      />

      <div className="grid gap-4 md:grid-cols-3">
        {summaryCards.map((card) => (
          <Card key={card.label}>
            <CardContent>
              <p className="text-sm font-medium text-gray-500">
                {card.label}
              </p>
              <p className="mt-3 text-2xl font-bold text-gray-900">
                {card.value}
              </p>
              <p className="mt-1 text-sm text-gray-500">
                {card.description}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}