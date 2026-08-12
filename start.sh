#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$project_dir"

if [ ! -f .env ]; then
    echo "Missing .env. Copy .env.example to .env and add the PostgreSQL credentials."
    exit 1
fi

if [ ! -x venv/bin/python ]; then
    python3 -m venv venv
fi

venv/bin/python -m pip install -r requirements.txt
docker compose up -d --build

echo "Waiting for the Flink jobs to be submitted..."
while :; do
    flink_jobs_container=$(docker compose ps -a -q flink-jobs)

    if [ -z "$flink_jobs_container" ]; then
        echo "The Flink job submitter container was not created."
        exit 1
    fi

    flink_jobs_status=$(docker inspect --format '{{.State.Status}}' "$flink_jobs_container")
    if [ "$flink_jobs_status" = "exited" ]; then
        flink_jobs_exit_code=$(docker inspect --format '{{.State.ExitCode}}' "$flink_jobs_container")
        if [ "$flink_jobs_exit_code" -ne 0 ]; then
            docker compose logs flink-jobs
            exit "$flink_jobs_exit_code"
        fi
        break
    fi

    sleep 1
done

exec venv/bin/python pmu_producer.py
