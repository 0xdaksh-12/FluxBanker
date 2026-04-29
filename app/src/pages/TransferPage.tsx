import { TransferForm } from "../components/forms/TransferForm";
import styles from "./TransferPage.module.css";

export const TransferPage = () => {
  return (
    <div>
      <div className="dashboard-header">
        <div className="dashboard-greeting">
          <h1>Transfer Money</h1>
          <p>Move funds instantly between your accounts.</p>
        </div>
      </div>

      <div className={styles.layout}>
        <div className={`bento-box ${styles.formPanel}`}>
          <TransferForm />
        </div>

        <div className={`bento-box ${styles.infoPanel}`}>
          <span className="material-symbols-outlined" style={{ fontSize: "40px", marginBottom: "20px", display: "block" }}>
            bolt
          </span>
          <h2 className={styles.infoPanelTitle}>Instant Internal Transfers</h2>
          <p className={styles.infoPanelText}>
            Transfers between your FluxBanker accounts are processed instantly,
            24/7. Funds are available for immediate use without any hold times.
          </p>
          <p className={styles.infoPanelText} style={{ marginTop: "16px" }}>
            A permanent, immutable record is generated and stored in our
            Kafka-backed audit ledger.
          </p>
          <div className={styles.infoPanelDivider} />
          <p className={styles.infoPanelMeta}>
            // SYSTEM: LEDGER_V2 // REALTIME_SETTLEMENT
          </p>
        </div>
      </div>
    </div>
  );
};
