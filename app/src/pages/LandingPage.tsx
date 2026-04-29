import { Link } from "react-router-dom";
import styles from "./LandingPage.module.css";
import { useAuthStore } from "../store/authStore";
import { useThemeStore } from "../store/themeStore";

export const LandingPage = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const theme = useThemeStore((state) => state.theme);
  const toggleTheme = useThemeStore((state) => state.toggle);

  return (
    <div className={styles.container}>
      <nav className={styles.nav}>
        <div className={styles.logo}>
          <img src="/user-logo.jpeg" alt="Logo" className={styles.logoImage} />
          <span className={styles.logoText}>FLUXBANKER</span>
        </div>
        <div className={styles.navLinks}>
          <button
            onClick={toggleTheme}
            className="btn btn-outline"
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              padding: "0.5rem",
              marginRight: "0.5rem",
            }}
            aria-label="Toggle theme"
          >
            <span className="material-symbols-outlined">
              {theme === "light" ? "dark_mode" : "light_mode"}
            </span>
          </button>
          {isAuthenticated ? (
            <Link to="/dashboard" className="btn btn-primary">
              ENTER FLUXBANKER
            </Link>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline">
                LOGIN
              </Link>
              <Link to="/register" className="btn btn-primary">
                REGISTER
              </Link>
            </>
          )}
        </div>
      </nav>

      <main className={styles.main}>
        <div className={styles.hero}>
          <h1 className={styles.title}>
            THE <em>FUTURE</em> OF ASSET MANAGEMENT
          </h1>
          <p className={styles.subtitle}>
            Enterprise-grade banking simulator. Modern design. Acid-compliant
            transactions.
          </p>
          <div className={styles.ctaWrapper}>
            {isAuthenticated ? (
              <Link to="/dashboard" className={`btn btn-primary ${styles.cta}`}>
                ACCESS FLUXBANKER
              </Link>
            ) : (
              <Link to="/register" className={`btn btn-primary ${styles.cta}`}>
                INITIALIZE ACCOUNT
              </Link>
            )}
          </div>
        </div>

        <div className={styles.grid}>
          <div className={`bento-box ${styles.feature}`}>
            <h3>SWISS POSTER AESTHETICS</h3>
            <p>
              Built with bold typography, high contrast, and absolute clarity.
            </p>
          </div>
          <div className={`bento-box ${styles.feature}`}>
            <h3>REAL-TIME LEDGER</h3>
            <p>
              Immediate transaction settlement using Spring Boot and Postgres.
            </p>
          </div>
          <div className={`bento-box ${styles.feature}`}>
            <h3>OAUTH2 READY</h3>
            <p>Fast and secure authentication for your banking needs.</p>
          </div>
        </div>
      </main>

      <footer className={styles.footer}>
        <div className="divider">
          <span>SYSTEM OFFLINE / ONLINE STATUS: VERIFIED</span>
        </div>
        <p className={styles.copyright}>© 2026 FLUXBANKER TERMINAL</p>
      </footer>
    </div>
  );
};
