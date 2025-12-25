package org.example.watermarkStrategies;

import org.apache.flink.api.common.eventtime.*;
import org.example.models.Measurement;

public class MeasurementWatermarkGenerator implements WatermarkGenerator<Measurement> {

    @Override
    public void onEvent(Measurement measurement, long l, WatermarkOutput output) {
        output.emitWatermark(new Watermark(measurement.getTimestamp()));
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput output) {
        output.emitWatermark(new Watermark(System.currentTimeMillis()));
    }
}