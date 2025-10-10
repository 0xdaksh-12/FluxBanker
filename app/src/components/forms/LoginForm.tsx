import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuthStore } from "../../store/authStore";
import { login } from "../../api/auth";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import styles from "./LoginForm.module.css";
import { useState } from "react";

const loginSchema = z.object({
  email: z.email("Invalid Email"),
  password: z.string().min(8, "Invalid Password"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export const LoginForm = () => {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormValues) => {
    try {
      const response = await login(data.email, data.password);
      setAuth(response.user, response.accessToken);
      toast.success("Login Successful");
      navigate("/");
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        toast.error(error.response?.data?.message || "Login Failed");
      } else {
        toast.error("Unknown Error");
      }
    }
  };

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className={styles.form}
      autoComplete="off"
    >
      <div className="form-group">
        <label className="form-label" htmlFor="email">
          Email
        </label>
        <input
          id="email"
          type="email"
          className="form-input"
          {...register("email")}
          placeholder="USER@FLUX.SYS"
          autoComplete="off"
          spellCheck={false}
        />
        {errors.email && (
          <span className="form-error">{errors.email.message}</span>
        )}
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="password">
          Password
        </label>
        <div style={{ position: "relative" }}>
          <input
            id="password"
            type={showPassword ? "text" : "password"}
            className="form-input"
            {...register("password")}
            placeholder="••••••••"
            autoComplete="off"
            style={{ paddingRight: "40px" }}
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            style={{
              position: "absolute",
              right: "12px",
              top: "50%",
              transform: "translateY(-50%)",
              background: "none",
              border: "none",
              padding: 0,
              display: "grid",
              placeItems: "center",
              color: "var(--ink-muted)",
              cursor: "pointer",
            }}
            aria-label={showPassword ? "Hide password" : "Show password"}
          >
            <span
              className="material-symbols-outlined"
              style={{ fontSize: "20px" }}
            >
              {showPassword ? "visibility_off" : "visibility"}
            </span>
          </button>
        </div>
        {errors.password && (
          <span className="form-error">{errors.password.message}</span>
        )}
      </div>

      <button
        type="submit"
        className={`btn btn-primary ${styles.submitBtn}`}
        disabled={isSubmitting}
      >
        {isSubmitting ? "Verifying..." : "Login"}
      </button>
    </form>
  );
};
