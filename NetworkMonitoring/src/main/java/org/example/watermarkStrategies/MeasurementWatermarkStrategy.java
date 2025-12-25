package org.example.watermarkStrategies;

import org.apache.flink.api.common.eventtime.*;
import org.example.models.Measurement;

public class MeasurementWatermarkStrategy implements WatermarkStrategy<Measurement> {
    @Override
    public WatermarkGenerator<Measurement> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new MeasurementWatermarkGenerator();
        };

    @Override
    public TimestampAssigner<Measurement> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return (measurement, recordTimestamp) -> measurement.getTimestamp();
    }
}
