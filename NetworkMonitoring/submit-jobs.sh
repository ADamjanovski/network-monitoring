#!/bin/sh
set -eu

jobmanager="flink-jobmanager:8081"
job_jar="/opt/flink/usrlib/network-monitoring.jar"

echo "Waiting for the Flink JobManager..."
until flink list -m "$jobmanager" >/dev/null 2>&1; do
    sleep 2
done

submit_if_missing() {
    job_class=$1
    job_name=$2

    if flink list -r -m "$jobmanager" 2>/dev/null | grep -Fq "$job_name"; then
        echo "$job_name is already running"
        return
    fi

    echo "Submitting $job_name"
    flink run -d -m "$jobmanager" -c "$job_class" "$job_jar"
}

submit_if_missing org.example.jobs.SimpleFaultDetectionJob Simple-Fault-Detection
submit_if_missing org.example.jobs.FrequencyStabilityJob Frequency-Stability-Analysis
submit_if_missing org.example.jobs.SystemAggregationJob System-Wide-Aggregation

echo "All Flink jobs are running"
