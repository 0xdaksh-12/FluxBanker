import { create } from "zustand";
import { persist } from "zustand/middleware";

type Theme = "light" | "dark";

interface ThemeState {
  theme: Theme;
  toggle: () => void;
  setTheme: (theme: Theme) => void;
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set, get) => ({
      theme: "light",
      toggle: () => {
        const next = get().theme === "light" ? "dark" : "light";
        set({ theme: next });
        applyTheme(next);
      },
      setTheme: (theme) => {
        set({ theme });
        applyTheme(theme);
      },
    }),
    { name: "fluxbanker-theme" },
  ),
);

/** Applies the theme to the <html> element data attribute. */
function applyTheme(theme: Theme) {
  document.documentElement.setAttribute("data-theme", theme);
}

/** Call once on app boot to sync persisted theme. */
export function initTheme() {
  const stored = useThemeStore.getState().theme;
  applyTheme(stored);
}
