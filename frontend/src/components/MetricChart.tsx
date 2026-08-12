import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { SystemMetric } from "../types";
import { formatChartTime, formatNumber } from "../utils";

interface MetricChartProps {
  title: string;
  description: string;
  data: SystemMetric[];
  dataKey: "avgFrequency" | "avgVoltage";
  rangeMs: number;
  unit: string;
  color: string;
  gradientId: string;
  digits: number;
}

export function MetricChart({
  title,
  description,
  data,
  dataKey,
  rangeMs,
  unit,
  color,
  gradientId,
  digits,
}: MetricChartProps) {
  const values = data.map((point) => point[dataKey]);
  const minimum = values.length ? Math.min(...values) : null;
  const maximum = values.length ? Math.max(...values) : null;

  return (
    <figure className="chart-card" aria-label={`${title} time-series chart`}>
      <div className="chart-card__header">
        <div>
          <h3>{title}</h3>
          <p>{description}</p>
        </div>
        <div className="chart-card__range" aria-label={`${title} range summary`}>
          <span>Low <strong>{minimum == null ? "—" : formatNumber(minimum, digits)} {unit}</strong></span>
          <span>High <strong>{maximum == null ? "—" : formatNumber(maximum, digits)} {unit}</strong></span>
        </div>
      </div>
      <div className="chart-card__plot">
        {data.length ? (
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data} margin={{ top: 14, right: 8, left: -16, bottom: 0 }}>
              <defs>
                <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor={color} stopOpacity={0.28} />
                  <stop offset="95%" stopColor={color} stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid stroke="rgba(148, 163, 184, 0.1)" vertical={false} />
              <XAxis
                dataKey="timestamp"
                tickFormatter={(timestamp) => formatChartTime(timestamp, rangeMs)}
                axisLine={false}
                tickLine={false}
                minTickGap={42}
                tick={{ fill: "#768397", fontSize: 11 }}
              />
              <YAxis
                domain={["auto", "auto"]}
                axisLine={false}
                tickLine={false}
                width={58}
                tickFormatter={(value: number) => value.toFixed(digits)}
                tick={{ fill: "#768397", fontSize: 11 }}
              />
              <Tooltip
                labelFormatter={(timestamp) => new Date(Number(timestamp)).toLocaleString()}
                formatter={(value) => [`${Number(value).toFixed(digits)} ${unit}`, title]}
                contentStyle={{
                  background: "#111a26",
                  border: "1px solid rgba(148, 163, 184, 0.2)",
                  borderRadius: 10,
                  boxShadow: "0 16px 40px rgba(0, 0, 0, 0.3)",
                }}
                labelStyle={{ color: "#92a0b3", marginBottom: 4 }}
                itemStyle={{ color: "#edf4fb" }}
              />
              <Area
                type="monotone"
                dataKey={dataKey}
                stroke={color}
                strokeWidth={2.25}
                fill={`url(#${gradientId})`}
                activeDot={{ r: 4, fill: color, stroke: "#091019", strokeWidth: 2 }}
                isAnimationActive={false}
              />
            </AreaChart>
          </ResponsiveContainer>
        ) : (
          <div className="empty-state empty-state--chart">
            <span className="empty-state__line" />
            <p>No metric samples in this period.</p>
          </div>
        )}
      </div>
      <figcaption className="sr-only">
        {data.length
          ? `${data.length} samples. Minimum ${minimum?.toFixed(digits)} ${unit}, maximum ${maximum?.toFixed(digits)} ${unit}.`
          : `No ${title.toLowerCase()} samples are available for this period.`}
      </figcaption>
    </figure>
  );
}
