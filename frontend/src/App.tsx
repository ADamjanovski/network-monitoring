import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Activity, AlertTriangle, Gauge, Radio, RefreshCw, Users, Waves, Zap } from "lucide-react";
import {
  faultAlert as parseFaultAlert,
  frequencyAlert as parseFrequencyAlert,
  getFaultAlerts,
  getFrequencyAlerts,
  getLatestMetric,
  getMetricHistory,
} from "./api";
import { AlertDetail } from "./components/AlertDetail";
import { AlertFilters } from "./components/AlertFilters";
import { AlertRail } from "./components/AlertRail";
import { FaultAlertTable, FrequencyAlertTable } from "./components/AlertTables";
import { MetricCard } from "./components/MetricCard";
import { MetricChart } from "./components/MetricChart";
import type {
  Alert,
  AlertFilters as FilterValues,
  FaultAlert,
  FrequencyAlert,
  RawFaultAlert,
  RawFrequencyAlert,
  RawSystemMetric,
  SystemMetric,
} from "./types";
import { formatDateTime, formatNumber } from "./utils";

const RANGE_OPTIONS = [
  { label: "15m", value: 15 * 60 * 1000 },
  { label: "1h", value: 60 * 60 * 1000 },
  { label: "6h", value: 6 * 60 * 60 * 1000 },
  { label: "24h", value: 24 * 60 * 60 * 1000 },
] as const;

const MAX_CHART_POINTS = 2_000;

const EMPTY_FILTERS: FilterValues = {
  start: "",
  end: "",
  region: "",
  substation: "",
  location: "",
  pmuId: "",
  alertType: "",
  severityLevel: "",
};

const FAULT_TYPE_LABELS: Record<string, string> = {
  VOLTAGE_SAG: "Voltage Sag",
  VOLTAGE_SWELL: "Voltage Swell",
  OVERCURRENT: "Overcurrent",
};

const FREQUENCY_TYPE_LABELS: Record<string, string> = {
  FREQUENCY_DEVIATION: "Frequency Deviation",
  HIGH_ROCOF: "High Rate of Change of Frequency",
  CRITICAL_ROCOF: "Critical Rate of Change of Frequency",
};

type ConnectionState = "connecting" | "live" | "disconnected";
type AlertTab = "fault" | "frequency";

function cloneEmptyFilters(): FilterValues {
  return { ...EMPTY_FILTERS };
}

function isWithinTime(timestamp: number, filters: FilterValues): boolean {
  if (filters.start && timestamp < new Date(filters.start).getTime()) return false;
  if (filters.end && timestamp > new Date(filters.end).getTime()) return false;
  return true;
}

function matchesFaultFilters(alert: FaultAlert, filters: FilterValues): boolean {
  return isWithinTime(alert.timestamp, filters)
    && (!filters.region || alert.region === filters.region)
    && (!filters.substation || alert.substation === filters.substation)
    && (!filters.location || alert.location === filters.location)
    && (!filters.pmuId || alert.pmuId === filters.pmuId)
    && (!filters.alertType || alert.alertType === FAULT_TYPE_LABELS[filters.alertType])
    && (!filters.severityLevel || alert.severityLevel.toUpperCase() === filters.severityLevel);
}

function matchesFrequencyFilters(alert: FrequencyAlert, filters: FilterValues): boolean {
  return isWithinTime(alert.timestamp, filters)
    && (!filters.region || alert.region === filters.region)
    && (!filters.alertType || alert.alertDisplayName === FREQUENCY_TYPE_LABELS[filters.alertType])
    && (!filters.severityLevel || alert.severityLevel.toUpperCase() === filters.severityLevel);
}

function normalizeStreamMetric(raw: RawSystemMetric): SystemMetric {
  return {
    timestamp: raw.timestamp,
    windowStart: raw.windowStart,
    windowEnd: raw.windowEnd,
    activePmuCount: raw.activePmuCount,
    avgFrequency: raw.avgFrequency,
    minFrequency: raw.minFrequency,
    maxFrequency: raw.maxFrequency,
    avgVoltage: raw.avgVoltage,
    minVoltage: raw.minVoltage,
    maxVoltage: raw.maxVoltage,
    avgCurrent: raw.avgCurrent,
    minCurrent: raw.minCurrent,
    maxCurrent: raw.maxCurrent,
  };
}

