import type {
  AlertFilters,
  FaultAlert,
  FrequencyAlert,
  RawFaultAlert,
  RawFrequencyAlert,
  RawSystemMetric,
  SystemMetric,
} from "./types";

function systemMetric(raw: RawSystemMetric): SystemMetric {
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

export function faultAlert(raw: RawFaultAlert): FaultAlert {
  return {
    kind: "fault",
    alertId: raw.alertId,
    timestamp: raw.timestamp,
    pmuId: raw.pmuId,
    region: raw.region,
    substation: raw.substation,
    location: raw.location,
    alertType: raw.alertType,
    description: raw.description,
    measuredValue: raw.measuredValue,
    threshold: raw.threshold,
    severity: raw.severity,
    severityLevel: raw.severityLevel,
    voltage: raw.voltage,
    current: raw.current,
    frequency: raw.frequency,
  };
}

export function frequencyAlert(raw: RawFrequencyAlert): FrequencyAlert {
  return {
    kind: "frequency",
    alertId: raw.alertId,
    timestamp: raw.timestamp,
    windowStart: raw.windowStart,
    windowEnd: raw.windowEnd,
    region: raw.region,
    avgFrequency: raw.avgFrequency,
    minFrequency: raw.minFrequency,
    maxFrequency: raw.maxFrequency,
    frequencyDeviation: raw.frequencyDeviation,
    rocof: raw.rocof,
    rocofVolatility: raw.rocofVolatility,
    alertDisplayName: raw.alertDisplayName,
    alertDescription: raw.alertDescription,
    message: raw.message,
    severityLevel: raw.severityLevel,
    severityScore: raw.severityScore,
    measurementCount: raw.measurementCount,
  };
}

async function request<T>(url: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(url, { signal });
  if (!response.ok) {
    throw new Error(`Request failed (${response.status})`);
  }
  return response.json() as Promise<T>;
}

function appendFilters(params: URLSearchParams, filters: AlertFilters, includeFaultFields: boolean) {
  const fields: Array<[string, string]> = [
    ["region", filters.region],
    ["alertType", filters.alertType],
    ["severityLevel", filters.severityLevel],
  ];

  if (includeFaultFields) {
    fields.push(
      ["substation", filters.substation],
      ["location", filters.location],
      ["pmuId", filters.pmuId],
    );
  }

  fields.forEach(([key, value]) => {
    if (value.trim()) params.set(key, value.trim());
  });

  if (filters.start) params.set("start", String(new Date(filters.start).getTime()));
  if (filters.end) params.set("end", String(new Date(filters.end).getTime()));
}

export async function getLatestMetric(signal?: AbortSignal): Promise<SystemMetric | null> {
  const response = await fetch("/api/system-metrics/latest", { signal });
  if (response.status === 204) return null;
  if (!response.ok) {
    throw new Error(`Request failed (${response.status})`);
  }
  return systemMetric(await response.json() as RawSystemMetric);
}

export async function getMetricHistory(
  start: number,
  end: number,
  signal?: AbortSignal,
): Promise<SystemMetric[]> {
  const raw = await request<RawSystemMetric[]>(
    `/api/system-metrics?start=${start}&end=${end}&limit=2000`,
    signal,
  );
  return raw.map(systemMetric).sort((a, b) => a.timestamp - b.timestamp);
}

export async function getFaultAlerts(
  filters: AlertFilters,
  signal?: AbortSignal,
): Promise<FaultAlert[]> {
  const params = new URLSearchParams({ limit: "200" });
  appendFilters(params, filters, true);
  const raw = await request<RawFaultAlert[]>(`/api/fault-alert?${params}`, signal);
  return raw.map(faultAlert);
}

export async function getFrequencyAlerts(
  filters: AlertFilters,
  signal?: AbortSignal,
): Promise<FrequencyAlert[]> {
  const params = new URLSearchParams({ limit: "200" });
  appendFilters(params, filters, false);
  const raw = await request<RawFrequencyAlert[]>(`/api/frequency-alert?${params}`, signal);
  return raw.map(frequencyAlert);
}
