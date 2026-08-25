# Network Monitoring

Personal Apache Flink project for processing simulated PMU measurements, storing alerts and system metrics, querying them through Spring Boot, and streaming new results with Server-Sent Events (SSE).

For a simple explanation of the electrical domain, architecture, alert logic, and current limits, read the [project documentation](documentation.md).

## Run It on Another Machine

The application runs locally with Docker Compose. Only the Python measurement producer runs directly on the host machine.

### 1. Check the required software

Install:

- Git.
- Docker Engine or Docker Desktop with Docker Compose v2.
- Python 3.9 or newer with `venv` support.

The script uses a Unix-style shell. On Windows, run these commands from WSL2.

Check that the commands are available:

```sh
git --version
docker --version
docker compose version
python3 --version
python3 -m venv --help
```

The following host ports must be free:

| Port | Used by |
|---:|---|
| 5173 | Web dashboard |
| 8080 | Backend API |
| 8081 | Flink dashboard |
| 9092 | Kafka producer connection |
| 2345 | PostgreSQL development connection |

### 2. Clone the repository

```sh
git clone https://github.com/ADamjanovski/network-monitoring.git
cd network-monitoring
```

### 3. Configure the local database

Copy the example file:

```sh
cp .env.example .env
```

Open `.env` and set both values. For example:

```dotenv
POSTGRES_USER=network_monitor
POSTGRES_PASSWORD=choose-a-local-password
```

Do not commit `.env`. It is ignored by Git.

### 4. Start the complete pipeline

```sh
./start.sh
```

On the first run, the script may need several minutes to download images and build the services. It performs these steps:

1. Creates a local Python virtual environment when one does not exist.
2. Installs the Python Kafka package.
3. Builds and starts Kafka, PostgreSQL, Flink, the backend, and the frontend.
4. Creates the four Kafka topics.
5. Submits all three Flink jobs and waits until they report `RUNNING`.
6. Starts the Python measurement producer in the current terminal.

Keep this terminal open while the producer is running. Startup stops with an error if the required Kafka topics or Flink jobs are not ready.

### 5. Open and check the application

| Page | URL |
|---|---|
| Main dashboard | <http://localhost:5173> |
| Backend API | <http://localhost:8080> |
| Flink dashboard | <http://localhost:8081> |

Check the containers:

```sh
docker compose ps -a
```

The `kafka-init` and `flink-jobs` services are setup tasks. It is normal for them to show `Exited (0)` after successful startup.

Check that the backend has received a system metric:

```sh
curl -i http://localhost:8080/api/system-metrics/latest
```

It can briefly return `204 No Content` during startup. Run it again after measurements begin. A working pipeline returns `200 OK` with JSON data.

List the Kafka topics:

```sh
docker exec kafka kafka-topics --bootstrap-server kafka:9093 --list
```

### 6. Stop or restart the application

Press `Ctrl+C` in the terminal running `start.sh` to stop the Python producer. Then stop the Docker services:

```sh
docker compose down
```

This keeps the PostgreSQL volume, so stored data remains available on the next run. Start the project again with:

```sh
./start.sh
```

To remove the containers **and permanently delete the stored database data**, run:

```sh
docker compose down -v
```

### Common startup problems

- **Docker command fails:** start Docker Engine or Docker Desktop, then run `docker compose version` again.
- **`venv` creation fails:** install the Python virtual-environment package for your operating system.
- **A port is already in use:** stop the other program or change the matching port in `docker-compose.yml`.
- **A service does not become ready:** inspect the logs with the command below.
- **`start.sh` is not executable:** run `chmod +x start.sh`.

```sh
docker compose logs --tail=200 kafka flink-jobmanager flink-taskmanager flink-jobs backend frontend
```

After fixing a startup problem, stop partial services with `docker compose down` and run `./start.sh` again.

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

Frequency disturbances are stored as system-wide incidents rather than one alert per region and sliding window. Each incident keeps one stable ID while moving through `START`, deduplicated `UPDATE`, `RECOVERY`, and `CLOSE` states. A region is listed only when its frequency difference is too large to be explained by normal measurement noise.

```sh
curl "http://localhost:8080/api/frequency-alert?region=System&severityLevel=HIGH&limit=50"
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