function evenlyLimitChartPoints(points: SystemMetric[]): SystemMetric[] {
  if (points.length <= MAX_CHART_POINTS) return points;
  const step = (points.length - 1) / (MAX_CHART_POINTS - 1);
  return Array.from(
    { length: MAX_CHART_POINTS },
    (_, index) => points[Math.round(index * step)],
  );
}

export default function App() {
  const [latestMetric, setLatestMetric] = useState<SystemMetric | null>(null);
  const [metricHistory, setMetricHistory] = useState<SystemMetric[]>([]);
  const [rangeMs, setRangeMs] = useState<number>(RANGE_OPTIONS[1].value);
  const [metricsLoading, setMetricsLoading] = useState(true);
  const [faultAlerts, setFaultAlerts] = useState<FaultAlert[]>([]);
  const [frequencyAlerts, setFrequencyAlerts] = useState<FrequencyAlert[]>([]);
  const [faultFilters, setFaultFilters] = useState<FilterValues>(cloneEmptyFilters);
  const [frequencyFilters, setFrequencyFilters] = useState<FilterValues>(cloneEmptyFilters);
  const [faultLoading, setFaultLoading] = useState(true);
  const [frequencyLoading, setFrequencyLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<AlertTab>("fault");
  const [notifications, setNotifications] = useState<Alert[]>([]);
  const [selectedAlert, setSelectedAlert] = useState<Alert | null>(null);
  const [connection, setConnection] = useState<ConnectionState>("connecting");
  const [dataError, setDataError] = useState<string | null>(null);

  const rangeRef = useRef(rangeMs);
  const appliedFaultFilters = useRef<FilterValues>(cloneEmptyFilters());
  const appliedFrequencyFilters = useRef<FilterValues>(cloneEmptyFilters());

  useEffect(() => { rangeRef.current = rangeMs; }, [rangeMs]);

  const loadMetrics = useCallback(async (selectedRange = rangeRef.current, signal?: AbortSignal) => {
    setMetricsLoading(true);
    const end = Date.now();
    try {
      const [latest, history] = await Promise.all([
        getLatestMetric(signal),
        getMetricHistory(end - selectedRange, end, signal),
      ]);
      setLatestMetric(latest);
      setMetricHistory(history);
      setDataError(null);
    } catch (error) {
      if (error instanceof DOMException && error.name === "AbortError") return;
      setDataError("The dashboard could not read system metrics from the backend.");
    } finally {
      setMetricsLoading(false);
    }
  }, []);

  const loadFaultData = useCallback(async (filters: FilterValues, signal?: AbortSignal) => {
    setFaultLoading(true);
    try {
      setFaultAlerts(await getFaultAlerts(filters, signal));
      setDataError(null);
    } catch (error) {
      if (error instanceof DOMException && error.name === "AbortError") return;
      setDataError("The dashboard could not read fault alerts from the backend.");
    } finally {
      setFaultLoading(false);
    }
  }, []);

  const loadFrequencyData = useCallback(async (filters: FilterValues, signal?: AbortSignal) => {
    setFrequencyLoading(true);
    try {
      setFrequencyAlerts(await getFrequencyAlerts(filters, signal));
      setDataError(null);
    } catch (error) {
      if (error instanceof DOMException && error.name === "AbortError") return;
      setDataError("The dashboard could not read frequency alerts from the backend.");
    } finally {
      setFrequencyLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    void loadMetrics(rangeMs, controller.signal);
    return () => controller.abort();
  }, [loadMetrics, rangeMs]);

  useEffect(() => {
    const controller = new AbortController();
    void loadFaultData(appliedFaultFilters.current, controller.signal);
    void loadFrequencyData(appliedFrequencyFilters.current, controller.signal);
    return () => controller.abort();
  }, [loadFaultData, loadFrequencyData]);

  useEffect(() => {
    const stream = new EventSource("/api/events/stream");
    let openedBefore = false;

    stream.onopen = () => {
      setConnection("live");
      if (openedBefore) {
        void loadMetrics(rangeRef.current);
        void loadFaultData(appliedFaultFilters.current);
        void loadFrequencyData(appliedFrequencyFilters.current);
      }
      openedBefore = true;
    };

    stream.onerror = () => setConnection("disconnected");

    stream.addEventListener("system-metric", (event) => {
      try {
        const metric = normalizeStreamMetric(JSON.parse(event.data) as RawSystemMetric);
        setLatestMetric(metric);
        setMetricHistory((previous) => {
          const cutoff = metric.timestamp - rangeRef.current;
          const withoutDuplicate = previous.filter((item) => item.timestamp !== metric.timestamp && item.timestamp >= cutoff);
          const ordered = [...withoutDuplicate, metric].sort((a, b) => a.timestamp - b.timestamp);
          return evenlyLimitChartPoints(ordered);
        });
      } catch {
        setDataError("A live system-metric event could not be read.");
      }
    });

    stream.addEventListener("fault-alert", (event) => {
      try {
        const alert = parseFaultAlert(JSON.parse(event.data) as RawFaultAlert);
        if (alert.severityLevel === "Critical") {
          setNotifications((previous) => [alert, ...previous.filter((item) => item.alertId !== alert.alertId)].slice(0, 12));
        }
        if (matchesFaultFilters(alert, appliedFaultFilters.current)) {
          setFaultAlerts((previous) => [alert, ...previous.filter((item) => item.alertId !== alert.alertId)].slice(0, 200));
        }
      } catch {
        setDataError("A live fault-alert event could not be read.");
      }
    });

    stream.addEventListener("frequency-alert", (event) => {
      try {
        const alert = parseFrequencyAlert(JSON.parse(event.data) as RawFrequencyAlert);
        if (alert.severityLevel === "Critical") {
          setNotifications((previous) => [alert, ...previous.filter((item) => item.alertId !== alert.alertId)].slice(0, 12));
        }
        if (matchesFrequencyFilters(alert, appliedFrequencyFilters.current)) {
          setFrequencyAlerts((previous) => [alert, ...previous.filter((item) => item.alertId !== alert.alertId)].slice(0, 200));
        }
      } catch {
        setDataError("A live frequency-alert event could not be read.");
      }
    });

    return () => stream.close();
  }, [loadFaultData, loadFrequencyData, loadMetrics]);

  const frequencyChange = useMemo(() => {
    if (!latestMetric || !metricHistory.length) return null;
    return latestMetric.avgFrequency - metricHistory[0].avgFrequency;
  }, [latestMetric, metricHistory]);

  const voltageChange = useMemo(() => {
    if (!latestMetric || !metricHistory.length) return null;
    return latestMetric.avgVoltage - metricHistory[0].avgVoltage;
  }, [latestMetric, metricHistory]);

  const applyFaultFilters = () => {
    appliedFaultFilters.current = { ...faultFilters };
    void loadFaultData(appliedFaultFilters.current);
  };

  const resetFaultFilters = () => {
    const empty = cloneEmptyFilters();
    setFaultFilters(empty);
    appliedFaultFilters.current = empty;
    void loadFaultData(empty);
  };

  const applyFrequencyFilters = () => {
    appliedFrequencyFilters.current = { ...frequencyFilters };
    void loadFrequencyData(appliedFrequencyFilters.current);
  };

  const resetFrequencyFilters = () => {
    const empty = cloneEmptyFilters();
    setFrequencyFilters(empty);
    appliedFrequencyFilters.current = empty;
    void loadFrequencyData(empty);
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="Grid Monitor dashboard home">
          <span className="brand__mark"><Zap size={19} aria-hidden="true" /></span>
          <span><strong>Grid Monitor</strong><small>Network observability</small></span>
        </a>
        <div className="topbar__status">
          <span className={`connection connection--${connection}`}>
            <span className="connection__dot" aria-hidden="true" />
            {connection === "live" ? "Live" : connection === "connecting" ? "Connecting" : "Reconnecting"}
          </span>
          <span className="last-update">
            Last update <strong>{latestMetric ? formatDateTime(latestMetric.timestamp) : "Waiting for data"}</strong>
          </span>
        </div>
      </header>

      <main id="top">
        {dataError ? (
          <div className="error-banner" role="alert">
            <AlertTriangle size={17} aria-hidden="true" />
            <span>{dataError}</span>
            <button onClick={() => {
              setDataError(null);
              void loadMetrics();
              void loadFaultData(appliedFaultFilters.current);
              void loadFrequencyData(appliedFrequencyFilters.current);
            }}><RefreshCw size={14} aria-hidden="true" /> Retry</button>
          </div>
        ) : null}

        <div className="dashboard-layout">
          <div className="dashboard-main">
            <section className="overview-section" aria-labelledby="overview-title">
              <div className="section-heading">
                <div>
                  <span className="eyebrow"><Radio size={13} aria-hidden="true" /> System overview</span>
                  <h1 id="overview-title">Grid performance</h1>
                  <p>Live operating measurements from all active phasor units.</p>
                </div>
                <div className="range-selector" aria-label="Chart time range">
                  {RANGE_OPTIONS.map((option) => (
                    <button
                      key={option.label}
                      className={rangeMs === option.value ? "active" : ""}
                      onClick={() => setRangeMs(option.value)}
                      aria-pressed={rangeMs === option.value}
                    >
                      {option.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className={`metric-grid ${metricsLoading ? "is-loading" : ""}`} aria-busy={metricsLoading}>
                <MetricCard
                  label="Average frequency"
                  value={latestMetric ? formatNumber(latestMetric.avgFrequency, 3) : "—"}
                  unit="Hz"
                  detail="Across the selected period"
                  icon={<Waves size={19} />}
                  accent="cyan"
                  change={frequencyChange}
                  changeUnit=" Hz"
                />
                <MetricCard
                  label="Average voltage"
                  value={latestMetric ? formatNumber(latestMetric.avgVoltage, 1) : "—"}
                  unit="V"
                  detail="Across the selected period"
                  icon={<Gauge size={19} />}
                  accent="amber"
                  change={voltageChange}
                  changeUnit=" V"
                />
                <MetricCard
                  label="Active PMUs"
                  value={latestMetric ? String(latestMetric.activePmuCount) : "—"}
                  detail="Reporting in the latest window"
                  icon={<Users size={19} />}
                  accent="violet"
                />
              </div>

              <div className="chart-grid">
                <MetricChart
                  title="Frequency"
                  description="Average system frequency"
                  data={metricHistory}
                  dataKey="avgFrequency"
                  rangeMs={rangeMs}
                  unit="Hz"
                  color="#45d7e8"
                  gradientId="frequencyFill"
                  digits={3}
                />
                <MetricChart
                  title="Voltage"
                  description="Average system voltage"
                  data={metricHistory}
                  dataKey="avgVoltage"
                  rangeMs={rangeMs}
                  unit="V"
                  color="#f2b84b"
                  gradientId="voltageFill"
                  digits={1}
                />
              </div>
            </section>
          </div>

          <AlertRail
            alerts={notifications}
            onDismiss={(alertId) => setNotifications((items) => items.filter((item) => item.alertId !== alertId))}
            onSelect={setSelectedAlert}
          />
        </div>

        <section className="history-section" aria-labelledby="alert-history-title">
          <div className="history-section__header">
            <div>
              <span className="eyebrow"><Activity size={13} aria-hidden="true" /> Event history</span>
              <h2 id="alert-history-title">Alert history</h2>
              <p>Review detected conditions and narrow the results by grid location or classification.</p>
            </div>
            <div className="tab-list" role="tablist" aria-label="Alert category">
              <button
                role="tab"
                aria-selected={activeTab === "fault"}
                className={activeTab === "fault" ? "active" : ""}
                onClick={() => setActiveTab("fault")}
              >
                Fault alerts <span>{faultAlerts.length}</span>
              </button>
              <button
                role="tab"
                aria-selected={activeTab === "frequency"}
                className={activeTab === "frequency" ? "active" : ""}
                onClick={() => setActiveTab("frequency")}
              >
                Frequency alerts <span>{frequencyAlerts.length}</span>
              </button>
            </div>
          </div>

          <div className="history-panel" role="tabpanel">
            {activeTab === "fault" ? (
              <>
                <AlertFilters
                  kind="fault"
                  values={faultFilters}
                  busy={faultLoading}
                  onChange={setFaultFilters}
                  onApply={applyFaultFilters}
                  onReset={resetFaultFilters}
                />
                <FaultAlertTable alerts={faultAlerts} loading={faultLoading} onSelect={setSelectedAlert} />
              </>
            ) : (
              <>
                <AlertFilters
                  kind="frequency"
                  values={frequencyFilters}
                  busy={frequencyLoading}
                  onChange={setFrequencyFilters}
                  onApply={applyFrequencyFilters}
                  onReset={resetFrequencyFilters}
                />
                <FrequencyAlertTable alerts={frequencyAlerts} loading={frequencyLoading} onSelect={setSelectedAlert} />
              </>
            )}
          </div>
        </section>
      </main>

      <footer>
        <span><Zap size={13} aria-hidden="true" /> Grid Monitor</span>
        <span>Times shown in your local timezone</span>
      </footer>

      <AlertDetail alert={selectedAlert} onClose={() => setSelectedAlert(null)} />
    </div>
  );
}
