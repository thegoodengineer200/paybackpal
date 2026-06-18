type LoadingStateProps = {
  title?: string;
  description?: string;
};

export function LoadingState({
  title = "Loading...",
  description = "Please wait while we fetch the latest data.",
}: LoadingStateProps) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white px-6 py-10 text-center shadow-sm">
      <div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-gray-300 border-t-gray-900" />

      <h2 className="mt-4 text-sm font-semibold text-gray-900">{title}</h2>
      <p className="mt-1 text-sm text-gray-500">{description}</p>
    </div>
  );
}