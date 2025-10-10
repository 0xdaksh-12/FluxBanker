import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuthStore } from "../../store/authStore";
import { register as registerApi } from "../../api/auth";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import styles from "./RegisterForm.module.css";

// Step 1 schema — account credentials
const step1Schema = z.object({
  firstName: z.string().min(2, "First name is required"),
  lastName: z.string().min(2, "Last name is required"),
  email: z.email("Invalid email address"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

// Step 2 schema — profile & identity
const step2Schema = z.object({
  dateOfBirth: z.string().min(1, "Date of birth is required"),
  aadhaar: z.string().regex(/^\d{12}$/, "Aadhaar must be exactly 12 digits"),
  address1: z.string().min(5, "Address is required"),
  city: z.string().min(2, "City is required"),
  state: z.string().length(2, "Use 2-letter state code (e.g. MH, DL)"),
  pinCode: z.string().regex(/^\d{6}$/, "PIN code must be 6 digits"),
});

type Step1Values = z.infer<typeof step1Schema>;
type Step2Values = z.infer<typeof step2Schema>;

export const RegisterForm = () => {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [step, setStep] = useState<1 | 2>(1);
  const [step1Data, setStep1Data] = useState<Step1Values | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  const form1 = useForm<Step1Values>({
    resolver: zodResolver(step1Schema),
  });

  const form2 = useForm<Step2Values>({
    resolver: zodResolver(step2Schema),
  });

  // Step 1: validate and advance
  const handleStep1 = form1.handleSubmit((data) => {
    setStep1Data(data);
    setStep(2);
  });

  // Step 2: register + patch profile
  const handleStep2 = form2.handleSubmit(async (profileData) => {
    if (!step1Data) return;
    try {
      const response = await registerApi(
        step1Data.firstName,
        step1Data.lastName,
        step1Data.email,
        step1Data.password,
        profileData.dateOfBirth,
        profileData.aadhaar,
        profileData.address1,
        profileData.city,
        profileData.state,
        profileData.pinCode,
      );
      setAuth(response.user, response.accessToken);

      toast.success("Account created successfully!");
      navigate("/");
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        toast.error(
          error.response?.data?.message || "Failed to create account.",
        );
      } else {
        toast.error("An unexpected error occurred.");
      }
    }
  });

  return (
    <div>
      {/* Progress indicator */}
      <div className={styles.progress}>
        <div
          className={`${styles.progressStep} ${step >= 1 ? styles.progressStepActive : ""}`}
        >
          <span className={styles.progressDot}>
            {step > 1 ? (
              <span
                className="material-symbols-outlined"
                style={{ fontSize: "14px" }}
              >
                check
              </span>
            ) : (
              "01"
            )}
          </span>
          <span className={styles.progressLabel}>Credentials</span>
        </div>
        <div className={styles.progressLine} />
        <div
          className={`${styles.progressStep} ${step >= 2 ? styles.progressStepActive : ""}`}
        >
          <span className={styles.progressDot}>02</span>
          <span className={styles.progressLabel}>Identity</span>
        </div>
      </div>

      {/* Step 1 */}
      {step === 1 && (
        <form onSubmit={handleStep1} autoComplete="off">
          <div className={styles.nameRow}>
            <div className="form-group">
              <label className="form-label" htmlFor="firstName">
                First Name
              </label>
              <input
                id="firstName"
                className="form-input"
                {...form1.register("firstName")}
                autoComplete="off"
                spellCheck={false}
              />
              {form1.formState.errors.firstName && (
                <span className="form-error">
                  {form1.formState.errors.firstName.message}
                </span>
              )}
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="lastName">
                Last Name
              </label>
              <input
                id="lastName"
                className="form-input"
                {...form1.register("lastName")}
                autoComplete="off"
                spellCheck={false}
              />
              {form1.formState.errors.lastName && (
                <span className="form-error">
                  {form1.formState.errors.lastName.message}
                </span>
              )}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="reg-email">
              Email
            </label>
            <input
              id="reg-email"
              type="email"
              className="form-input"
              {...form1.register("email")}
              autoComplete="off"
              spellCheck={false}
            />
            {form1.formState.errors.email && (
              <span className="form-error">
                {form1.formState.errors.email.message}
              </span>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="reg-password">
              Password
            </label>
            <div style={{ position: "relative" }}>
              <input
                id="reg-password"
                type={showPassword ? "text" : "password"}
                className="form-input"
                {...form1.register("password")}
                style={{ paddingRight: "40px" }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: "absolute",
                  right: "12px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  background: "none",
                  border: "none",
                  padding: 0,
                  display: "grid",
                  placeItems: "center",
                  color: "var(--ink-muted)",
                  cursor: "pointer",
                }}
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                <span className="material-symbols-outlined" style={{ fontSize: "20px" }}>
                  {showPassword ? "visibility_off" : "visibility"}
                </span>
              </button>
            </div>
            {form1.formState.errors.password && (
              <span className="form-error">
                {form1.formState.errors.password.message}
              </span>
            )}
          </div>

          <button
            type="submit"
            className={`btn btn-primary ${styles.submitBtn}`}
          >
            Next
            <span
              className="material-symbols-outlined"
              style={{ fontSize: "18px" }}
            >
              arrow_forward
            </span>
          </button>
        </form>
      )}

      {/* Step 2 */}
      {step === 2 && (
        <form onSubmit={handleStep2} autoComplete="off">
          <div className="form-group">
            <label className="form-label" htmlFor="dob">
              Date of Birth
            </label>
            <input
              id="dob"
              type="date"
              className="form-input"
              {...form2.register("dateOfBirth")}
            />
            {form2.formState.errors.dateOfBirth && (
              <span className="form-error">
                {form2.formState.errors.dateOfBirth.message}
              </span>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="ssn">
              Aadhaar Number
            </label>
            <input
              id="ssn"
              type="password"
              inputMode="numeric"
              maxLength={12}
              className="form-input"
              placeholder="12 digits, no spaces"
              {...form2.register("aadhaar")}
            />
            {form2.formState.errors.aadhaar && (
              <span className="form-error">
                {form2.formState.errors.aadhaar.message}
              </span>
            )}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="address1">
              Street Address
            </label>
            <input
              id="address1"
              className="form-input"
              placeholder="123 Main St"
              {...form2.register("address1")}
            />
            {form2.formState.errors.address1 && (
              <span className="form-error">
                {form2.formState.errors.address1.message}
              </span>
            )}
          </div>

          <div className={styles.addressRow}>
            <div className="form-group">
              <label className="form-label" htmlFor="city">
                City
              </label>
              <input
                id="city"
                className="form-input"
                {...form2.register("city")}
              />
              {form2.formState.errors.city && (
                <span className="form-error">
                  {form2.formState.errors.city.message}
                </span>
              )}
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="state">
                State
              </label>
              <input
                id="state"
                className="form-input"
                maxLength={2}
                placeholder="MH"
                style={{ textTransform: "uppercase" }}
                {...form2.register("state")}
              />
              {form2.formState.errors.state && (
                <span className="form-error">
                  {form2.formState.errors.state.message}
                </span>
              )}
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="postalCode">
                PIN Code
              </label>
              <input
                id="postalCode"
                className="form-input"
                inputMode="numeric"
                maxLength={6}
                placeholder="400001"
                {...form2.register("pinCode")}
              />
              {form2.formState.errors.pinCode && (
                <span className="form-error">
                  {form2.formState.errors.pinCode.message}
                </span>
              )}
            </div>
          </div>

          <div className={styles.step2Actions}>
            <button
              type="button"
              className={`btn btn-outline ${styles.backBtn}`}
              onClick={() => setStep(1)}
            >
              <span
                className="material-symbols-outlined"
                style={{ fontSize: "18px" }}
              >
                arrow_back
              </span>
              Back
            </button>
            <button
              type="submit"
              className={`btn btn-primary ${styles.submitBtn}`}
              disabled={form2.formState.isSubmitting}
            >
              {form2.formState.isSubmitting
                ? "Creating account..."
                : "Create Account"}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};
