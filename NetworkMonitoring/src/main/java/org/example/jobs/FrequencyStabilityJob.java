package org.example.jobs;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.example.utils.MeasurementDeserializer;
import org.example.models.Measurement;
import org.example.models.FrequencyAlert;
import org.example.windowsFunctions.FrequencyStabilityWindowFunction;
import org.example.models.configuration.AppConfig;

import java.time.Duration;

public class FrequencyStabilityJob {

    private static final String JOB_NAME                = "Frequency-Stability-Analysis";

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
                                .setTopic(AppConfig.FREQUENCY_ALERTS_TOPIC)
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        DataStream<String> rawStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "PMU-Frequency-Source"
        );

        DataStream<Measurement> measurementStream = rawStream
                .map(new MeasurementDeserializer())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<Measurement>forBoundedOutOfOrderness(Duration.ofMillis(100))
                                .withTimestampAssigner((m, ts) -> m.getTimestamp())
                                .withIdleness(Duration.ofSeconds(5))
                );

        
        DataStream<FrequencyAlert> alerts = measurementStream
                .keyBy(Measurement::getRegion)
                .window(SlidingEventTimeWindows.of(
                        Time.seconds(5), 
                        Time.seconds(1)
                ))
                .process(new FrequencyStabilityWindowFunction());

        alerts.print();
        alerts.map(FrequencyAlert::toJson)
              .sinkTo(sink);
        
        try {
            env.execute(JOB_NAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}