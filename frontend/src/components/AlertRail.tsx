import { Activity, BellRing, Radio, X } from "lucide-react";
import type { Alert } from "../types";
import { alertMessage, alertTitle, formatDateTime } from "../utils";

interface AlertRailProps {
  alerts: Alert[];
  onDismiss: (alertId: string) => void;
  onSelect: (alert: Alert) => void;
}

export function AlertRail({ alerts, onDismiss, onSelect }: AlertRailProps) {
  return (
    <aside className="alert-rail" aria-labelledby="live-alerts-title">
      <div className="alert-rail__header">
        <div>
          <span className="eyebrow"><Radio size={13} aria-hidden="true" /> Live feed</span>
          <h2 id="live-alerts-title">Critical alerts</h2>
        </div>
        <span className="alert-count" aria-label={`${alerts.length} critical alerts`}>{alerts.length}</span>
      </div>

      <div className="alert-rail__list" aria-live="polite">
        {alerts.length ? alerts.map((alert) => (
          <article
            className={`live-alert severity-border--${alert.severityLevel.toLowerCase()}`}
            key={`${alert.kind}-${alert.alertId}`}
          >
            <button className="live-alert__body" onClick={() => onSelect(alert)}>
              <span className={`severity-mark severity-mark--${alert.severityLevel.toLowerCase()}`}>
                {alert.kind === "fault" ? <BellRing size={15} /> : <Activity size={15} />}
              </span>
              <span className="live-alert__content">
                <span className="live-alert__meta">
                  <strong>{alert.severityLevel}</strong>
                  <time dateTime={new Date(alert.timestamp).toISOString()}>{formatDateTime(alert.timestamp)}</time>
                </span>
                <span className="live-alert__title">{alertTitle(alert)}</span>
                <span className="live-alert__message">{alertMessage(alert)}</span>
                <span className="live-alert__region">{alert.region}{alert.kind === "fault" ? ` · ${alert.pmuId}` : ""}</span>
              </span>
            </button>
            <button
              className="icon-button live-alert__dismiss"
              onClick={() => onDismiss(alert.alertId)}
              aria-label={`Dismiss ${alertTitle(alert)} notification`}
            >
              <X size={15} aria-hidden="true" />
            </button>
          </article>
        )) : (
          <div className="alert-rail__empty">
            <span className="signal-rings" aria-hidden="true"><Radio size={19} /></span>
            <strong>Listening for critical alerts</strong>
            <p>Critical fault and frequency alerts will appear here.</p>
          </div>
        )}
      </div>
    </aside>
  );
}
