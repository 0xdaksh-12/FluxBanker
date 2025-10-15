import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAccounts } from "../../hooks/useAccounts";
import { useTransfer } from "../../hooks/useTransactions";
import { formatCurrency } from "../../lib/utils";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { CustomSelect } from "../ui/CustomSelect";

const transferSchema = z
  .object({
    sourceAccountId: z.string().min(1, "Please select a source account"),
    destinationAccountId: z
      .string()
      .min(1, "Please select a destination account"),
    amount: z.number().positive("Amount must be positive"),
  })
  .refine((data) => data.sourceAccountId !== data.destinationAccountId, {
    message: "Source and destination accounts must be different",
    path: ["destinationAccountId"],
  });

type TransferFormValues = z.infer<typeof transferSchema>;

export const TransferForm = () => {
  const { data: accounts, isLoading } = useAccounts();
  const transferMutation = useTransfer();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    control,
    setValue,
    formState: { errors },
  } = useForm<TransferFormValues>({
    resolver: zodResolver(transferSchema),
    defaultValues: {
      sourceAccountId: "",
      destinationAccountId: "",
    },
  });

  const sourceAccountId = useWatch({ control, name: "sourceAccountId" });
  const destinationAccountId = useWatch({
    control,
    name: "destinationAccountId",
  });
  const sourceAccount = accounts?.find((a) => a.id === sourceAccountId);

  const onSubmit = (data: TransferFormValues) => {
    transferMutation.mutate(data, {
      onSuccess: () => {
        toast.success("Transfer completed successfully!");
        navigate("/transactions");
      },
      onError: () => {
        toast.error(
          "Transfer failed. Please check your balance and try again.",
        );
      },
    });
  };

  if (isLoading) return <div>Loading accounts...</div>;
  if (!accounts || accounts.length < 2)
    return (
      <div className="text-muted">
        You need at least 2 accounts to make a transfer.
      </div>
    );

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <CustomSelect
        label="From Account"
        placeholder="Select an account"
        value={sourceAccountId}
        onChange={(val) => setValue("sourceAccountId", val)}
        showDescriptionInTrigger={true}
        className="form-group"
        options={accounts.map((account) => ({
          value: account.id,
          label: account.name,
          description: `••${account.mask}`,
          rightLabel: formatCurrency(account.availableBalance),
        }))}
      />
      {errors.sourceAccountId && (
        <span className="form-error">{errors.sourceAccountId.message}</span>
      )}

      <CustomSelect
        label="To Account"
        placeholder="Select an account"
        value={destinationAccountId}
        onChange={(val) => setValue("destinationAccountId", val)}
        showDescriptionInTrigger={true}
        className="form-group"
        options={accounts.map((account) => ({
          value: account.id,
          label: account.name,
          description: `••${account.mask}`,
        }))}
      />
      {errors.destinationAccountId && (
        <span className="form-error">
          {errors.destinationAccountId.message}
        </span>
      )}

      <div className="form-group">
        <label className="form-label" htmlFor="amount">
          Amount
        </label>
        <div style={{ position: "relative" }}>
          <span
            style={{
              position: "absolute",
              left: "12px",
              top: "50%",
              transform: "translateY(-50%)",
              color: "var(--ink-muted)",
              fontFamily: "var(--mono)",
            }}
          >
            ₹
          </span>
          <input
            type="number"
            step="0.01"
            id="amount"
            className="form-input"
            style={{ paddingLeft: "28px", width: "100%" }}
            {...register("amount", { valueAsNumber: true })}
          />
        </div>
        {errors.amount && (
          <span className="form-error">{errors.amount.message}</span>
        )}
        {sourceAccount && (
          <span
            className="text-label"
            style={{ marginTop: "4px", display: "block" }}
          >
            Available: {formatCurrency(sourceAccount.availableBalance)}
          </span>
        )}
      </div>

      <button
        type="submit"
        className="btn btn-primary btn-full"
        disabled={transferMutation.isPending}
        style={{ marginTop: "1rem" }}
      >
        {transferMutation.isPending ? "Processing..." : "Complete Transfer"}
      </button>
    </form>
  );
};
