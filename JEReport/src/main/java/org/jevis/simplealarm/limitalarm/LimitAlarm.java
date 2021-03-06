/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.simplealarm.limitalarm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.simplealarm.AlarmData;
import org.jevis.simplealarm.PeriodService;
import org.jevis.simplealarm.usageperiod.UsagePeriod;
import org.joda.time.DateTime;

/**
 *
 * @author ai
 */
public abstract class LimitAlarm implements ILimitAlarm {

    private final int SILENT = 1;
    private final int STANDBY = 2;
    public final static String SILENT_TIME = "Silent Time";
    public final static String STANDBY_TIME = "Standby Time";
    public final static String STATUS = "Status";
    public final static String ENABLED = "Enable";
    public final static String ALARM_LOG = "Alarm Log";
    public final static String OPERATOR_ATTRIBUTE = "Operator";
    final JEVisObject alarmObj;
    JEVisAttribute _status;
    boolean _enabled = false;
    JEVisAttribute _log;
    private List<UsagePeriod> up = new ArrayList<>();

    @Override
    abstract public void checkAlarm() throws JEVisException;

    public enum OPERATOR {
        SMALER, BIGGER, EQUALS, UNEQUALS
    }

    public LimitAlarm(JEVisObject alarm) {
        alarmObj = alarm;
    }

    @Override
    public void init() {
        try {
            _log = alarmObj.getAttribute(ALARM_LOG);
            _status = alarmObj.getAttribute(STATUS);
            _enabled = isEnabled(alarmObj);
            for (JEVisAttribute att : alarmObj.getAttributes()) {
                System.out.println("   Attribute: " + att);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initUsageTime();
    }

    //public abstract void checkAlarm() throws JEVisException;
    void logAlarms(JEVisAttribute logAtt, List<AlarmData> alarms) {
        Integer lastLog = 0;
        try {
            JEVisSample lstLog = logAtt.getLatestSample();
            if (lstLog != null) {
                lastLog = lstLog.getValueAsLong().intValue();
            }
        } catch (JEVisException jex) {
            jex.printStackTrace();
        }

        List<JEVisSample> alarmLogs = new ArrayList<>();
        for (AlarmData alarm : alarms) {
            Integer logVal = 0;
            if (alarm.isAlarm()) {
                logVal = PeriodService.getValueForLog(alarm.getTime(), up);
            }

            try {
                alarmLogs.add(logAtt.buildSample(alarm.getTime(), logVal));
            } catch (JEVisException ex) {
                Logger.getLogger(LimitAlarm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            logAtt.addSamples(alarmLogs);
        } catch (JEVisException ex) {
            Logger.getLogger(LimitAlarm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    DateTime getLastUpdate() throws JEVisException {

        try {
            if (_status.hasSample()) {
                JEVisSample dateSample = _status.getLatestSample();

                return dateSample.getTimestamp();
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    OPERATOR getOperator(JEVisObject alarm) throws JEVisException {
        JEVisAttribute operatorAtt = alarm.getAttribute(OPERATOR_ATTRIBUTE);

        JEVisSample operatorSample = operatorAtt.getLatestSample();

        try {

            switch (operatorSample.getValueAsString()) {
                case "<":
                    return OPERATOR.SMALER;
                case ">":
                    return OPERATOR.BIGGER;
                case "=":
                    return OPERATOR.EQUALS;
                case "!=":
                    return OPERATOR.UNEQUALS;
            }
        } catch (Exception ex) {
            Logger.getLogger(LimitAlarm.class.getName()).log(Level.SEVERE, "Error could not parse Operator");
        }
        return OPERATOR.SMALER;
    }

    boolean isEnabled(JEVisObject alarmObj) {
        try {
            JEVisAttribute enabeldatt = alarmObj.getAttribute(ENABLED);
            if (enabeldatt.hasSample()) {
                boolean value = enabeldatt.getLatestSample().getValueAsBoolean();
//                System.out.println("ebalbed: " + value);
                return value;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    void setStatus(DateTime ts, String message) {
        try {
            JEVisSample sample = _status.buildSample(DateTime.now(), message);
            sample.commit();
        } catch (Exception ex) {

        }

    }

    @SuppressWarnings("unused")
    private void printAlarms(List<AlarmData> alarms) {
        for (AlarmData alarm : alarms) {
            System.out.println("Alarm: " + alarm.getMessage());
        }
    }

    private void initUsageTime() {

        UsagePeriod silentTime = new UsagePeriod(SILENT);
        silentTime.setPeriod(alarmObj, SILENT_TIME);
        up.add(silentTime);
        UsagePeriod standbyTime = new UsagePeriod(STANDBY);
        standbyTime.setPeriod(alarmObj, STANDBY_TIME);
        up.add(standbyTime);
    }

}
