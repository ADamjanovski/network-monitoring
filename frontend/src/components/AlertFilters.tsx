import { Filter, RotateCcw } from "lucide-react";
import type { FormEvent } from "react";
import type { AlertFilters as FilterValues } from "../types";

const FAULT_TYPES = [
  ["VOLTAGE_SAG", "Voltage Sag"],
  ["VOLTAGE_SWELL", "Voltage Swell"],
  ["OVERCURRENT", "Overcurrent"],
] as const;

const FREQUENCY_TYPES = [
  ["FREQUENCY_DEVIATION", "Frequency Deviation"],
  ["HIGH_ROCOF", "High Rate of Change"],
  ["CRITICAL_ROCOF", "Critical Rate of Change"],
] as const;

const SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"] as const;

interface AlertFiltersProps {
  kind: "fault" | "frequency";
  values: FilterValues;
  busy: boolean;
  onChange: (values: FilterValues) => void;
  onApply: () => void;
  onReset: () => void;
}

export function AlertFilters({ kind, values, busy, onChange, onApply, onReset }: AlertFiltersProps) {
  const types = kind === "fault" ? FAULT_TYPES : FREQUENCY_TYPES;

  const update = (field: keyof FilterValues, value: string) => {
    onChange({ ...values, [field]: value });
  };

  const submit = (event: FormEvent) => {
    event.preventDefault();
    onApply();
  };

  return (
    <form className="filters" onSubmit={submit} aria-label={`${kind} alert filters`}>
      <label>
        <span>From</span>
        <input type="datetime-local" value={values.start} onChange={(event) => update("start", event.target.value)} />
      </label>
      <label>
        <span>To</span>
        <input type="datetime-local" value={values.end} onChange={(event) => update("end", event.target.value)} />
      </label>
      <label>
        <span>Region</span>
        <input value={values.region} onChange={(event) => update("region", event.target.value)} placeholder="Any region" />
      </label>
      {kind === "fault" ? (
        <>
          <label>
            <span>Substation</span>
            <input value={values.substation} onChange={(event) => update("substation", event.target.value)} placeholder="Any substation" />
          </label>
          <label>
            <span>Location</span>
            <input value={values.location} onChange={(event) => update("location", event.target.value)} placeholder="Any location" />
          </label>
          <label>
            <span>PMU ID</span>
            <input value={values.pmuId} onChange={(event) => update("pmuId", event.target.value)} placeholder="Any PMU" />
          </label>
        </>
      ) : null}
      <label>
        <span>Alert type</span>
        <select value={values.alertType} onChange={(event) => update("alertType", event.target.value)}>
          <option value="">All alert types</option>
          {types.map(([value, label]) => <option value={value} key={value}>{label}</option>)}
        </select>
      </label>
      <label>
        <span>Severity</span>
        <select value={values.severityLevel} onChange={(event) => update("severityLevel", event.target.value)}>
          <option value="">All severities</option>
          {SEVERITIES.map((severity) => (
            <option value={severity} key={severity}>{severity[0] + severity.slice(1).toLowerCase()}</option>
          ))}
        </select>
      </label>
      <div className="filters__actions">
        <button type="submit" className="button button--primary" disabled={busy}>
          <Filter size={15} aria-hidden="true" /> {busy ? "Loading…" : "Apply filters"}
        </button>
        <button type="button" className="button button--quiet" onClick={onReset} disabled={busy}>
          <RotateCcw size={14} aria-hidden="true" /> Reset
        </button>
      </div>
    </form>
  );
}
