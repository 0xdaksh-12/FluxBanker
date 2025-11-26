import CountUp from "react-countup";
import styles from "./BalanceWidget.module.css";

interface BalanceWidgetProps {
  totalBalance: number;
  totalAccounts: number;
}

export const BalanceWidget = ({
  totalBalance,
  totalAccounts,
}: BalanceWidgetProps) => {
  const CountUpComponent =
    (CountUp as unknown as { default: typeof CountUp }).default || CountUp;

  return (
    <div className={`bento-box ${styles.widget}`}>
      <p className={`text-label ${styles.label}`}>Total Balance</p>
      <div className={styles.amount}>
        <CountUpComponent
          end={totalBalance}
          prefix="₹"
          separator=","
          decimals={2}
          duration={1.5}
        >
          {({ countUpRef }) => <span ref={countUpRef} />}
        </CountUpComponent>
      </div>
      <div className={styles.sub}>
        <span
          className="material-symbols-outlined"
          style={{ fontSize: "16px" }}
        >
          account_balance
        </span>
        <span>
          Across {totalAccounts} active{" "}
          {totalAccounts === 1 ? "account" : "accounts"}
        </span>
      </div>
    </div>
  );
};
