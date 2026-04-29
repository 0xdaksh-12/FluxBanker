import {
  useAccounts,
  useCreateAccount,
  useDeposit,
} from "../hooks/useAccounts";
import { Account } from "../types";
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
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState("");
  const [subtype, setSubtype] = useState<Account["subtype"]>("CHECKING");

  // Deposit Modal State
  const [isDepositModalOpen, setIsDepositModalOpen] = useState(false);
  const [depositAmount, setDepositAmount] = useState("1000");
  const [targetAccount, setTargetAccount] = useState<Pick<
    Account,
    "id" | "mask"
  > | null>(null);

  const handleCreate = (e: React.SyntheticEvent) => {
    e.preventDefault();
    createMutation.mutate(
      { name, subtype },
      {
        onSuccess: () => {
          queryClient.invalidateQueries({ queryKey: ["accounts"] });
          setShowForm(false);
          setName("");
          setSubtype("CHECKING");
        },
      },
    );
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

  return (
    <div>
      <div className="dashboard-header">
        <div className="dashboard-greeting">
          <h1>Accounts</h1>
          <p>Manage your depository and credit accounts.</p>
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
            <CustomSelect
              label="Account Type"
              value={subtype}
              onChange={(val) => setSubtype(val as Account["subtype"])}
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
                  value: "CREDIT_CARD",
                  label: "Credit Card",
                  description: "Flexible credit line for your purchases",
                },
                {
                  value: "MONEY_MARKET",
                  label: "Money Market",
                  description: "High-yield account with limited transfers",
                },
              ]}
            />
            <button
              type="submit"
              className="btn btn-primary"
              disabled={createMutation.isPending}
              style={{ alignSelf: "flex-end", marginBottom: "15px" }}
            >
              {createMutation.isPending ? "Opening..." : "Submit"}
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
              <button
                className="btn btn-outline"
                style={{ width: "100%", justifyContent: "center" }}
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
          <p
            style={{
              fontSize: "14px",
              color: "var(--ink-muted)",
            }}
          >
            Adding funds to account ending in{" "}
            <strong>{targetAccount?.mask}</strong>.
          </p>
          <div className="form-group">
            <label className="form-label">Amount (INR)</label>
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
