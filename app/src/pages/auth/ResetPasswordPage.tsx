import { useState, useEffect } from "react";
import { Link, useSearchParams, useNavigate } from "react-router-dom";
import { validateResetToken, resetPassword } from "../../api/auth";
import { toast } from "sonner";
import "../../styles/pages.css";

export const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token");

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [signOutAll, setSignOutAll] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isValidating, setIsValidating] = useState(true);
  const [isSuccess, setIsSuccess] = useState(false);

  useEffect(() => {
    if (!token) {
      toast.error("Invalid reset link");
      navigate("/login");
      return;
    }

    const validate = async () => {
      try {
        await validateResetToken(token);
        setIsValidating(false);
      } catch (error: any) {
        toast.error(error.response?.data?.message || "Token is invalid or expired");
        navigate("/login");
      }
    };

    validate();
  }, [token, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      toast.error("Passwords do not match");
      return;
    }

    if (password.length < 6) {
      toast.error("Password must be at least 6 characters");
      return;
    }

    setIsLoading(true);
    try {
      await resetPassword(token!, password, signOutAll);
      setIsSuccess(true);
      toast.success("Password reset successfully!");
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Failed to reset password");
    } finally {
      setIsLoading(false);
    }
  };

  if (isValidating) {
    return (
      <div className="auth-page">
        <div className="auth-card" style={{ textAlign: "center" }}>
          <p>Validating token...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: "450px" }}>
        {isSuccess ? (
          <div style={{ textAlign: "center", padding: "2rem 0" }}>
            <div
              className="material-symbols-outlined"
              style={{ fontSize: "64px", color: "var(--success)", marginBottom: "1rem" }}
            >
              check_circle
            </div>
            <h3>Success!</h3>
            <p className="text-muted">Your password has been reset. You can now log in.</p>
            <Link
              to="/login"
              className="btn btn-primary"
              style={{ marginTop: "1.5rem", display: "inline-block", width: "100%" }}
            >
              Go to Login
            </Link>
          </div>
        ) : (
          <>
            <div className="auth-header">
              <h1>Reset Password</h1>
              <p>Set your new premium security credentials</p>
            </div>

            <form onSubmit={handleSubmit} className="dialog-form">
              <div className="form-group">
                <label htmlFor="password">New Password</label>
                <input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="form-control"
                />
              </div>

              <div className="form-group">
                <label htmlFor="confirmPassword">Confirm Password</label>
                <input
                  id="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  className="form-control"
                />
              </div>

              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "10px",
                  margin: "1rem 0",
                  padding: "0.75rem",
                  background: "rgba(0,0,0,0.03)",
                  borderRadius: "8px",
                  cursor: "pointer"
                }}
                onClick={() => setSignOutAll(!signOutAll)}
              >
                <input
                  type="checkbox"
                  id="signOutAll"
                  checked={signOutAll}
                  onChange={(e) => setSignOutAll(e.target.checked)}
                  style={{ width: "18px", height: "18px" }}
                />
                <label htmlFor="signOutAll" style={{ marginBottom: 0, fontSize: "0.875rem", cursor: "pointer" }}>
                  Sign out from all other devices and locations
                </label>
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                style={{ width: "100%", marginTop: "1rem" }}
                disabled={isLoading}
              >
                {isLoading ? "Updating..." : "Reset Password"}
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  );
};
