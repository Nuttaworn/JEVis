/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.report3.DateHelper;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.simplealarm.limitalarm.DynamicLimitAlarm;
import org.jevis.simplealarm.AlarmPeriod;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author broder
 */
public class AlarmFunction implements ReportData {

    private String templateName = "";
    private String alarmLinkName = "";

    private static String COLUMN_FROM = "from";
    private static String COLUMN_UNTIL = "until";
    private static String COLUMN_DIFF = "diff";
    private static String COLUMN_REFF = "reff";
    private static String COLUMN_IST = "value";

    private JEVisObject alarmObj = null;
    private JEVisObject linkObject;

    public AlarmFunction(JEVisObject funktionObject) {
        try {

            this.linkObject = funktionObject;
            templateName = funktionObject.getAttribute("Template Name").getLatestSample().getValueAsString();
            alarmLinkName = funktionObject.getAttribute("Alarm Link").getLatestSample().getValueAsString();

            TargetHelper th = new TargetHelper(funktionObject.getDataSource(), funktionObject.getAttribute("Alarm Link"));
            if (th.hasObject()) {
                alarmObj = th.getObject();
            }
        } catch (JEVisException ex) {
            Logger.getLogger(AlarmFunction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Called form outside
     *
     * @param property
     * @param intervalCalc
     * @return
     */
    @Override
    public Map<String, Object> getReportMap(ReportProperty property, IntervalCalculator intervalCalc) {
        Map<String, Object> funktionMap = new HashMap<>();
        Interval interval = intervalCalc.getInterval(IntervalCalculator.PeriodModus.CURRENT);
        DateTime start = interval.getStart();
        DateTime end = interval.getEnd();

        try {
            System.out.println("\n\nCheck Alarm: [" + alarmObj.getID() + "]" + alarmObj.getName());
            DynamicLimitAlarm sAlarm = new DynamicLimitAlarm(alarmObj);
            sAlarm.init();
            List<AlarmPeriod> alarms = sAlarm.makeAlarmReport(interval.getStart(), interval.getEnd());

            int limit = 0;

            List<Object> content = new ArrayList<>();
            for (AlarmPeriod ap : alarms) {
                if (limit++ >= 12) {
                    break;
                }
                content.add(getElement(ap));
            }
            funktionMap.put(templateName, content);

        } catch (Exception ex) {
            System.out.println("Error while creating Dynamic Alarm");
            ex.printStackTrace();
        }

        return funktionMap;
    }

    private Map<String, Object> getElement(AlarmPeriod alarm) {
        //hier is das mit .value/.timestamp usw
        Map<String, Object> tmpMap = new HashMap<>();
        tmpMap.put(COLUMN_FROM, DateHelper.transformTimestampsToExcelTime(alarm.getPeriodStart()));
        tmpMap.put(COLUMN_UNTIL, DateHelper.transformTimestampsToExcelTime(alarm.getPeriodEnd()));
        tmpMap.put(COLUMN_IST, alarm.getSumIst());
        tmpMap.put(COLUMN_REFF, alarm.getSumSoll());
        tmpMap.put(COLUMN_DIFF, alarm.getSumIst() - alarm.getSumSoll());

        return tmpMap;
    }

    @Override
    public JEVisObject getDataObject() {
        return alarmObj;
    }

    @Override
    public LinkStatus getReportLinkStatus(DateTime end) {
        return new LinkStatus(true, "ok");
    }

}
