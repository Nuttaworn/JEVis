package org.jevis.report3.data.report.event;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportConfiguration;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author broder
 */
public class EventIntervalCalc implements IntervalCalculator {

    private static boolean isInit = false;
    private final SampleHandler samplesHandler;
    private Interval interval;

    @Inject
    public EventIntervalCalc(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

    @Override
    public Interval getInterval(PeriodModus modus) {
        return interval;
    }

    public synchronized boolean getIsInit() {
        return isInit;
    }

    public synchronized void setIsInitTrue() {
        isInit = true;
    }

    @Override
    public void buildIntervals(JEVisObject reportObject) {
        try {
            String startRecordString = samplesHandler.getLastSampleAsString(reportObject, "Start Record");
            DateTime start = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);

            Long jevisId = samplesHandler.getLastSampleAsLong(reportObject, "JEVis ID");
            String attributeName = samplesHandler.getLastSampleAsString(reportObject, "Attribute Name");

            DateTime lastDate = samplesHandler.getTimeStampFromLastSample(reportObject.getDataSource().getObject(jevisId), attributeName);

            interval = new Interval(start, lastDate);
        } catch (JEVisException ex) {
            Logger.getLogger(EventIntervalCalc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
