import type { Alert, FaultAlert, FrequencyAlert, Severity } from "./types";

export const severityOrder: Record<Severity, number> = {
  Low: 1,
  Medium: 2,
  High: 3,
  Critical: 4,
};

export function formatDateTime(timestamp: number): string {
  return new Intl.DateTimeFormat(undefined, {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  }).format(timestamp);
}

export function formatTime(timestamp: number): string {
  return new Intl.DateTimeFormat(undefined, {
    hour: "2-digit",
    minute: "2-digit",
  }).format(timestamp);
}

export function formatChartTime(timestamp: number, rangeMs: number): string {
  const options: Intl.DateTimeFormatOptions = rangeMs >= 24 * 60 * 60 * 1000
    ? { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" }
    : { hour: "2-digit", minute: "2-digit" };
  return new Intl.DateTimeFormat(undefined, options).format(timestamp);
}

export function formatNumber(value: number, digits = 2): string {
  return Number.isFinite(value) ? value.toFixed(digits) : "—";
}

export function alertTitle(alert: Alert): string {
  return alert.kind === "fault" ? alert.alertType : alert.alertDisplayName;
}

export function alertMessage(alert: Alert): string {
  return alert.kind === "fault" ? alert.description : alert.message;
}

export function isFaultAlert(alert: Alert): alert is FaultAlert {
  return alert.kind === "fault";
}

export function isFrequencyAlert(alert: Alert): alert is FrequencyAlert {
  return alert.kind === "frequency";
}
