package org.example.windowsFunctions;

import org.example.models.Measurement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FrequencyStabilityWindowFunctionTest {

    @Test
    public void aggregatesEachTimestampUsingSpatialMedian() {
        List<Measurement> measurements = Arrays.asList(
                measurement(1000L, 49.98),
                measurement(1000L, 50.00),
                measurement(1000L, 50.01),
                measurement(1000L, 50.02),
                measurement(1000L, 55.00),
                measurement(2000L, 49.90),
                measurement(2000L, 49.91),
                measurement(2000L, 49.92)
        );

        List<FrequencyStabilityWindowFunction.ReportingFrame> frames =
                FrequencyStabilityWindowFunction.aggregateReportingFrames(measurements);

        assertEquals(2, frames.size());
        assertEquals(1000L, frames.get(0).timestamp);
        assertEquals(50.01, frames.get(0).frequency, 0.000001);
        assertEquals(2000L, frames.get(1).timestamp);
        assertEquals(49.91, frames.get(1).frequency, 0.000001);
    }

    @Test
    public void calculatesRocofBetweenReportingFrames() {
        List<FrequencyStabilityWindowFunction.ReportingFrame> frames = Arrays.asList(
                frame(1000L, 50.0),
                frame(2000L, 49.5),
                frame(3000L, 49.0)
        );

        double rocof = FrequencyStabilityWindowFunction.calculateRoCoF(frames);
        double volatility = FrequencyStabilityWindowFunction.calculateRoCoFVolatility(frames, rocof);

        assertEquals(-0.5, rocof, 0.000001);
        assertEquals(0.0, volatility, 0.000001);
    }

    @Test
    public void simultaneousPmuNoiseDoesNotBecomeRocof() {
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(measurement(1000L, 49.98));
        measurements.add(measurement(1000L, 50.00));
        measurements.add(measurement(1000L, 50.50));
        measurements.add(measurement(2000L, 49.50));
        measurements.add(measurement(2000L, 50.00));
        measurements.add(measurement(2000L, 50.02));

        List<FrequencyStabilityWindowFunction.ReportingFrame> frames =
                FrequencyStabilityWindowFunction.aggregateReportingFrames(measurements);
        double rocof = FrequencyStabilityWindowFunction.calculateRoCoF(frames);
        double volatility = FrequencyStabilityWindowFunction.calculateRoCoFVolatility(frames, rocof);

        assertEquals(0.0, rocof, 0.000001);
        assertEquals(0.0, volatility, 0.000001);
    }

    private Measurement measurement(long timestamp, double frequency) {
        Measurement measurement = new Measurement();
        measurement.setTimestamp(timestamp);
        measurement.setFrequency(frequency);
        return measurement;
    }

    private FrequencyStabilityWindowFunction.ReportingFrame frame(long timestamp, double frequency) {
        return new FrequencyStabilityWindowFunction.ReportingFrame(timestamp, frequency);
    }
}
