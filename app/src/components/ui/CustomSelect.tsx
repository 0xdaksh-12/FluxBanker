import React, { useState, useRef, useEffect } from "react";
import styles from "./CustomSelect.module.css";

interface Option {
  value: string;
  label: string;
  description?: string;
  rightLabel?: string;
}

interface CustomSelectProps {
  label?: string;
  options: Option[];
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  className?: string;
  showDescriptionInTrigger?: boolean;
  disabled?: boolean;
  compact?: boolean;
}

export const CustomSelect: React.FC<CustomSelectProps> = ({
  label,
  options,
  value,
  onChange,
  placeholder = "Select an option",
  className = "",
  showDescriptionInTrigger = false,
  disabled = false,
  compact = false,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const selectedOption = options.find((opt) => opt.value === value);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className={`${styles.container} ${className}`} ref={containerRef}>
      {label && <label className="form-label">{label}</label>}
      <div className={styles.wrapper}>
        <button
          type="button"
          className={`form-input ${styles.trigger} ${compact ? styles.compact : ""}`}
          onClick={() => !disabled && setIsOpen(!isOpen)}
          disabled={disabled}
        >
          <div className={selectedOption ? "" : styles.placeholder}>
            {selectedOption ? (
              <div className={styles.triggerContent}>
                <span className={styles.triggerLabel}>
                  {selectedOption.label}
                </span>
                {showDescriptionInTrigger && selectedOption.description && (
                  <span className={styles.triggerDescription}>
                    {" "}
                    ({selectedOption.description})
                  </span>
                )}
              </div>
            ) : (
              placeholder
            )}
          </div>
          <span className="material-symbols-outlined">
            {isOpen ? "expand_less" : "expand_more"}
          </span>
        </button>

        {isOpen && (
          <div className={styles.dropdown}>
            {options.map((option) => (
              <button
                key={option.value}
                type="button"
                className={`${styles.option} ${
                  value === option.value ? styles.optionSelected : ""
                }`}
                onClick={() => {
                  onChange(option.value);
                  setIsOpen(false);
                }}
              >
                <div className={styles.optionContent}>
                  <div>
                    <div className={styles.optionLabel}>{option.label}</div>
                    {option.description && (
                      <div className={styles.optionDescription}>
                        {option.description}
                      </div>
                    )}
                  </div>
                  {option.rightLabel && (
                    <div className={styles.optionRightLabel}>
                      {option.rightLabel}
                    </div>
                  )}
                </div>
                {value === option.value && (
                  <span
                    className="material-symbols-outlined"
                    style={{ fontSize: "18px", marginLeft: "8px" }}
                  >
                    check
                  </span>
                )}
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
