package org.example.windowsFunctions;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.example.models.Measurement;
import org.example.models.SystemMetrics;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SystemAggregationWindowFunction extends
        ProcessWindowFunction<Measurement, SystemMetrics, String, TimeWindow> {

    @Override
    public void process(
            String key,
            Context context,
            Iterable<Measurement> measurements,
            Collector<SystemMetrics> out) throws Exception {

        List<Measurement> windowData = new ArrayList<>();
        measurements.forEach(windowData::add);

        if (windowData.isEmpty()) return;

        Set<String> activePmus = new HashSet<>();
        windowData.forEach(m -> activePmus.add(m.getPmuId()));

        DoubleSummaryStatistics freqStats = windowData.stream()
                .mapToDouble(Measurement::getFrequency)
                .summaryStatistics();

        DoubleSummaryStatistics voltStats = windowData.stream()
                .mapToDouble(Measurement::getVoltageMagnitude)
                .summaryStatistics();

        DoubleSummaryStatistics currStats = windowData.stream()
                .mapToDouble(Measurement::getCurrentMagnitude)
                .summaryStatistics();

        SystemMetrics metrics = new SystemMetrics();
        metrics.setTimestamp(context.window().getEnd());
        metrics.setWindowStart(context.window().getStart());
        metrics.setWindowEnd(context.window().getEnd());

        metrics.setActivePmuCount(activePmus.size());

        metrics.setAvgFrequency(freqStats.getAverage());
        metrics.setMinFrequency(freqStats.getMin());
        metrics.setMaxFrequency(freqStats.getMax());

        metrics.setAvgVoltage(voltStats.getAverage());
        metrics.setMinVoltage(voltStats.getMin());
        metrics.setMaxVoltage(voltStats.getMax());

        metrics.setAvgCurrent(currStats.getAverage());
        metrics.setMinCurrent(currStats.getMin());
        metrics.setMaxCurrent(currStats.getMax());

        out.collect(metrics);
    }
}