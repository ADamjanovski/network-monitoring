# Backend Contract

## Timestamp Convention

Every backend timestamp is a UTC Unix epoch value in **milliseconds**.

- A fault alert's `timestamp` is the source PMU measurement time.
- A frequency alert's `timestamp` is the end of its Flink event-time window.
- A system metric's `timestamp` is the end of its Flink event-time window.
- `window_start` is inclusive and `window_end` is exclusive, matching Flink time-window semantics.
- REST `start` and `end` filters apply to the entity's `timestamp`; the proposed range is inclusive at both ends.

Alert type and severity are Java enums in the persistence model and are stored in PostgreSQL using their stable enum names. Kafka and REST DTOs continue to use the existing human-readable display names defined by the Flink JSON contract.

Existing development rows written before this change contain display names rather than enum names. They must be migrated or the local database volume must be recreated before those rows can be read through the enum-backed entities.
