import { Account } from "../../types";
import { formatCurrency } from "../../lib/utils";
import { AccountBadge } from "./AccountBadge";
import styles from "./BankCard.module.css";

interface BankCardProps {
  account: Account;
}

export const BankCard = ({ account }: BankCardProps) => {
  return (
    <div className={`bento-box ${styles.card}`}>
      <div className={styles.header}>
        <div>
          <h3 className={styles.name}>{account.name}</h3>
          <AccountBadge type={account.type} subtype={account.subtype} />
        </div>
        <span className="material-symbols-outlined">contactless</span>
      </div>

      <div className={styles.balance}>
        {formatCurrency(account.currentBalance)}
      </div>

      <div className={styles.footer}>
        <span>**** **** **** {account.mask}</span>
        <span>VALID THRU 12/28</span>
      </div>
    </div>
  );
};
