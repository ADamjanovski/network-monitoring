import type { ReactNode } from "react";
import { ArrowDownRight, ArrowUpRight, Minus } from "lucide-react";

interface MetricCardProps {
  label: string;
  value: string;
  unit?: string;
  detail: string;
  icon: ReactNode;
  accent: "cyan" | "amber" | "violet" | "green";
  change?: number | null;
  changeUnit?: string;
}

export function MetricCard({
  label,
  value,
  unit,
  detail,
  icon,
  accent,
  change,
  changeUnit = "",
}: MetricCardProps) {
  const direction = change == null || change === 0 ? "flat" : change > 0 ? "up" : "down";
  const TrendIcon = direction === "up" ? ArrowUpRight : direction === "down" ? ArrowDownRight : Minus;

  return (
    <article className={`metric-card metric-card--${accent}`}>
      <div className="metric-card__header">
        <span>{label}</span>
        <span className="metric-card__icon" aria-hidden="true">{icon}</span>
      </div>
      <div className="metric-card__value">
        {value}<span>{unit}</span>
      </div>
      <div className="metric-card__footer">
        {change != null ? (
          <span className={`trend trend--${direction}`}>
            <TrendIcon size={14} aria-hidden="true" />
            {Math.abs(change).toFixed(changeUnit === " Hz" ? 3 : 1)}{changeUnit}
          </span>
        ) : null}
        <span>{detail}</span>
      </div>
    </article>
  );
}
