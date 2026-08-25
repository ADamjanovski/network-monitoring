import { ChevronRight, Waves } from "lucide-react";
import type { FaultAlert, FrequencyAlert } from "../types";
import { formatDateTime, formatNumber } from "../utils";

interface FaultAlertTableProps {
  alerts: FaultAlert[];
  loading: boolean;
  onSelect: (alert: FaultAlert) => void;
}

interface FrequencyAlertTableProps {
  alerts: FrequencyAlert[];
  loading: boolean;
  onSelect: (alert: FrequencyAlert) => void;
}

function EmptyTable({ loading }: { loading: boolean }) {
  return (
    <div className="table-empty">
      <Waves size={22} aria-hidden="true" />
      <strong>{loading ? "Loading alerts…" : "No alerts found"}</strong>
      <span>{loading ? "Reading the latest records." : "Try changing the selected filters."}</span>
    </div>
  );
}

export function FaultAlertTable({ alerts, loading, onSelect }: FaultAlertTableProps) {
  if (!alerts.length) return <EmptyTable loading={loading} />;

  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Detected</th>
            <th>Severity</th>
            <th>Alert type</th>
            <th>Region</th>
            <th>Substation / location</th>
            <th>PMU</th>
            <th className="numeric">Value / threshold</th>
            <th><span className="sr-only">Details</span></th>
          </tr>
        </thead>
        <tbody>
          {alerts.map((alert) => (
            <tr key={alert.alertId}>
              <td><time dateTime={new Date(alert.timestamp).toISOString()}>{formatDateTime(alert.timestamp)}</time></td>
              <td><span className={`severity-pill severity-pill--${alert.severityLevel.toLowerCase()}`}>{alert.severityLevel}</span></td>
              <td><strong className="table-primary">{alert.alertType}</strong></td>
              <td>{alert.region}</td>
              <td><span className="stacked-value"><strong>{alert.substation}</strong><small>{alert.location}</small></span></td>
              <td><span className="mono">{alert.pmuId}</span></td>
              <td className="numeric"><span className="stacked-value"><strong>{formatNumber(alert.measuredValue)}</strong><small>limit {formatNumber(alert.threshold)}</small></span></td>
              <td>
                <button className="row-action" onClick={() => onSelect(alert)} aria-label={`View ${alert.alertType} details`}>
                  <ChevronRight size={17} aria-hidden="true" />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function FrequencyAlertTable({ alerts, loading, onSelect }: FrequencyAlertTableProps) {
  if (!alerts.length) return <EmptyTable loading={loading} />;

  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Detected</th>
            <th>Severity</th>
            <th>State</th>
            <th>Alert type</th>
            <th>Region</th>
            <th className="numeric">Frequency</th>
            <th className="numeric">Deviation</th>
            <th className="numeric">RoCoF</th>
            <th>Message</th>
            <th><span className="sr-only">Details</span></th>
          </tr>
        </thead>
        <tbody>
          {alerts.map((alert) => (
            <tr key={alert.alertId}>
              <td><time dateTime={new Date(alert.timestamp).toISOString()}>{formatDateTime(alert.timestamp)}</time></td>
              <td><span className={`severity-pill severity-pill--${alert.severityLevel.toLowerCase()}`}>{alert.severityLevel}</span></td>
              <td><strong className="table-primary">{alert.incidentState ?? "LEGACY"}</strong></td>
              <td><strong className="table-primary">{alert.alertDisplayName}</strong></td>
              <td>{alert.region}</td>
              <td className="numeric"><span className="stacked-value"><strong>{formatNumber(alert.avgFrequency, 3)} Hz</strong><small>{formatNumber(alert.minFrequency, 3)}–{formatNumber(alert.maxFrequency, 3)}</small></span></td>
              <td className="numeric">{formatNumber(alert.frequencyDeviation, 3)} Hz</td>
              <td className="numeric">{formatNumber(alert.rocof, 3)} Hz/s</td>
              <td><span className="table-message">{alert.message}</span></td>
              <td>
                <button className="row-action" onClick={() => onSelect(alert)} aria-label={`View ${alert.alertDisplayName} details`}>
                  <ChevronRight size={17} aria-hidden="true" />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
