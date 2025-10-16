import { useAccounts } from "../hooks/useAccounts";
import { useTransactions } from "../hooks/useTransactions";
import { TransactionsTable } from "../components/ui/TransactionsTable";
import { Skeleton } from "../components/ui/Skeleton";
import { useState } from "react";
import styles from "./TransactionsPage.module.css";

/**
 * Sub-component for the transaction list to handle its own pagination state.
 * Using a key on this component in the parent will automatically reset its state
 * when the account changes.
 */
const TransactionSection = ({ accountId }: { accountId: string }) => {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const { data: txData, isLoading: txLoading } = useTransactions(
    accountId,
    page,
    size,
  );

  const transactions = txData?.content || [];
  const totalPages = txData?.totalPages || 1;
  const totalElements = txData?.totalElements || 0;

  const isFirstPage = page === 0;
  const isLastPage = page >= totalPages - 1;

  const handleNextPage = () => {
    if (!isLastPage) setPage((p) => p + 1);
  };

  const handlePrevPage = () => {
    if (!isFirstPage) setPage((p) => p - 1);
  };

  return (
    <>
      <div className={styles.mainHeader}>
        <h2 className={styles.mainTitle}>Transactions ({totalElements})</h2>
        <div className={styles.controls}>
          <div className={styles.sizeControl}>
            <input
              type="number"
              className={styles.sizeInput}
              value={size || ""}
              onChange={(e) => {
                const val = e.target.value;
                if (val === "") {
                  setSize(0);
                } else {
                  setSize(Math.max(1, Number(val)));
                }
                setPage(0);
              }}
              min="1"
            />
          </div>
          <div className={styles.pagination}>
            <button
              onClick={handlePrevPage}
              disabled={isFirstPage}
              className={styles.pageButton}
            >
              PREV
            </button>
            <span className={styles.pageIndicator}>
              {page + 1} / {totalPages}
            </span>
            <button
              onClick={handleNextPage}
              disabled={isLastPage}
              className={styles.pageButton}
            >
              NEXT
            </button>
          </div>
        </div>
      </div>
      {txLoading ? (
        <div style={{ padding: "24px" }}>
          <Skeleton height="400px" />
        </div>
      ) : (
        <TransactionsTable transactions={transactions} />
      )}
    </>
  );
};

export const TransactionsPage = () => {
  const { data: accounts, isLoading: accountsLoading } = useAccounts();
  const [selectedAccountId, setSelectedAccountId] = useState<string | null>(
    null,
  );

  const activeAccountId =
    selectedAccountId ||
    (accounts && accounts.length > 0 ? accounts[0].id : null);

  return (
    <div>
      <div className="dashboard-header">
        <div className="dashboard-greeting">
          <h1>CENTRAL LEDGER</h1>
          <p>View all your recent activity.</p>
        </div>
      </div>

      <div className={styles.layout}>
        {/* Sidebar: Account selector */}
        <div className={`bento-box ${styles.sidebar}`}>
          <h2 className={styles.sidebarTitle}>Select Account</h2>
          {accountsLoading ? (
            <Skeleton height="150px" />
          ) : (
            <div className={styles.accountList}>
              {accounts?.map((account) => (
                <button
                  key={account.id}
                  onClick={() => setSelectedAccountId(account.id)}
                  className={`${styles.accountBtn} ${activeAccountId === account.id ? styles.accountBtnActive : ""}`}
                >
                  <span className={styles.accountBtnName}>
                    {account.name} (••{account.mask})
                  </span>
                  <span className="text-mono" style={{ fontSize: "13px" }}>
                    ${account.currentBalance.toFixed(2)}
                  </span>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Main: Transactions list */}
        <div className={`bento-box ${styles.main}`}>
          {activeAccountId ? (
            <TransactionSection
              key={activeAccountId}
              accountId={activeAccountId}
            />
          ) : !accountsLoading ? (
            <div style={{ padding: "24px", textAlign: "center" }}>
              <p className="text-muted">No accounts found.</p>
            </div>
          ) : (
            <div style={{ padding: "24px" }}>
              <Skeleton height="400px" />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
