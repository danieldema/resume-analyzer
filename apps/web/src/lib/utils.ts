import axios from 'axios';

export function extractErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const status = err.response?.status;
    if (status === 429) return "You've reached the request limit. Please wait a few minutes before trying again.";
    if (status === 422) return err.response?.data?.error ?? "Couldn't read this PDF — it may be password-protected or corrupted.";
    return err.response?.data?.error ?? err.response?.data?.message ?? 'Something went wrong. Please try again.';
  }
  return 'Something went wrong. Please try again.';
}
