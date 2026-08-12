import { useEffect } from "react";
import { Activity, MapPin, X } from "lucide-react";
import type { Alert } from "../types";
import { alertMessage, alertTitle, formatDateTime, formatNumber } from "../utils";

interface AlertDetailProps {
  alert: Alert | null;
  onClose: () => void;
}

export function AlertDetail({ alert, onClose }: AlertDetailProps) {
  useEffect(() => {
    if (!alert) return;
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    document.addEventListener("keydown", closeOnEscape);
    return () => document.removeEventListener("keydown", closeOnEscape);
  }, [alert, onClose]);

  if (!alert) return null;

  const details = alert.kind === "fault"
    ? [
        ["PMU ID", alert.pmuId],
        ["Substation", alert.substation],
        ["Location", alert.location],
        ["Measured value", formatNumber(alert.measuredValue, 3)],
        ["Threshold", formatNumber(alert.threshold, 3)],
        ["Voltage", formatNumber(alert.voltage, 2)],
        ["Current", formatNumber(alert.current, 2)],
        ["Frequency", `${formatNumber(alert.frequency, 3)} Hz`],
      ]
    : [
        ["Average frequency", `${formatNumber(alert.avgFrequency, 3)} Hz`],
        ["Frequency range", `${formatNumber(alert.minFrequency, 3)}–${formatNumber(alert.maxFrequency, 3)} Hz`],
        ["Deviation", `${formatNumber(alert.frequencyDeviation, 3)} Hz`],
        ["RoCoF", `${formatNumber(alert.rocof, 3)} Hz/s`],
        ["RoCoF volatility", formatNumber(alert.rocofVolatility, 3)],
        ["Measurements", String(alert.measurementCount)],
        ["Severity score", formatNumber(alert.severityScore, 3)],
        ["Window", `${formatDateTime(alert.windowStart)} – ${formatDateTime(alert.windowEnd)}`],
      ];

  return (
    <div className="drawer-backdrop" role="presentation" onMouseDown={(event) => {
      if (event.currentTarget === event.target) onClose();
    }}>
      <section className="alert-drawer" role="dialog" aria-modal="true" aria-labelledby="alert-detail-title">
        <div className="alert-drawer__header">
          <span className="eyebrow"><Activity size={13} aria-hidden="true" /> Alert detail</span>
          <button className="icon-button" onClick={onClose} aria-label="Close alert detail"><X size={18} /></button>
        </div>
        <div className="alert-drawer__title">
          <span className={`severity-pill severity-pill--${alert.severityLevel.toLowerCase()}`}>{alert.severityLevel}</span>
          <h2 id="alert-detail-title">{alertTitle(alert)}</h2>
          <p>{alertMessage(alert)}</p>
        </div>
        <div className="alert-drawer__location">
          <MapPin size={16} aria-hidden="true" />
          <span>{alert.region}{alert.kind === "fault" ? ` · ${alert.substation}` : ""}</span>
        </div>
        <dl className="detail-grid">
          <div><dt>Detected</dt><dd>{formatDateTime(alert.timestamp)}</dd></div>
          <div><dt>Alert ID</dt><dd className="mono">{alert.alertId}</dd></div>
          {details.map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value}</dd></div>)}
        </dl>
      </section>
    </div>
  );
}
