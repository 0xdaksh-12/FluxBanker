export const Skeleton = ({
  width,
  height,
  className = "",
}: {
  width?: string | number;
  height?: string | number;
  className?: string;
}) => {
  return (
    <div
      className={`skeleton ${className}`}
      style={{
        width: width || "100%",
        height: height || "20px",
      }}
    />
  );
};

export const BankCardSkeleton = () => (
  <div className="bento-box" style={{ minHeight: "200px", display: "flex", flexDirection: "column", gap: "16px" }}>
    <Skeleton width="60%" height="24px" />
    <Skeleton width="80%" height="40px" />
    <Skeleton width="100%" height="16px" />
  </div>
);
