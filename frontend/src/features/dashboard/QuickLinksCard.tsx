import { Link } from "react-router";
import { Card, CardContent, CardHeader } from "../../components/Card";

const quickLinks = [
  {
    to: "/transactions",
    title: "Log transaction",
    description: "Add a personal or borrowed card expense.",
    icon: "🧾",
  },
  {
    to: "/cards",
    title: "Manage cards",
    description: "Add or update your credit cards.",
    icon: "💳",
  },
  {
    to: "/borrowers",
    title: "Manage borrowers",
    description: "Add friends and repayment contacts.",
    icon: "👥",
  },
  {
    to: "/repayments",
    title: "Track repayments",
    description: "Confirm payments and send reminders.",
    icon: "✅",
  },
];

export function QuickLinksCard() {
  return (
    <Card>
      <CardHeader>
        <h2 className="text-base font-semibold text-gray-900">Quick actions</h2>
        <p className="mt-1 text-sm text-gray-500">
          Common PayBackPal workflows.
        </p>
      </CardHeader>

      <CardContent>
        <div className="grid gap-3 sm:grid-cols-2">
          {quickLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className="rounded-xl border border-gray-200 p-4 transition hover:bg-gray-50"
            >
              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gray-900 text-white">
                  {link.icon}
                </div>

                <div>
                  <p className="font-semibold text-gray-900">{link.title}</p>
                  <p className="mt-1 text-sm text-gray-500">
                    {link.description}
                  </p>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}