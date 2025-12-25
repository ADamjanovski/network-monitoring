package org.example.windowsFunctions;

import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.example.models.AnomalyResult;
import org.example.models.AnomalyTypes;
import org.example.models.Measurement;

import java.util.DoubleSummaryStatistics;

public class ElectricalFaultDetection implements WindowFunction<Measurement,AnomalyResult,String, TimeWindow> {


    public double baseVoltage=20000.0;
    public double baseCurrent=100.0;



    @Override
    public void apply(String key, TimeWindow timeWindow, Iterable<Measurement> iterable, Collector<AnomalyResult> collector) throws Exception {
        DoubleSummaryStatistics voltageStatistics =new DoubleSummaryStatistics();
        DoubleSummaryStatistics currentStatistics =new DoubleSummaryStatistics();

        iterable.forEach(measurement -> voltageStatistics.accept(measurement.getVoltage()));
        iterable.forEach(measurement -> currentStatistics.accept(measurement.getCurrent()));

        double avgVoltage=voltageStatistics.getAverage();
        double maxCurrent = currentStatistics.getMax();
        if (avgVoltage<(baseVoltage*0.9)){
            collector.collect(new AnomalyResult(
                    key,
                    AnomalyTypes.VOLTAGE_SAG.name(),
                    timeWindow.getStart(),
                    timeWindow.getEnd(),
                    avgVoltage,
                    baseVoltage*0.9
                    ));
        }else if(avgVoltage>(baseVoltage*1.1)){
            collector.collect(new AnomalyResult(
                    key,
                    AnomalyTypes.VOLTAGE_SWELL.name(),
                    timeWindow.getStart(),
                    timeWindow.getEnd(),
                    avgVoltage,
                    baseVoltage*1.1
            ));
        }
        if(maxCurrent>(baseCurrent*2)){
            AnomalyResult res= new AnomalyResult(
                    key,
                    AnomalyTypes.CRITICAL_OVERCURRENT.name(),
                    timeWindow.getStart(),
                    timeWindow.getEnd(),
                    maxCurrent,
                    baseCurrent
            );
            collector.collect(res);
        }else if(maxCurrent>baseCurrent){
            collector.collect(new AnomalyResult(
                    key,
                    AnomalyTypes.WARNING_OVERCURRENT.name(),
                    timeWindow.getStart(),
                    timeWindow.getEnd(),
                    maxCurrent,
                    baseCurrent
            ));
        }
    }
}
