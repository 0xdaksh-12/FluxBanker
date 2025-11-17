import React, { useState, useRef, useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuthStore } from "../../store/authStore";
import {
  useUpdateProfile,
  useVerifyEmail,
  useRequestPasswordReset,
  useMe,
} from "../../hooks/useUser";
import { Modal } from "./Modal";
import { toast } from "sonner";
import styles from "./ProfileModal.module.css";

const profileSchema = z.object({
  firstName: z.string().min(2, "First name is required"),
  lastName: z.string().min(2, "Last name is required"),
  dateOfBirth: z.string().min(1, "Date of birth is required"),
  address1: z.string().min(5, "Address is required"),
  city: z.string().min(2, "City is required"),
  state: z.string().length(2, "Use 2-letter state code (e.g. MH)"),
  pinCode: z.string().regex(/^\d{6}$/, "PIN code must be 6 digits"),
});

type ProfileValues = z.infer<typeof profileSchema>;

interface ProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
}

type Tab = "general" | "security";

export const ProfileModal = ({ isOpen, onClose }: ProfileModalProps) => {
  const [activeTab, setActiveTab] = useState<Tab>("general");
  const authUser = useAuthStore((state) => state.user);
  const { data: userData, isLoading: isUserLoading } = useMe(isOpen);
  const user = userData || authUser;
  const setAuth = useAuthStore((state) => state.setAuth);
  const accessToken = useAuthStore((state) => state.accessToken);
  
  const profileMutation = useUpdateProfile();
  const verifyEmailMutation = useVerifyEmail();
  const passwordResetMutation = useRequestPasswordReset();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 1024 * 1024) {
      toast.error("File size must be less than 1MB");
      return;
    }

    const reader = new FileReader();
    reader.onloadend = async () => {
      const base64String = reader.result as string;
      try {
        await profileMutation.mutateAsync({ profilePic: base64String });
        toast.success("Profile picture updated");
      } catch {
        toast.error("Failed to update profile picture");
      }
    };
    reader.readAsDataURL(file);
  };

  const {
    register,
    handleSubmit,
    reset: resetForm,
    formState: { errors, isSubmitting },
  } = useForm<ProfileValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: user?.firstName || "",
      lastName: user?.lastName || "",
      dateOfBirth: user?.dateOfBirth || "",
      address1: user?.address1 || "",
      city: user?.city || "",
      state: user?.state || "",
      pinCode: user?.pinCode || "",
    },
  });

  // Update local store when fresh data arrives
  useEffect(() => {
    if (userData && accessToken) {
      setAuth(userData, accessToken);
    }
  }, [userData, accessToken, setAuth]);

  // Re-sync form when user data changes
  useEffect(() => {
    if (user) {
      resetForm({
        firstName: user.firstName || "",
        lastName: user.lastName || "",
        dateOfBirth: user.dateOfBirth || "",
        address1: user.address1 || "",
        city: user.city || "",
        state: user.state || "",
        pinCode: user.pinCode || "",
      });
    }
  }, [user, resetForm, isOpen]);

  const onUpdateProfile = async (data: ProfileValues) => {
    try {
      await profileMutation.mutateAsync(data);
      toast.success("Profile updated successfully");
      onClose();
    } catch {
      toast.error("Failed to update profile");
    }
  };

  const handleVerifyEmail = async () => {
    try {
      await verifyEmailMutation.mutateAsync();
      toast.success("Email verified successfully");
    } catch {
      toast.error("Failed to verify email");
    }
  };

  const handlePasswordReset = async () => {
    try {
      await passwordResetMutation.mutateAsync();
      toast.success("Password reset link sent to your email!");
    } catch {
      toast.error("Failed to request password reset");
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="User Profile">
      <div className={styles.container}>
        {isUserLoading && !user ? (
          <div className={styles.loading}>Loading profile...</div>
        ) : (
          <>
            {/* Tab Switcher */}
        <div className={styles.tabs}>
          <button
            onClick={() => setActiveTab("general")}
            className={`${styles.tab} ${activeTab === "general" ? styles.tabActive : ""}`}
          >
            GENERAL INFO
          </button>
          <button
            onClick={() => setActiveTab("security")}
            className={`${styles.tab} ${activeTab === "security" ? styles.tabActive : ""}`}
          >
            SECURITY & ID
          </button>
        </div>

        <form onSubmit={handleSubmit(onUpdateProfile)} className={styles.form}>
          {activeTab === "general" ? (
            <div className={styles.tabContent}>
              <div className={styles.grid}>
                <div className="form-group">
                  <label className="form-label">First Name</label>
                  <input className="form-input" {...register("firstName")} />
                  {errors.firstName && (
                    <span className="form-error">
                      {errors.firstName.message}
                    </span>
                  )}
                </div>
                <div className="form-group">
                  <label className="form-label">Last Name</label>
                  <input className="form-input" {...register("lastName")} />
                  {errors.lastName && (
                    <span className="form-error">
                      {errors.lastName.message}
                    </span>
                  )}
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Date of Birth</label>
                <input
                  className="form-input"
                  type="date"
                  {...register("dateOfBirth")}
                />
                {errors.dateOfBirth && (
                  <span className="form-error">
                    {errors.dateOfBirth.message}
                  </span>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">Street Address</label>
                <input className="form-input" {...register("address1")} />
                {errors.address1 && (
                  <span className="form-error">{errors.address1.message}</span>
                )}
              </div>

              <div className={styles.addressGrid}>
                <div className="form-group">
                  <label className="form-label">City</label>
                  <input className="form-input" {...register("city")} />
                  {errors.city && (
                    <span className="form-error">{errors.city.message}</span>
                  )}
                </div>
                <div className="form-group">
                  <label className="form-label">State</label>
                  <input
                    className="form-input"
                    maxLength={2}
                    style={{ textTransform: "uppercase" }}
                    {...register("state")}
                  />
                  {errors.state && (
                    <span className="form-error">{errors.state.message}</span>
                  )}
                </div>
                <div className="form-group">
                  <label className="form-label">PIN</label>
                  <input
                    className="form-input"
                    maxLength={6}
                    {...register("pinCode")}
                  />
                  {errors.pinCode && (
                    <span className="form-error">{errors.pinCode.message}</span>
                  )}
                </div>
              </div>
            </div>
          ) : (
            <div className={styles.tabContent}>
              {/* Profile Picture Section */}
              <div className={styles.avatarSection}>
                <div className={styles.avatarWrapper}>
                  {user?.profilePic ? (
                    <img
                      src={user.profilePic}
                      alt="Profile"
                      className={styles.avatar}
                    />
                  ) : (
                    <div className={styles.avatarInitials}>
                      {user?.firstName?.[0]}
                      {user?.lastName?.[0]}
                    </div>
                  )}
                  <button
                    type="button"
                    className={styles.avatarEditBtn}
                    onClick={() => fileInputRef.current?.click()}
                  >
                    <span className="material-symbols-outlined">edit</span>
                  </button>
                </div>
                <div className={styles.avatarInfo}>
                  <p className="form-label">Profile Picture</p>
                  <p className={styles.avatarHint}>
                    Click the icon to upload a new photo (max 1MB)
                  </p>
                </div>
                <input
                  type="file"
                  ref={fileInputRef}
                  hidden
                  accept="image/*"
                  onChange={handleFileChange}
                />
              </div>

              {/* Status Section */}
              <div className={styles.statusSection}>
                <p className="form-label">Email Account Status</p>
                <div className={styles.statusRow}>
                  <span className={styles.emailText}>{user?.email}</span>
                  {user?.isEmailVerified ? (
                    <span className="status-badge status-completed">
                      <span
                        className="material-symbols-outlined"
                        style={{ fontSize: "14px" }}
                      >
                        check_circle
                      </span>
                      Verified
                    </span>
                  ) : (
                    <button
                      type="button"
                      onClick={handleVerifyEmail}
                      className="btn btn-primary"
                      style={{ padding: "4px 12px", fontSize: "10px" }}
                      disabled={verifyEmailMutation.isPending}
                    >
                      {verifyEmailMutation.isPending ? "Verifying..." : "Verify Now"}
                    </button>
                  )}
                </div>
              </div>

              {/* Identity Section (Locked Aadhaar) */}
              <div className={styles.securitySection}>
                <label className="form-label">Aadhaar Number</label>
                <div
                  style={{ position: "relative", marginTop: "var(--space-2)" }}
                >
                  <input
                    className="form-input"
                    value={user?.aadhaar || ""}
                    disabled
                    style={{
                      background: "var(--paper)",
                      opacity: 0.8,
                      cursor: "not-allowed",
                    }}
                  />
                  <span
                    className="material-symbols-outlined"
                    style={{
                      position: "absolute",
                      right: "12px",
                      top: "50%",
                      transform: "translateY(-50%)",
                      fontSize: "18px",
                      color: "var(--ink-muted)",
                      pointerEvents: "none",
                    }}
                    title="This field is locked for identity security."
                  >
                    lock
                  </span>
                </div>
              </div>

              {/* Password Section */}
              <div className={styles.securitySection}>
                <p className="form-label">Security Credentials</p>
                <button
                  type="button"
                  className="btn btn-outline"
                  style={{ width: "100%", marginTop: "var(--space-2)" }}
                  onClick={handlePasswordReset}
                  disabled={passwordResetMutation.isPending}
                >
                  <span
                    className="material-symbols-outlined"
                    style={{ fontSize: "18px" }}
                  >
                    lock_reset
                  </span>
                  {passwordResetMutation.isPending
                    ? "Sending Link..."
                    : "Update Password"}
                </button>
              </div>
            </div>
          )}

          <div className={styles.actions}>
            <button type="button" onClick={onClose} className="btn btn-outline">
              Cancel
            </button>
            {activeTab === "general" ? (
              <button
                type="submit"
                className="btn btn-primary"
                disabled={isSubmitting}
              >
                {isSubmitting ? "Saving..." : "Update Profile"}
              </button>
            ) : (
              <button
                type="button"
                onClick={onClose}
                className="btn btn-primary"
              >
                Close
              </button>
            )}
          </div>
        </form>
          </>
        )}
      </div>
    </Modal>
  );
};
