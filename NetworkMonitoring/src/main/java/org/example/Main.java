package org.example;

import com.google.gson.Gson;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.example.models.AnomalyResult;
import org.example.models.AnomalyTypes;
import org.example.models.Measurement;
import org.example.watermarkStrategies.MeasurementWatermarkStrategy;
import org.example.windowsFunctions.ElectricalFaultDetection;
import org.apache.flink.connector.base.DeliveryGuarantee;
// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("pmu-measurements")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        DataStream<Measurement> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source")
                .map(value -> new Gson().fromJson(value, Measurement.class))
                .assignTimestampsAndWatermarks(new MeasurementWatermarkStrategy());


        SingleOutputStreamOperator<String> result=stream.keyBy(Measurement::getKey)
                .window(SlidingEventTimeWindows.of(Time.seconds(2), Time.milliseconds(100)))
                .apply(new ElectricalFaultDetection())
                .map(AnomalyResult::toString);


        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("electricalFaultDetection")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        result.sinkTo(sink);

        env.execute("Flink Java API Skeleton");

    }
}