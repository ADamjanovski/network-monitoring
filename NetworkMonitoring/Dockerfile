FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests

FROM flink:1.20.0-scala_2.12-java17

COPY --from=build /build/target/NetworkMonitoring-1.0-SNAPSHOT.jar /opt/flink/usrlib/network-monitoring.jar
COPY submit-jobs.sh /opt/flink/bin/submit-jobs.sh

USER root
RUN chmod +x /opt/flink/bin/submit-jobs.sh
USER flink
