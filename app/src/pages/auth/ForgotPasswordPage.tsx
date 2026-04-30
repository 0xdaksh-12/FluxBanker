import { useState } from "react";
import { Link } from "react-router-dom";
import { forgotPassword } from "../../api/auth";
import { toast } from "sonner";
import "../../styles/pages.css";

export const ForgotPasswordPage = () => {
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSent, setIsSent] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await forgotPassword(email);
      setIsSent(true);
      toast.success("Reset link sent to your email!");
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Failed to send reset link");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <Link
          to="/login"
          style={{
            position: "absolute",
            top: "20px",
            right: "20px",
            color: "var(--ink)",
            textDecoration: "none",
            display: "grid",
            placeItems: "center",
          }}
          aria-label="Back to login"
        >
          <span className="material-symbols-outlined" style={{ fontSize: "24px" }}>
            close
          </span>
        </Link>
        <div className="auth-header">
          <img
            src="/user-logo.jpeg"
            alt="Logo"
            style={{ width: 48, height: 48, marginBottom: "1rem" }}
          />
          <h1>Forgot Password</h1>
          <p>Enter your email to receive a password reset link</p>
        </div>

        {isSent ? (
          <div style={{ textAlign: "center", padding: "2rem 0" }}>
            <div
              className="material-symbols-outlined"
              style={{ fontSize: "64px", color: "var(--success)", marginBottom: "1rem" }}
            >
              check_circle
            </div>
            <h3>Email Sent!</h3>
            <p className="text-muted">
              Check your inbox at <strong>{email}</strong> for instructions.
            </p>
            <Link
              to="/login"
              className="btn btn-primary"
              style={{ marginTop: "1.5rem", display: "inline-block", width: "100%" }}
            >
              Back to Login
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <input
                id="email"
                type="email"
                placeholder="daksh@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="form-control"
              />
            </div>
            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: "100%", marginTop: "1rem" }}
              disabled={isLoading}
            >
              {isLoading ? "Sending..." : "Send Reset Link"}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
