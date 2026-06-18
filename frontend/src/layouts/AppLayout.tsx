import { NavLink, Outlet } from "react-router";

const navItems = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/cards", label: "Cards" },
  { to: "/borrowers", label: "Borrowers" },
  { to: "/transactions", label: "Transactions" },
  { to: "/repayments", label: "Repayments" },
];

export function AppLayout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <aside className="fixed inset-y-0 left-0 hidden w-64 border-r border-gray-200 bg-white px-5 py-6 md:block">
        <div className="mb-8">
          <h1 className="text-xl font-bold text-gray-900">PayBackPal</h1>
          <p className="mt-1 text-sm text-gray-500">
            Credit card repayments
          </p>
        </div>

        <nav className="space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                [
                  "block rounded-lg px-3 py-2 text-sm font-medium",
                  isActive
                    ? "bg-gray-900 text-white"
                    : "text-gray-700 hover:bg-gray-100",
                ].join(" ")
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <main className="min-h-screen md:pl-64">
        <header className="border-b border-gray-200 bg-white px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Welcome back</p>
              <h2 className="text-lg font-semibold text-gray-900">
                PayBackPal Dashboard
              </h2>
            </div>

            <button className="rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
              Logout
            </button>
          </div>
        </header>

        <div className="p-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
}