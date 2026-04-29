import { Link } from "react-router-dom";
import { RegisterForm } from "../../components/forms/RegisterForm";
import "../../styles/pages.css";

export const RegisterPage = () => {
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
          <span
            className="material-symbols-outlined"
            style={{ fontSize: "24px" }}
          >
            close
          </span>
        </Link>
        <div className="auth-header">
          <img
            src="/user-logo.jpeg"
            alt="Logo"
            style={{ width: 48, height: 48, marginBottom: "1rem" }}
          />
          <h1>Create an Account</h1>
          <p>Join FluxBanker today</p>
        </div>

        <RegisterForm />

        <div
          style={{
            marginTop: "2rem",
            textAlign: "center",
            fontSize: "0.875rem",
          }}
        >
          <span className="text-muted">Already have an account? </span>
          <Link
            to="/login"
            style={{
              color: "var(--ink)",
              fontWeight: 700,
              borderBottom: "2px solid var(--ink)",
            }}
          >
            Sign in
          </Link>
        </div>
      </div>
    </div>
  );
};
