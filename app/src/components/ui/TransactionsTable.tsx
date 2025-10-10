import { Transaction } from "../../types";
import { formatCurrency, formatDateTime } from "../../lib/utils";

interface TransactionsTableProps {
  transactions: Transaction[];
  limit?: number;
}

export const TransactionsTable = ({
  transactions = [],
  limit,
}: TransactionsTableProps) => {
  const safeTransactions = Array.isArray(transactions) ? transactions : [];
  const displayTx = limit ? safeTransactions.slice(0, limit) : safeTransactions;

  if (safeTransactions.length === 0) {
    return (
      <div
        style={{
          padding: "2rem",
          textAlign: "center",
          color: "var(--ink-muted)",
          fontFamily: "var(--mono)",
          fontSize: "13px",
          textTransform: "uppercase",
          letterSpacing: "0.08em",
        }}
      >
        No transactions found.
      </div>
    );
  }

  return (
    <div style={{ overflowX: "auto" }}>
      <table className="data-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Description</th>
            <th>Category</th>
            <th>Amount</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {displayTx.map((tx) => (
            <tr key={tx.id}>
              <td className="text-muted text-mono">
                {formatDateTime(tx.timestamp)}
              </td>
              <td style={{ fontWeight: 600 }}>
                {tx.type === "TRANSFER"
                  ? tx.amount < 0
                    ? `Transfer to ${tx.counterpartyName}`
                    : `Transfer from ${tx.counterpartyName}`
                  : tx.type}
              </td>
              <td className="text-muted">{tx.category || "—"}</td>
              <td
                className={`text-mono ${tx.amount > 0 ? "text-positive" : "text-negative"}`}
                style={{ fontWeight: 700, whiteSpace: "nowrap" }}
              >
                {tx.amount > 0 ? "+" : ""}
                {formatCurrency(tx.amount)}
              </td>
              <td>
                <span
                  className={`status-badge ${
                    tx.status === "COMPLETED"
                      ? "status-completed"
                      : tx.status === "FAILED"
                        ? "status-failed"
                        : "status-pending"
                  }`}
                >
                  <span
                    className="material-symbols-outlined"
                    style={{ fontSize: "14px" }}
                  >
                    {tx.status === "COMPLETED"
                      ? "check_circle"
                      : tx.status === "FAILED"
                        ? "error"
                        : "pending"}
                  </span>
                  {tx.status}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
