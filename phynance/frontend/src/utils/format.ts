export function formatCurrency(
  value: number,
  currency: string = "USD"
): string {
  return value.toLocaleString("en-US", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
  });
}
