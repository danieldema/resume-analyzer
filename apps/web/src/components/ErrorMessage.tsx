export function ErrorMessage({ message }: { message: string | null }) {
  if (!message) return null;
  return (
    <div className="bg-red-50 border border-red-200 text-red-700 rounded px-4 py-3 text-sm mt-2">
      {message}
    </div>
  );
}
