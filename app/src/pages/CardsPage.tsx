import { useState } from "react";
import {
  useCards,
  useIssueCard,
  useFreezeCard,
  useUnfreezeCard,
  useSetPin,
} from "../hooks/useCards";
import { useAccounts } from "../hooks/useAccounts";
import { useAuthStore } from "../store/authStore";
import { Card } from "../types";
import { Modal } from "../components/ui/Modal";
import { toast } from "sonner";
import { CustomSelect } from "../components/ui/CustomSelect";
import styles from "./CardsPage.module.css";

const InteractiveCard = ({ card }: { card: Card }) => {
  const [isFlipped, setIsFlipped] = useState(false);
  const { user } = useAuthStore();
  const freezeMutation = useFreezeCard();
  const unfreezeMutation = useUnfreezeCard();
  const setPinMutation = useSetPin();

  const [isPinModalOpen, setIsPinModalOpen] = useState(false);
  const [pin, setPin] = useState("");

  const handleToggleFreeze = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (card.status === "FROZEN") {
      unfreezeMutation.mutate(card.id, {
        onSuccess: () => toast.success("Card unfrozen successfully"),
        onError: () => toast.error("Failed to unfreeze card"),
      });
    } else {
      freezeMutation.mutate(card.id, {
        onSuccess: () => toast.success("Card frozen successfully"),
        onError: () => toast.error("Failed to freeze card"),
      });
    }
  };

  const handleSetPinSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (pin.length !== 4) {
      toast.error("PIN must be exactly 4 digits");
      return;
    }
    setPinMutation.mutate(
      { cardId: card.id, pin },
      {
        onSuccess: () => {
          toast.success("PIN set successfully");
          setIsPinModalOpen(false);
          setPin("");
        },
        onError: () => toast.error("Failed to set PIN"),
      }
    );
  };

  const formatCardNumber = (num: string) => {
    if (num.includes(" ")) return num;
    return num.match(/.{1,4}/g)?.join(" ") || num;
  };

  return (
    <div>
      <div
        className={`${styles.cardContainer} ${isFlipped ? styles.flipped : ""}`}
        onClick={() => setIsFlipped(!isFlipped)}
      >
        <div className={styles.cardInner}>
          {/* Front of Card */}
          <div
            className={`${styles.cardFront} ${
              card.status === "FROZEN" ? styles.frozen : ""
            } ${card.type === "VIRTUAL" ? styles.virtualCard : styles.physicalCard}`}
          >
            {card.status === "FROZEN" && (
              <div className={styles.frozenBadge}>FROZEN</div>
            )}
            <div className={styles.cardHeader}>
              <span className={styles.bankName}>FluxBanker</span>
              <div className={styles.cardTypeBrand}>
                {card.type === "VIRTUAL" ? "VIRTUAL" : "DEBIT"}
              </div>
            </div>
            <div className={styles.chipSection}>
              <div className={styles.chip}></div>
              <span className={`material-symbols-outlined ${styles.wifiIcon}`}>wifi</span>
            </div>
            <div className={styles.cardNumber}>
              {formatCardNumber(card.cardNumber)}
            </div>
            <div className={styles.cardDetails}>
              <div className={styles.cardHolder}>
                <span className={styles.label}>Cardholder</span>
                <span className={styles.value}>{user?.firstName} {user?.lastName}</span>
              </div>
              <div className={styles.cardExpiry}>
                <span className={styles.label}>Valid Thru</span>
                <span className={styles.value}>{card.expiryDate}</span>
              </div>
            </div>
          </div>

          {/* Back of Card */}
          <div className={styles.cardBack}>
            <div className={styles.magStripe}></div>
            <div className={styles.signature}>
              <span className={styles.cvv}>{card.cvv}</span>
            </div>
            <div style={{ marginTop: "140px", fontSize: "0.8rem", opacity: 0.8 }}>
              For customer service call 1-800-FLUX-BNK
            </div>
          </div>
        </div>
      </div>

      <div className={styles.actionGrid}>
        <button
          className="btn btn-outline"
          style={{ justifyContent: "center" }}
          onClick={handleToggleFreeze}
          disabled={freezeMutation.isPending || unfreezeMutation.isPending}
        >
          <span className="material-symbols-outlined">
            {card.status === "FROZEN" ? "lock_open" : "lock"}
          </span>
          {card.status === "FROZEN" ? "Unfreeze" : "Freeze"}
        </button>
        <button
          className="btn btn-outline"
          style={{ justifyContent: "center" }}
          onClick={() => setIsPinModalOpen(true)}
        >
          <span className="material-symbols-outlined">dialpad</span>
          Set PIN
        </button>
      </div>

      <Modal
        isOpen={isPinModalOpen}
        onClose={() => setIsPinModalOpen(false)}
        title="Set Card PIN"
      >
        <form
          onSubmit={handleSetPinSubmit}
          style={{ display: "flex", flexDirection: "column", gap: "24px" }}
        >
          <p style={{ fontSize: "14px", color: "var(--ink-muted)" }}>
            Enter a new 4-digit PIN for your card ending in {card.cardNumber.slice(-4)}.
          </p>
          <div className="form-group">
            <label className="form-label">New PIN</label>
            <input
              type="password"
              required
              maxLength={4}
              pattern="\d{4}"
              value={pin}
              onChange={(e) => setPin(e.target.value.replace(/\D/g, ""))}
              className="form-input"
              placeholder="****"
              style={{ letterSpacing: "8px", fontSize: "1.5rem", textAlign: "center" }}
              autoFocus
            />
          </div>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={setPinMutation.isPending}
            style={{ width: "100%", justifyContent: "center" }}
          >
            {setPinMutation.isPending ? "Saving..." : "Save PIN"}
          </button>
        </form>
      </Modal>
    </div>
  );
};

