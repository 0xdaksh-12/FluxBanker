import { useMemo } from "react";
import { useAuthStore } from "../store/authStore";
import { useAccounts } from "../hooks/useAccounts";
import { useUserTransactions } from "../hooks/useTransactions";
import { BalanceWidget } from "../components/ui/BalanceWidget";
import { DoughnutChart } from "../components/ui/DoughnutChart";
import { BankCard } from "../components/ui/BankCard";
import { TransactionsTable } from "../components/ui/TransactionsTable";
import { BankCardSkeleton, Skeleton } from "../components/ui/Skeleton";
import { Link } from "react-router-dom";
import type { PaginatedResponse, Transaction } from "../types";
import styles from "./DashboardPage.module.css";

export const DashboardPage = () => {
  const user = useAuthStore((state) => state.user);
  const { data: accounts, isLoading: accountsLoading } = useAccounts();

  const { data: transactions, isLoading: txLoading } = useUserTransactions(
    0,
    5,
  );

  const totalBalance = useMemo(
    () => accounts?.reduce((sum, acc) => sum + acc.currentBalance, 0) || 0,
    [accounts],
  );

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.headerTitle}>
          <h1 className={styles.title}>Wassup, {user?.firstName}</h1>
          <p className={styles.status}>
            System status: Operational // Balance: {accounts?.length || 0}{" "}
            Accounts Active
          </p>
        </div>
        <Link to="/transfer" className={styles.btnSharp}>
          <span className={`material-symbols-outlined ${styles.btnIcon}`}>
            swap_horiz
          </span>
          Move Assets
        </Link>
      </div>

      <div className={styles.grid}>
        {/* Left Side: Chart & Balance */}
        <div className={styles.leftColumn}>
          <div className={`bento-box ${styles.chartContainer}`}>
            {accountsLoading ? (
              <div className={styles.skeletonChart} />
            ) : (
              <DoughnutChart accounts={accounts || []} />
            )}
          </div>

          {accountsLoading ? (
            <Skeleton height="300px" className="bento-box" />
          ) : (
            <BalanceWidget
              totalBalance={totalBalance}
              totalAccounts={accounts?.length || 0}
            />
          )}
        </div>

        {/* Right Side: Accounts summary */}
        <div className={styles.rightColumn}>
          <div className={styles.section}>
            <div className={styles.sectionHeader}>
              <h2 className={styles.sectionTitle}>Active Accounts</h2>
              <Link to="/accounts" className={styles.sectionLink}>
                View Repository
              </Link>
            </div>
            <div className={styles.cardsGrid}>
              {accountsLoading ? (
                <>
                  <BankCardSkeleton />
                  <BankCardSkeleton />
                </>
              ) : (
                accounts
                  ?.slice(0, 2)
                  .map((account) => (
                    <BankCard key={account.id} account={account} />
                  ))
              )}
            </div>
          </div>

          {/* Transactions */}
          <div className={styles.section}>
            <div className={styles.sectionHeader}>
              <h2 className={styles.sectionTitle}>LEDGER — RECENT ACTIVITY</h2>
              <Link to="/transactions" className={styles.sectionLink}>
                COMPLETE LEDGER
              </Link>
            </div>
            <div className={`bento-box ${styles.tableContainer}`}>
              {txLoading ? (
                <div className={styles.loadingContainer}>
                  <Skeleton height="200px" />
                </div>
              ) : (
                <TransactionsTable
                  transactions={
                    (transactions as PaginatedResponse<Transaction>)?.content ||
                    []
                  }
                  limit={5}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
