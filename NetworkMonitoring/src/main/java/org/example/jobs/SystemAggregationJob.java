package org.example.jobs;

import org.apache.flink.connector.base.DeliveryGuarantee;
import org.example.models.SystemMetrics;
import org.example.utils.MeasurementDeserializer;
import org.example.models.Measurement;
import org.example.models.configuration.AppConfig;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.example.windowsFunctions.SystemAggregationWindowFunction;

import java.time.Duration;

public class SystemAggregationJob {

    private static final String JOB_NAME                = "System-Wide-Aggregation";

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
//        env.enableCheckpointing(1000);
        System.out.println("Starting Flink Job: " + JOB_NAME);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(AppConfig.KAFKA_BOOTSTRAP_SERVERS)
                .setTopics(AppConfig.INPUT_TOPIC)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers(AppConfig.KAFKA_BOOTSTRAP_SERVERS)
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic(AppConfig.SYSTEM_METRICS_TOPIC)
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        DataStream<String> rawStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "PMU-Aggregation-Source"
        );

        DataStream<Measurement> measurementStream = rawStream
                .map(new MeasurementDeserializer())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<Measurement>forBoundedOutOfOrderness(Duration.ofMillis(100))
                                .withTimestampAssigner((m, ts) -> m.getTimestamp())
                                .withIdleness(Duration.ofSeconds(2))
                );

        
        DataStream<SystemMetrics> systemMetrics = measurementStream
                .keyBy(m -> "SYSTEM")
                .window(SlidingEventTimeWindows.of(
                        Time.milliseconds(1500),
                        Time.milliseconds(500)
                ))
                .process(new SystemAggregationWindowFunction());

        systemMetrics.print();
        systemMetrics.map(SystemMetrics::toJson)
                     .sinkTo(sink);

        env.execute(JOB_NAME);
    }
}