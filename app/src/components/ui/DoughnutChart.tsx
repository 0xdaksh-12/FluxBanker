import { Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import type { Account } from "../../types";

const COLORS = ["#1C3829", "#166534", "#6B7A69", "#DDD9CF", "#1C2B1A"];

interface DoughnutChartProps {
  accounts: Account[];
}

export const DoughnutChart = ({ accounts }: DoughnutChartProps) => {
  const data = accounts
    .map((a, index) => ({
      name: a.name,
      value: a.currentBalance,
      fill: COLORS[index % COLORS.length],
    }))
    .filter((a) => a.value > 0);

  if (data.length === 0) {
    return (
      <div
        style={{
          height: "100%",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          color: "var(--ink-muted)",
        }}
      >
        No balance data
      </div>
    );
  }

  return (
    <div style={{ width: "100%", height: "100%", position: "relative" }}>
      <div
        style={{ position: "absolute", top: 0, left: 0, right: 0, bottom: 0 }}
      >
        <ResponsiveContainer
          width="100%"
          height="100%"
          minWidth={0}
          minHeight={0}
        >
          <PieChart margin={{ top: 10, right: 10, bottom: 10, left: 10 }}>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius="65%"
              outerRadius="90%"
              paddingAngle={3}
              dataKey="value"
              stroke="none"
            />
            <Tooltip
              formatter={(
                value:
                  | number
                  | string
                  | readonly (string | number)[]
                  | undefined,
              ) =>
                typeof value === "number"
                  ? new Intl.NumberFormat("en-IN", {
                      style: "currency",
                      currency: "INR",
                      maximumFractionDigits: 0,
                    }).format(value)
                  : (value ?? "")
              }
              contentStyle={{
                backgroundColor: "var(--paper)",
                border: "2px solid var(--border)",
                borderRadius: "0",
              }}
              itemStyle={{
                color: "var(--ink)",
                fontFamily: "var(--mono)",
              }}
            />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};
