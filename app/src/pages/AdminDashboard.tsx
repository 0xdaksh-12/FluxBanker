import { useState } from "react";
import { useAllUsers, useAllTransactions } from "../hooks/useAdmin";
import { formatCurrency, formatDate } from "../lib/utils";
import { PaginatedResponse, User, Transaction } from "../types";
import styles from "./AdminDashboard.module.css";

const AdminDashboard = () => {
  const [txPage, setTxPage] = useState(0);
  const [txSize, setTxSize] = useState(10);
  const [userPage, setUserPage] = useState(0);
  const [userSize, setUserSize] = useState(10);

  const { data: usersData, isLoading: usersLoading } = useAllUsers(
    userPage,
    userSize,
  );
  const { data: transactionsData, isLoading: txLoading } = useAllTransactions(
    txPage,
    txSize,
  );

  if (usersLoading || txLoading) {
    return (
      <div className={styles.loading}>
        <div className={styles.spinner} />
      </div>
    );
  }

  const userPaginated = usersData as PaginatedResponse<User>;
  const users = Array.isArray(usersData)
    ? usersData
    : userPaginated?.content || [];

  const txPaginated = transactionsData as PaginatedResponse<Transaction>;
  const transactions = Array.isArray(transactionsData)
    ? transactionsData
    : txPaginated?.content || [];

  // Transaction Pagination Helpers
  const txTotalPages = txPaginated?.totalPages || 1;
  const isTxFirstPage = txPage === 0;
  const isTxLastPage = txPage >= txTotalPages - 1;

  // User Pagination Helpers
  const userTotalPages = userPaginated?.totalPages || 1;
  const isUserFirstPage = userPage === 0;
  const isUserLastPage = userPage >= userTotalPages - 1;

  const handleNextTx = () => {
    if (!isTxLastPage) setTxPage((p) => p + 1);
  };
  const handlePrevTx = () => {
    if (!isTxFirstPage) setTxPage((p) => p - 1);
  };

  const handleNextUser = () => {
    if (!isUserLastPage) setUserPage((p) => p + 1);
  };
  const handlePrevUser = () => {
    if (!isUserFirstPage) setUserPage((p) => p - 1);
  };

  return (
    <div className={styles.container}>
      <div className="dashboard-header">
        <div className="dashboard-greeting">
          <h1>Admin Control Panel</h1>
        </div>
      </div>

      <div className={styles.grid}>
        {/* Users Section */}
        <div className="bento-box">
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>
              System Users ({userPaginated?.totalElements || users?.length || 0}
              )
            </h2>
            <div className={styles.controls}>
              <div className={styles.sizeControl}>
                <input
                  type="number"
                  className={styles.sizeInput}
                  value={userSize || ""}
                  onChange={(e) => {
                    const val = e.target.value;
                    if (val === "") {
                      setUserSize(0);
                    } else {
                      setUserSize(Math.max(1, Number(val)));
                    }
                    setUserPage(0);
                  }}
                  min="1"
                />
              </div>
              <div className={styles.pagination}>
                <button
                  onClick={handlePrevUser}
                  disabled={isUserFirstPage}
                  className={styles.pageButton}
                >
                  PREV
                </button>
                <span className={styles.pageIndicator}>
                  {userPage + 1} / {userTotalPages}
                </span>
                <button
                  onClick={handleNextUser}
                  disabled={isUserLastPage}
                  className={styles.pageButton}
                >
                  NEXT
                </button>
              </div>
            </div>
          </div>
          <div style={{ overflowX: "auto" }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                </tr>
              </thead>
              <tbody>
                {users?.map((user) => (
                  <tr key={user.id}>
                    <td style={{ fontWeight: 600 }}>
                      {user.firstName} {user.lastName}
                    </td>
                    <td className="text-muted">{user.email}</td>
                    <td>
                      <span className={styles.roleBadge} data-role={user.role}>
                        {user.role}
                      </span>
                    </td>
                  </tr>
                ))}
                {!users?.length && (
                  <tr>
                    <td colSpan={3} style={{ textAlign: "center" }}>
                      No users found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Transactions Section */}
        <div className="bento-box">
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>
              Global Ledger (
              {txPaginated?.totalElements || transactions?.length || 0})
            </h2>
            <div className={styles.controls}>
              <div className={styles.sizeControl}>
                <input
                  type="number"
                  className={styles.sizeInput}
                  value={txSize || ""}
                  onChange={(e) => {
                    const val = e.target.value;
                    if (val === "") {
                      setTxSize(0);
                    } else {
                      setTxSize(Math.max(1, Number(val)));
                    }
                    setTxPage(0);
                  }}
                  min="1"
                />
              </div>
              <div className={styles.pagination}>
                <button
                  onClick={handlePrevTx}
                  disabled={isTxFirstPage}
                  className={styles.pageButton}
                >
                  PREV
                </button>
                <span className={styles.pageIndicator}>
                  {txPage + 1} / {txTotalPages}
                </span>
                <button
                  onClick={handleNextTx}
                  disabled={isTxLastPage}
                  className={styles.pageButton}
                >
                  NEXT
                </button>
              </div>
            </div>
          </div>
          <div style={{ overflowX: "auto" }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {transactions?.map((tx) => (
                  <tr key={tx.id}>
                    <td className="text-muted text-mono">
                      {formatDate(tx.timestamp)}
                    </td>
                    <td>{tx.type}</td>
                    <td className="text-mono" style={{ fontWeight: 600 }}>
                      {formatCurrency(tx.amount)}
                    </td>
                    <td>
                      <span
                        className={`status-badge ${
                          tx.status === "COMPLETED"
                            ? "status-completed"
                            : "status-pending"
                        }`}
                      >
                        {tx.status}
                      </span>
                    </td>
                  </tr>
                ))}
                {!transactions?.length && (
                  <tr>
                    <td colSpan={4} style={{ textAlign: "center" }}>
                      No transactions found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
