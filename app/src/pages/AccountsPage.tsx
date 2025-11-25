import {
  useAccounts,
  useCreateAccount,
  useDeposit,
  useApplyForLoan,
  useOpenCreditCard,
} from "../hooks/useAccounts";
import type { Account } from "../types";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";
import { Modal } from "../components/ui/Modal";
import { BankCard } from "../components/ui/BankCard";
import { BankCardSkeleton } from "../components/ui/Skeleton";
import { CustomSelect } from "../components/ui/CustomSelect";
import { useState } from "react";
import styles from "./AccountsPage.module.css";

export const AccountsPage = () => {
  const { data: accounts, isLoading } = useAccounts();
  const createMutation = useCreateAccount();
  const depositMutation = useDeposit();
  const applyLoanMutation = useApplyForLoan();
  const openCreditMutation = useOpenCreditCard();

  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState("");
  const [productType, setProductType] = useState<string>("CHECKING");

  const [principal, setPrincipal] = useState("10000");
  const [termMonths, setTermMonths] = useState("60");
  const [creditLimit, setCreditLimit] = useState("5000");

  // Deposit Modal State
  const [isDepositModalOpen, setIsDepositModalOpen] = useState(false);
  const [depositAmount, setDepositAmount] = useState("1000");
  const [targetAccount, setTargetAccount] = useState<Pick<
    Account,
    "id" | "mask"
  > | null>(null);

  const handleCreate = (e: React.SyntheticEvent) => {
    e.preventDefault();

    const onSuccessHandler = () => {
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
      setShowForm(false);
      setName("");
      setProductType("CHECKING");
      setPrincipal("10000");
      setTermMonths("60");
      setCreditLimit("5000");
    };

    if (productType === "LOAN") {
      applyLoanMutation.mutate(
        {
          principal: parseFloat(principal),
          termMonths: parseInt(termMonths, 10),
        },
        { onSuccess: onSuccessHandler },
      );
    } else if (productType === "CREDIT_CARD") {
      openCreditMutation.mutate(
        { creditLimit: parseFloat(creditLimit) },
        { onSuccess: onSuccessHandler },
      );
    } else {
      createMutation.mutate(
        { name, subtype: productType as Account["subtype"] },
        { onSuccess: onSuccessHandler },
      );
    }
  };

  const handleDeposit = (accountId: string, mask: string) => {
    setTargetAccount({ id: accountId, mask });
    setIsDepositModalOpen(true);
  };

  const handleSubmitDeposit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetAccount) return;

    const amount = parseFloat(depositAmount);
    if (isNaN(amount) || amount <= 0) {
      toast.error("Invalid deposit amount. Must be greater than 0.");
      return;
    }

    const promise = depositMutation.mutateAsync(
      { accountId: targetAccount.id, amount },
      {
        onSuccess: () => {
          queryClient.invalidateQueries({ queryKey: ["accounts"] });
          queryClient.invalidateQueries({ queryKey: ["transactions"] });
          setIsDepositModalOpen(false);
          setDepositAmount("1000");
        },
      },
    );
    toast.promise(promise, {
      loading: "Processing deposit...",
      success: `Successfully deposited ₹${amount.toFixed(2)}`,
      error: "Failed to process deposit",
    });
  };

  const isPending =
    createMutation.isPending ||
    applyLoanMutation.isPending ||
    openCreditMutation.isPending;

  return (
    <div>
      <div className="dashboard-header">
        <div className="dashboard-greeting">
          <h1>Accounts</h1>
          <p>Manage your depository, credit, and loan accounts.</p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setShowForm(!showForm)}
        >
          <span className="material-symbols-outlined">
            {showForm ? "close" : "add"}
          </span>
          {showForm ? "Cancel" : "Open Account"}
        </button>
      </div>

      {showForm && (
        <div className={`bento-box ${styles.formBox}`}>
          <h2 className={styles.formTitle}>Open a New Account</h2>
          <form onSubmit={handleCreate} className={styles.form}>
            {productType !== "LOAN" && productType !== "CREDIT_CARD" && (
              <div className="form-group">
                <label className="form-label" htmlFor="acct-name">
                  Account Name
                </label>
                <input
                  id="acct-name"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="form-input"
                  placeholder="e.g. Travel Savings"
                  style={{ paddingBlock: "13px" }}
                />
              </div>
            )}

            <CustomSelect
              label="Account Product"
              value={productType}
              onChange={(val) => setProductType(val as string)}
              className="form-group"
              options={[
                {
                  value: "CHECKING",
                  label: "Checking",
                  description: "Standard account for daily transactions",
                },
                {
                  value: "SAVINGS",
                  label: "Savings",
                  description: "Earn interest on your deposited funds",
                },
                {
                  value: "MONEY_MARKET",
                  label: "Money Market",
                  description: "High-yield account with limited transfers",
                },
                {
                  value: "CREDIT_CARD",
                  label: "Credit Card",
                  description: "Flexible credit line for your purchases",
                },
                {
                  value: "LOAN",
                  label: "Personal Loan",
                  description: "Fixed-rate personal loan",
                },
              ]}
            />

            {productType === "LOAN" && (
              <>
                <div className="form-group">
                  <label className="form-label">Principal Amount (₹)</label>
                  <input
                    type="number"
                    required
                    min="1000"
                    value={principal}
                    onChange={(e) => setPrincipal(e.target.value)}
                    className="form-input"
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Term (Months)</label>
                  <input
                    type="number"
                    required
                    min="12"
                    max="120"
                    value={termMonths}
                    onChange={(e) => setTermMonths(e.target.value)}
                    className="form-input"
                  />
                </div>
              </>
            )}

            {productType === "CREDIT_CARD" && (
              <div className="form-group">
                <label className="form-label">Requested Credit Limit (₹)</label>
                <input
                  type="number"
                  required
                  min="500"
                  value={creditLimit}
                  onChange={(e) => setCreditLimit(e.target.value)}
                  className="form-input"
                />
              </div>
            )}

            <button
              type="submit"
              className="btn btn-primary"
              disabled={isPending}
              style={{ alignSelf: "flex-end", marginBottom: "15px" }}
            >
              {isPending ? "Opening..." : "Submit Application"}
            </button>
          </form>
        </div>
      )}

      <div className={styles.grid}>
        {isLoading ? (
          <>
            <BankCardSkeleton />
            <BankCardSkeleton />
            <BankCardSkeleton />
            <BankCardSkeleton />
          </>
        ) : (
          accounts?.map((account) => (
            <div key={account.id} className={styles.cardWrapper}>
              <BankCard account={account} />
              {account.type !== "LOAN" && account.subtype !== "CREDIT_CARD" && (
                <button
                  className="btn btn-outline"
                  style={{
                    width: "100%",
                    justifyContent: "center",
                    marginTop: "12px",
                  }}
                  onClick={() => handleDeposit(account.id, account.mask)}
                  disabled={depositMutation.isPending}
                >
                  <span
                    className="material-symbols-outlined"
                    style={{ fontSize: "1.1rem" }}
                  >
                    payments
                  </span>
                  Simulate Deposit
                </button>
              )}
            </div>
          ))
        )}
      </div>

      <Modal
        isOpen={isDepositModalOpen}
        onClose={() => setIsDepositModalOpen(false)}
        title="Simulate Deposit"
      >
        <form
          onSubmit={handleSubmitDeposit}
          style={{ display: "flex", flexDirection: "column", gap: "24px" }}
        >
          <p style={{ fontSize: "14px", color: "var(--ink-muted)" }}>
            Adding funds to account ending in{" "}
            <strong>{targetAccount?.mask}</strong>.
          </p>
          <div className="form-group">
            <label className="form-label">Amount (₹)</label>
            <input
              type="number"
              required
              min="0.01"
              step="0.01"
              value={depositAmount}
              onChange={(e) => setDepositAmount(e.target.value)}
              className="form-input"
              autoFocus
            />
          </div>
          <div style={{ display: "flex", gap: "16px", marginTop: "12px" }}>
            <button
              type="button"
              className="btn btn-outline"
              onClick={() => setIsDepositModalOpen(false)}
              style={{ flex: 1, justifyContent: "center" }}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              style={{ flex: 1, justifyContent: "center" }}
              disabled={depositMutation.isPending}
            >
              {depositMutation.isPending ? "Processing..." : "Confirm Deposit"}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
