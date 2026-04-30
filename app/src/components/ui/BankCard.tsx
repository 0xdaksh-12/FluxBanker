import { Account } from "../../types";
import { formatCurrency } from "../../lib/utils";
import { AccountBadge } from "./AccountBadge";
import styles from "./BankCard.module.css";

interface BankCardProps {
  account: Account;
}

export const BankCard = ({ account }: BankCardProps) => {
  const isLoan = account.type === "LOAN";
  const isCredit = account.subtype === "CREDIT_CARD";

  const getBalanceLabel = () => {
    if (isLoan) return "Outstanding Principal";
    if (isCredit) return "Current Balance";
    return "Available Balance";
  };

  const getSecondaryInfo = () => {
    if (isLoan && account.loanDetails) {
      return `Monthly Payment: ${formatCurrency(account.loanDetails.monthlyPayment)}`;
    }
    if (isCredit && account.creditDetails) {
      return `Available Credit: ${formatCurrency(account.availableBalance)}`;
    }
    return `**** **** **** ${account.mask}`;
  };

  return (
    <div className={`bento-box ${styles.card}`}>
      <div className={styles.header}>
        <div>
          <h3 className={styles.name}>{account.name}</h3>
          <AccountBadge type={account.type} subtype={account.subtype} />
        </div>
        <span className="material-symbols-outlined">
          {isLoan ? "account_balance" : "contactless"}
        </span>
      </div>

      <div style={{ marginTop: "1rem", marginBottom: "1rem" }}>
        <p style={{ fontSize: "0.85rem", color: "var(--ink-muted)", marginBottom: "4px" }}>
          {getBalanceLabel()}
        </p>
        <div className={styles.balance}>
          {formatCurrency(account.currentBalance)}
        </div>
      </div>

      <div className={styles.footer}>
        <span>{getSecondaryInfo()}</span>
        {!isLoan && <span>VALID THRU 12/28</span>}
      </div>
    </div>
  );
};
