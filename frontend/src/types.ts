export type Severity = "Low" | "Medium" | "High" | "Critical";

export interface SystemMetric {
  timestamp: number;
  windowStart: number;
  windowEnd: number;
  activePmuCount: number;
  avgFrequency: number;
  minFrequency: number;
  maxFrequency: number;
  avgVoltage: number;
  minVoltage: number;
  maxVoltage: number;
  avgCurrent: number;
  minCurrent: number;
  maxCurrent: number;
}

export interface FaultAlert {
  kind: "fault";
  alertId: string;
  timestamp: number;
  pmuId: string;
  region: string;
  substation: string;
  location: string;
  alertType: string;
  description: string;
  measuredValue: number;
  threshold: number;
  severity: number;
  severityLevel: Severity;
  voltage: number;
  current: number;
  frequency: number;
}

export interface FrequencyAlert {
  kind: "frequency";
  alertId: string;
  timestamp: number;
  windowStart: number;
  windowEnd: number;
  region: string;
  avgFrequency: number;
  minFrequency: number;
  maxFrequency: number;
  frequencyDeviation: number;
  rocof: number;
  rocofVolatility: number;
  alertDisplayName: string;
  alertDescription: string;
  message: string;
  severityLevel: Severity;
  severityScore: number;
  measurementCount: number;
}

export type Alert = FaultAlert | FrequencyAlert;

export interface AlertFilters {
  start: string;
  end: string;
  region: string;
  substation: string;
  location: string;
  pmuId: string;
  alertType: string;
  severityLevel: string;
}

export interface RawSystemMetric {
  timestamp: number;
  windowStart: number;
  windowEnd: number;
  activePmuCount: number;
  avgFrequency: number;
  minFrequency: number;
  maxFrequency: number;
  avgVoltage: number;
  minVoltage: number;
  maxVoltage: number;
  avgCurrent: number;
  minCurrent: number;
  maxCurrent: number;
}

export interface RawFaultAlert {
  alertId: string;
  timestamp: number;
  pmuId: string;
  region: string;
  substation: string;
  location: string;
  alertType: string;
  description: string;
  measuredValue: number;
  threshold: number;
  severity: number;
  severityLevel: Severity;
  voltage: number;
  current: number;
  frequency: number;
}

export interface RawFrequencyAlert {
  alertId: string;
  timestamp: number;
  windowStart: number;
  windowEnd: number;
  region: string;
  avgFrequency: number;
  minFrequency: number;
  maxFrequency: number;
  frequencyDeviation: number;
  rocof: number;
  rocofVolatility: number;
  alertDisplayName: string;
  alertDescription: string;
  message: string;
  severityLevel: Severity;
  severityScore: number;
  measurementCount: number;
}
