import { Link } from "react-router-dom";
import { LoginForm } from "../../components/forms/LoginForm";
import "../../styles/pages.css";

export const LoginPage = () => {
  return (
    <div className="auth-page">
      <div className="auth-card">
        <Link
          to="/"
          style={{
            position: "absolute",
            top: "20px",
            right: "20px",
            color: "var(--ink)",
            textDecoration: "none",
            display: "grid",
            placeItems: "center",
          }}
          aria-label="Close"
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
          <h1>Sign in to FluxBanker</h1>
          <p>Welcome back to your premium banking dashboard</p>
        </div>

        <LoginForm />

        <div
          style={{
            marginTop: "2rem",
            textAlign: "center",
            fontSize: "0.875rem",
          }}
        >
          <span className="text-muted">Don't have an account? </span>
          <Link
            to="/register"
            style={{ color: "var(--ink)", fontWeight: 700, borderBottom: "2px solid var(--ink)" }}
          >
            Sign up
          </Link>
        </div>
      </div>
    </div>
  );
};
