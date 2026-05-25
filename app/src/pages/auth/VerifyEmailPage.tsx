import { useEffect, useState, useRef } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import { verifyEmail } from "../../api/auth";
import { toast } from "sonner";
import "../../styles/pages.css";

export const VerifyEmailPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token");
  const [status, setStatus] = useState<"loading" | "success" | "error">(token ? "loading" : "error");
  const [errorMsg, setErrorMsg] = useState(token ? "" : "No verification token provided in the URL.");
  
  // Using a ref to prevent strict mode double-firing from attempting to verify twice
  const hasAttempted = useRef(false);

  useEffect(() => {
    if (!token || hasAttempted.current || status !== "loading") return;
    
    hasAttempted.current = true;

    const performVerification = async () => {
      try {
        await verifyEmail(token);
        setStatus("success");
        toast.success("Email verified successfully! You can now log in.");
      } catch (err: any) {
        setStatus("error");
        if (err.response?.data?.message) {
          setErrorMsg(err.response.data.message);
        } else {
          setErrorMsg("Invalid or expired verification token. Please request a new one.");
        }
      }
    };

    performVerification();
  }, [token, status]);

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: "450px" }}>
        <div className="auth-header">
          <img
            src="/user-logo.jpeg"
            alt="Logo"
            style={{ width: 48, height: 48, marginBottom: "1rem" }}
          />
          <h1>Email Verification</h1>
          <p>Verifying your premium FluxBanker credentials</p>
        </div>
        
        {status === "loading" && (
          <div style={{ textAlign: "center", padding: "2rem 0" }}>
            <div 
              className="spinner" 
              style={{ 
                margin: "0 auto 1.5rem", 
                width: "40px", 
                height: "40px", 
                border: "var(--bw-thick) solid var(--border-light)", 
                borderTop: "var(--bw-thick) solid var(--ink)", 
                animation: "spin 1s linear infinite" 
              }} 
            />
            <p className="text-muted">Verifying your email address...</p>
            <style>
              {`
                @keyframes spin {
                  0% { transform: rotate(0deg); }
                  100% { transform: rotate(360deg); }
                }
              `}
            </style>
          </div>
        )}

        {status === "success" && (
          <div style={{ textAlign: "center", padding: "2rem 0" }}>
            <div 
              className="material-symbols-outlined" 
              style={{ fontSize: "64px", color: "var(--positive)", marginBottom: "1.5rem" }}
            >
              check_circle
            </div>
            <p style={{ marginBottom: "1.5rem", fontSize: "14px" }}>
              Your email has been successfully verified! Your account is now fully active.
            </p>
            <button
              className="btn btn-primary"
              style={{ width: "100%" }}
              onClick={() => navigate("/login")}
            >
              Go to Login
            </button>
          </div>
        )}

        {status === "error" && (
          <div style={{ textAlign: "center", padding: "2rem 0" }}>
            <div 
              className="material-symbols-outlined" 
              style={{ fontSize: "64px", color: "var(--negative)", marginBottom: "1.5rem" }}
            >
              cancel
            </div>
            <p style={{ marginBottom: "1.5rem", fontWeight: 700, fontSize: "14px" }}>
              {errorMsg}
            </p>
            <Link 
              to="/login" 
              className="btn btn-outline" 
              style={{ display: "block", width: "100%", textAlign: "center" }}
            >
              Return to Login
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};
