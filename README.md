# Network Monitoring

Personal Apache Flink project for processing simulated PMU measurements, storing alerts and system metrics, querying them through Spring Boot, and streaming new results with Server-Sent Events (SSE).

## Prerequisites

- Docker with Docker Compose
- Python 3 with virtual-environment support

## Start the Project

### 1. Configure the database

```sh
cp .env.example .env
```

Set `POSTGRES_USER` and `POSTGRES_PASSWORD` in `.env`.

### 2. Start the complete pipeline

```sh
./start.sh
```

This builds and starts Kafka, PostgreSQL, the Flink cluster and all three Flink jobs, the backend, and the frontend. An idempotent initialization service creates the required Kafka topics before any consumers start. The Python PMU producer starts only after all three Flink jobs report the `RUNNING` state; startup exits with an error if they do not become ready within two minutes.

The dashboard is available at `http://localhost:5173`, the backend API at `http://localhost:8080`, and the Flink dashboard at `http://localhost:8081`. Stop the producer with `Ctrl+C`; the Docker services continue running until they are stopped separately.

```sh
docker compose logs -f flink-jobmanager flink-taskmanager backend frontend
docker compose down
```

Kafka topics are created automatically during startup. To inspect them:

```sh
docker exec kafka kafka-topics --bootstrap-server kafka:9093 --list
```

## REST API

All collection endpoints return newest records first. `start` and `end` are optional inclusive UTC Unix timestamps in milliseconds. Alert limits default to 200 and are capped at 1,000. The system-metrics limit defaults to 2,000 and is capped at 5,000.

### Fault alerts

```text
GET /api/fault-alert
GET /api/fault-alert/{alertId}
```

Optional filters: `start`, `end`, `region`, `substation`, `location`, `pmuId`, `alertType`, `severityLevel`, and `limit`.

```sh
curl "http://localhost:8080/api/fault-alert?region=Skopje&alertType=VOLTAGE_SAG&limit=50"
```

### Frequency alerts

```text
GET /api/frequency-alert
GET /api/frequency-alert/{alertId}
```

Optional filters: `start`, `end`, `region`, `alertType`, `severityLevel`, and `limit`.

Frequency disturbances are stored as system-wide incidents rather than one alert per region and sliding window. Each incident keeps one stable ID while moving through `START`, deduplicated `UPDATE`, `RECOVERY`, and `CLOSE` states; affected regions are included only when their robust frequency estimates show statistically significant disagreement from the system median.

```sh
curl "http://localhost:8080/api/frequency-alert?region=Skopje&severityLevel=HIGH&limit=50"
```

### System metrics

```text
GET /api/system-metrics
GET /api/system-metrics/latest
GET /api/system-metrics/{timestamp}
```

The collection accepts `start`, `end`, and `limit`. When both time bounds are supplied, results are evenly sampled across the complete period instead of returning only its newest records.

```sh
curl "http://localhost:8080/api/system-metrics/latest"
```

Unknown individual alert IDs and metrics timestamps return `404 Not Found`. The latest-metrics endpoint returns `204 No Content` until the first system metric has been stored.

## Live SSE Stream

Connect with any SSE-compatible HTTP client:

```sh
curl -N http://localhost:8080/api/events/stream
```

The stream sends three named event types after their records have been saved:

- `fault-alert`
- `frequency-alert`
- `system-metric`

Heartbeat comments are sent every 15 seconds to keep the connection active.

## Backend Configuration

The main settings are in `backend/src/main/resources/application.properties`:

```properties
spring.kafka.bootstrap-servers=localhost:9092
app.kafka.topics.system-metrics=system-metrics
app.kafka.topics.fault-alerts=fault-alerts
app.kafka.topics.frequency-alerts=frequency-alerts
app.sse.heartbeat-ms=15000
```

PostgreSQL credentials remain environment variables. Timestamp semantics and enum persistence are documented in `backend/README.md`.
