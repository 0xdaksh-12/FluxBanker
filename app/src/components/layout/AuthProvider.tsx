import React, { useEffect } from "react";
import { useAuthStore } from "../../store/authStore";
import { refresh } from "../../api/auth";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const setAuth = useAuthStore((state) => state.setAuth);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const setInitializing = useAuthStore((state) => state.setInitializing);

  useEffect(() => {
    const bootstrap = async () => {
      // If we don't have a token in the store but we might have a refresh cookie
      if (!isAuthenticated) {
        const response = await refresh();
        if (response) {
          setAuth(response.user, response.accessToken);
        }
      }
      setInitializing(false);
    };

    bootstrap();
  }, [isAuthenticated, setAuth, setInitializing]);

  return <>{children}</>;
};