export const CardsPage = () => {
  const { data: cards, isLoading: isLoadingCards } = useCards();
  const { data: accounts } = useAccounts();
  const issueMutation = useIssueCard();

  const [isIssueModalOpen, setIsIssueModalOpen] = useState(false);
  const [selectedAccountId, setSelectedAccountId] = useState("");
  const [selectedType, setSelectedType] = useState<"PHYSICAL" | "VIRTUAL">("VIRTUAL");

  const handleIssueCard = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAccountId) {
      toast.error("Please select an account");
      return;
    }
    issueMutation.mutate(
      { accountId: selectedAccountId, type: selectedType },
      {
        onSuccess: () => {
          toast.success(`${selectedType === "VIRTUAL" ? "Virtual" : "Physical"} Card Issued!`);
          setIsIssueModalOpen(false);
          setSelectedAccountId("");
        },
        onError: () => toast.error("Failed to issue card"),
      }
    );
  };

  const eligibleAccounts = accounts?.filter(a => a.type === "DEPOSITORY" || a.type === "CREDIT") || [];

  return (
    <div>
      <div className="dashboard-header">
        <div className="dashboard-greeting">
          <h1>My Cards</h1>
          <p>Manage your physical and virtual cards seamlessly.</p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setIsIssueModalOpen(true)}
        >
          <span className="material-symbols-outlined">add_card</span>
          Issue Card
        </button>
      </div>

      {isLoadingCards ? (
        <div className={styles.grid}>
          <div className="skeleton" style={{ height: "200px", borderRadius: "16px" }}></div>
          <div className="skeleton" style={{ height: "200px", borderRadius: "16px" }}></div>
        </div>
      ) : cards?.length === 0 ? (
        <div className="bento-box" style={{ textAlign: "center", padding: "48px" }}>
          <span className="material-symbols-outlined" style={{ fontSize: "48px", color: "var(--ink-muted)" }}>
            credit_card_off
          </span>
          <h3 style={{ marginTop: "16px" }}>No Cards Found</h3>
          <p style={{ color: "var(--ink-muted)", marginTop: "8px" }}>
            You haven't issued any physical or virtual cards yet.
          </p>
          <button
            className="btn btn-primary"
            style={{ marginTop: "24px", margin: "24px auto 0" }}
            onClick={() => setIsIssueModalOpen(true)}
          >
            Issue Your First Card
          </button>
        </div>
      ) : (
        <div className={styles.grid}>
          {cards?.map((card) => (
            <InteractiveCard key={card.id} card={card} />
          ))}
        </div>
      )}

      <Modal
        isOpen={isIssueModalOpen}
        onClose={() => setIsIssueModalOpen(false)}
        title="Issue New Card"
      >
        <form onSubmit={handleIssueCard} style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
          <CustomSelect
            label="Linked Account"
            value={selectedAccountId}
            onChange={setSelectedAccountId}
            className="form-group"
            options={[
              { value: "", label: "-- Select an Account --" },
              ...eligibleAccounts.map(a => ({
                value: a.id,
                label: `${a.name} (...${a.mask})`,
                description: `Balance: ₹${a.availableBalance.toFixed(2)}`
              }))
            ]}
          />

          <CustomSelect
            label="Card Type"
            value={selectedType}
            onChange={(v) => setSelectedType(v as "PHYSICAL" | "VIRTUAL")}
            className="form-group"
            options={[
              { value: "VIRTUAL", label: "Virtual Card", description: "Available instantly for online purchases" },
              { value: "PHYSICAL", label: "Physical Card", description: "Shipped to your address in 5-7 days" },
            ]}
          />

          <button
            type="submit"
            className="btn btn-primary"
            disabled={issueMutation.isPending || !selectedAccountId}
            style={{ width: "100%", justifyContent: "center", marginTop: "8px" }}
          >
            {issueMutation.isPending ? "Issuing..." : "Confirm Issue"}
          </button>
        </form>
      </Modal>
    </div>
  );
};
