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
    <div className="page-container" style={{ justifyContent: "center", minHeight: "100vh" }}>
      <div className="card" style={{ maxWidth: "400px", width: "100%", padding: "2rem", textAlign: "center" }}>
        <h2 style={{ marginBottom: "1rem" }}>Email Verification</h2>
        
        {status === "loading" && (
          <div>
            <div className="spinner" style={{ margin: "0 auto 1rem", width: "40px", height: "40px", border: "4px solid var(--border-color)", borderTop: "4px solid var(--primary-color)", borderRadius: "50%", animation: "spin 1s linear infinite" }} />
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
          <div>
            <div style={{ color: "var(--success-color)", fontSize: "3rem", marginBottom: "1rem" }}>✓</div>
            <p style={{ marginBottom: "1.5rem" }}>
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
          <div>
            <div style={{ color: "var(--danger-color)", fontSize: "3rem", marginBottom: "1rem" }}>✗</div>
            <p className="text-danger" style={{ marginBottom: "1.5rem", fontWeight: 500 }}>
              {errorMsg}
            </p>
            <Link to="/login" className="btn btn-outline" style={{ display: "block", width: "100%", textAlign: "center" }}>
              Return to Login
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};
