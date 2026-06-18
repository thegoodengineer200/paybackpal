import { useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router";
import { Button } from "../components/Button";
import { cn } from "../utils/cn";
import { useAuth } from "../features/auth/AuthContext";

const navItems = [
  { to: "/dashboard", label: "Dashboard", icon: "📊" },
  { to: "/cards", label: "Cards", icon: "💳" },
  { to: "/borrowers", label: "Borrowers", icon: "👥" },
  { to: "/transactions", label: "Transactions", icon: "🧾" },
  { to: "/repayments", label: "Repayments", icon: "✅" },
];

const routeTitles: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/cards": "Credit Cards",
  "/borrowers": "Borrowers",
  "/transactions": "Transactions",
  "/repayments": "Repayments",
};

export function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();

  const [isMobileNavOpen, setIsMobileNavOpen] = useState(false);

  const currentTitle = getCurrentTitle(location.pathname);

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  function closeMobileNav() {
    setIsMobileNavOpen(false);
  }

  function getCurrentTitle(pathname: string): string {
  if (pathname.startsWith("/transactions/")) {
    return "Transaction detail";
  }

  return routeTitles[pathname] ?? "PayBackPal";
}

  return (
    <div className="min-h-screen bg-gray-50">
      <DesktopSidebar onLogout={handleLogout} />

      {isMobileNavOpen && (
        <div className="fixed inset-0 z-40 md:hidden">
          <button
            type="button"
            aria-label="Close navigation overlay"
            className="absolute inset-0 bg-gray-900/40"
            onClick={closeMobileNav}
          />

          <div className="relative h-full w-72 bg-white shadow-xl">
            <MobileSidebar
              onLogout={handleLogout}
              onNavigate={closeMobileNav}
            />
          </div>
        </div>
      )}

      <main className="min-h-screen md:pl-64">
        <header className="sticky top-0 z-30 border-b border-gray-200 bg-white/95 px-4 py-3 backdrop-blur md:px-6">
          <div className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => setIsMobileNavOpen(true)}
                className="rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-700 md:hidden"
              >
                ☰
              </button>

              <div>
                <p className="text-xs font-medium uppercase tracking-wide text-gray-500">
                  PayBackPal
                </p>
                <h1 className="text-lg font-semibold text-gray-900">
                  {currentTitle}
                </h1>
              </div>
            </div>

            <Button
              type="button"
              variant="secondary"
              size="sm"
              onClick={handleLogout}
            >
              Logout
            </Button>
          </div>
        </header>

        <div className="p-4 md:p-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
}

type SidebarProps = {
  onLogout: () => void;
  onNavigate?: () => void;
};

function DesktopSidebar({ onLogout }: SidebarProps) {
  return (
    <aside className="fixed inset-y-0 left-0 hidden w-64 border-r border-gray-200 bg-white px-5 py-6 md:flex md:flex-col">
      <SidebarContent onLogout={onLogout} />
    </aside>
  );
}

function MobileSidebar({ onLogout, onNavigate }: SidebarProps) {
  return (
    <aside className="flex h-full flex-col px-5 py-6">
      <SidebarContent onLogout={onLogout} onNavigate={onNavigate} />
    </aside>
  );
}

function SidebarContent({ onLogout, onNavigate }: SidebarProps) {
  return (
    <>
      <div className="mb-8">
        <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gray-900 text-lg text-white">
          ₹
        </div>

        <h2 className="mt-4 text-xl font-bold text-gray-900">PayBackPal</h2>
        <p className="mt-1 text-sm text-gray-500">
          Credit card repayments
        </p>
      </div>

      <nav className="flex-1 space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            onClick={onNavigate}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition",
                isActive
                  ? "bg-gray-900 text-white"
                  : "text-gray-700 hover:bg-gray-100",
              )
            }
          >
            <span>{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-gray-200 pt-4">
        <Button
          type="button"
          variant="ghost"
          className="w-full justify-start"
          onClick={onLogout}
        >
          Logout
        </Button>
      </div>
    </>
  );
}