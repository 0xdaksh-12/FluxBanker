import { NavLink, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import { useThemeStore } from "../../store/themeStore";
import { apiClient } from "../../api/client";
import { toast } from "sonner";
import styles from "./Navbar.module.css";

export const Navbar = () => {
  const { user, logout } = useAuthStore();
  const { theme, toggle } = useThemeStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await apiClient.post("/auth/logout");
    } catch {
      // Proceed with client-side logout even if API fails
    } finally {
      logout();
      navigate("/login");
      toast.success("SESSION_TERMINATED");
    }
  };

  return (
    <nav className={styles.navbar}>
      <NavLink
        to="/"
        end
        className={({ isActive }) =>
          `${styles.link} ${isActive ? styles.linkActive : ""}`
        }
      >
        <div className={styles.brand}>
          <img src="/user-logo.jpeg" alt="" className={styles.brandLogo} />
          <span className={styles.brandName}>FluxBanker</span>
        </div>
      </NavLink>

      <div className={styles.links}>
        <NavLink
          to="/dashboard"
          className={({ isActive }) =>
            `${styles.link} ${isActive ? styles.linkActive : ""}`
          }
        >
          <span className="material-symbols-outlined">dashboard</span>
          Dashboard
        </NavLink>
        <NavLink
          to="/accounts"
          className={({ isActive }) =>
            `${styles.link} ${isActive ? styles.linkActive : ""}`
          }
        >
          <span className="material-symbols-outlined">account_balance</span>
          Accounts
        </NavLink>
        <NavLink
          to="/transactions"
          className={({ isActive }) =>
            `${styles.link} ${isActive ? styles.linkActive : ""}`
          }
        >
          <span className="material-symbols-outlined">receipt_long</span>
          LEDGER
        </NavLink>
        <NavLink
          to="/transfer"
          className={({ isActive }) =>
            `${styles.link} ${isActive ? styles.linkActive : ""}`
          }
        >
          <span className="material-symbols-outlined">swap_horiz</span>
          Transfer
        </NavLink>
        {user?.role === "ADMIN" && (
          <NavLink
            to="/admin"
            className={({ isActive }) =>
              `${styles.link} ${isActive ? styles.linkActive : ""}`
            }
          >
            <span className="material-symbols-outlined">
              admin_panel_settings
            </span>
            Admin
          </NavLink>
        )}
      </div>

      <div className={styles.user}>
        <span className={styles.userLabel}>
          {user?.firstName} {user?.lastName}
        </span>
        <button
          className={styles.themeBtn}
          onClick={toggle}
          title={
            theme === "dark" ? "Switch to Light Mode" : "Switch to Dark Mode"
          }
        >
          <span className="material-symbols-outlined">
            {theme === "dark" ? "light_mode" : "dark_mode"}
          </span>
        </button>
        <button className={styles.logoutBtn} onClick={handleLogout}>
          <span className="material-symbols-outlined">logout</span>
        </button>
      </div>
    </nav>
  );
};
