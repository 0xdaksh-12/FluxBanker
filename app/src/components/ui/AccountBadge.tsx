import type { Account } from "../../types";
import styles from "./AccountBadge.module.css";

interface AccountBadgeProps {
  type: Account["type"];
  subtype: Account["subtype"];
}

export const AccountBadge = ({ type, subtype }: AccountBadgeProps) => {
  return (
    <span className={styles.badge}>
      <span className={styles.type}>{type}</span>
      <span className={styles.dot}>•</span>
      <span className={styles.subtype}>{subtype}</span>
    </span>
  );
};
