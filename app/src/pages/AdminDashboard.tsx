import { useState } from "react";
import {
  useAllUsers,
  useAllTransactions,
  useUpdateKycStatus,
} from "../hooks/useAdmin";
import { formatCurrency, formatDate } from "../lib/utils";
import type { PaginatedResponse, User, Transaction } from "../types";
import { toast } from "sonner";
import { Modal } from "../components/ui/Modal";
import styles from "./AdminDashboard.module.css";

const AdminDashboard = () => {
  const [txPage, setTxPage] = useState(0);
  const [txSize, setTxSize] = useState(10);
  const [userPage, setUserPage] = useState(0);
  const [userSize, setUserSize] = useState(10);
  const [selectedKycUser, setSelectedKycUser] = useState<User | null>(null);

  const { data: usersData, isLoading: usersLoading } = useAllUsers(
    userPage,
    userSize,
  );
  const { data: transactionsData, isLoading: txLoading } = useAllTransactions(
    txPage,
    txSize,
  );
  const updateKycMutation = useUpdateKycStatus();

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

  const handleKycChange = (
    userId: string,
    newStatus: "APPROVED" | "REJECTED" | "PENDING",
  ) => {
    updateKycMutation.mutate(
      { userId, status: newStatus },
      {
        onSuccess: () => toast.success(`KYC status updated to ${newStatus}`),
        onError: () => toast.error("Failed to update KYC status"),
      },
    );
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
          <div className={styles.tableWrapper}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>KYC Status</th>
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
                    <td>
                      {user.role === "ADMIN" ||
                      user.kycStatus === "APPROVED" ? (
                        <span className="status-badge status-completed">
                          {user.role === "ADMIN" ? "AUTO-APPROVED" : "APPROVED"}
                        </span>
                      ) : (
                        <button
                          className={styles.statusTrigger}
                          onClick={() => setSelectedKycUser(user)}
                        >
                          <span
                            className={`status-badge ${
                              user.kycStatus === "REJECTED"
                                ? "status-failed"
                                : "status-pending"
                            }`}
                          >
                            {user.kycStatus || "PENDING"}
                            <span
                              className="material-symbols-outlined"
                              style={{ fontSize: "14px" }}
                            >
                              edit
                            </span>
                          </span>
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
                {!users?.length && (
                  <tr>
                    <td colSpan={4} style={{ textAlign: "center" }}>
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

      <Modal
        isOpen={!!selectedKycUser}
        onClose={() => setSelectedKycUser(null)}
        title="Update KYC Status"
      >
        {selectedKycUser && (
          <div className={styles.modalContent}>
            <div className={styles.userInfo}>
              <p className="form-label">Update Verification For</p>
              <h3 style={{ margin: "4px 0" }}>
                {selectedKycUser.firstName} {selectedKycUser.lastName}
              </h3>
              <p className="text-muted" style={{ fontSize: "12px" }}>
                {selectedKycUser.email}
              </p>
            </div>

            <div className={styles.statusOptions}>
              <button
                className={`${styles.optionBtn} ${styles.approved}`}
                onClick={() => {
                  handleKycChange(selectedKycUser.id, "APPROVED");
                  setSelectedKycUser(null);
                }}
                disabled={updateKycMutation.isPending}
              >
                <span className="material-symbols-outlined">check_circle</span>
                APPROVE USER
              </button>
              <button
                className={`${styles.optionBtn} ${styles.rejected}`}
                onClick={() => {
                  handleKycChange(selectedKycUser.id, "REJECTED");
                  setSelectedKycUser(null);
                }}
                disabled={updateKycMutation.isPending}
              >
                <span className="material-symbols-outlined">cancel</span>
                REJECT USER
              </button>
              <button
                className={`${styles.optionBtn} ${styles.pending}`}
                onClick={() => {
                  handleKycChange(selectedKycUser.id, "PENDING");
                  setSelectedKycUser(null);
                }}
                disabled={updateKycMutation.isPending}
              >
                <span className="material-symbols-outlined">schedule</span>
                RESET TO PENDING
              </button>
            </div>

            <button
              className="btn btn-outline"
              style={{ width: "100%", marginTop: "24px" }}
              onClick={() => setSelectedKycUser(null)}
            >
              CANCEL
            </button>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default AdminDashboard;
