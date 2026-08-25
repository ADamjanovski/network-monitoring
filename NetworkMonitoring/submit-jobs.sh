#!/bin/sh
set -eu

jobmanager="flink-jobmanager:8081"
job_jar="/opt/flink/usrlib/network-monitoring.jar"
max_status_checks=${FLINK_JOB_STATUS_MAX_CHECKS:-60}
status_check_interval=${FLINK_JOB_STATUS_CHECK_INTERVAL_SECONDS:-2}

echo "Waiting for the Flink JobManager..."
until flink list -m "$jobmanager" >/dev/null 2>&1; do
    sleep 2
done

submit_if_missing() {
    job_class=$1
    job_name=$2

    if flink list -r -m "$jobmanager" 2>/dev/null | grep -Fq ": $job_name (RUNNING)"; then
        echo "$job_name is already running"
        return
    fi

    echo "Submitting $job_name"
    flink run -d -m "$jobmanager" -c "$job_class" "$job_jar"
}

all_jobs_are_running() {
    running_jobs=$1

    echo "$running_jobs" | grep -Fq ": Simple-Fault-Detection (RUNNING)" &&
        echo "$running_jobs" | grep -Fq ": Frequency-Stability-Analysis (RUNNING)" &&
        echo "$running_jobs" | grep -Fq ": System-Wide-Aggregation (RUNNING)"
}

submit_if_missing org.example.jobs.SimpleFaultDetectionJob Simple-Fault-Detection
submit_if_missing org.example.jobs.FrequencyStabilityJob Frequency-Stability-Analysis
submit_if_missing org.example.jobs.SystemAggregationJob System-Wide-Aggregation

echo "Waiting for all Flink jobs to reach RUNNING state..."
status_check=1
while [ "$status_check" -le "$max_status_checks" ]; do
    running_jobs=$(flink list -r -m "$jobmanager" 2>/dev/null || true)
    if all_jobs_are_running "$running_jobs"; then
        echo "All Flink jobs are running"
        exit 0
    fi

    status_check=$((status_check + 1))
    sleep "$status_check_interval"
done

echo "Flink jobs did not all reach RUNNING state within the configured timeout" >&2
flink list -a -m "$jobmanager" >&2 || true
exit 1
