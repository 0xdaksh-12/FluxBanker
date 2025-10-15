import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import { Navbar } from "./Navbar";

const AdminRoute = () => {
  const { isAuthenticated, user, isInitializing } = useAuthStore();

  if (isInitializing) {
    return (
      <div style={{ 
        height: '100vh', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        background: 'var(--paper)',
        color: 'var(--ink)'
      }}>
        <div className="loading-spinner">Initializing Session...</div>
      </div>
    );
  }

  // Redirect to dashboard if not authenticated or not an ADMIN
  if (!isAuthenticated || user?.role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="layout-container">
      <Navbar />
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
};

export default AdminRoute;
