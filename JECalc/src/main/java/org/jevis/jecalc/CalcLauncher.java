/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.utils.Benchmark;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class CalcLauncher extends AbstractCliApp {

    private final Logger logger = LogManager.getLogger(CalcLauncher.class);
    private final Command commands = new Command();
    private static final String APP_INFO = "JECalculation ver. 2018-07-11 - JEVis - Energy Monitring Software";
    private Benchmark bench;
    private int cycleTime = 900000;

    public CalcLauncher(String[] args) {
        super(args, APP_INFO);
    }

    @Override
    protected void runService(Integer cycle_time) {
        java.util.logging.Logger.getLogger(CalcLauncher.class.getName()).log(java.util.logging.Level.SEVERE, "JECalc: service mode not supported");

        if (cycle_time != null) {
            ServiceMode sm = new ServiceMode(ds, cycle_time);
            sm.run();
        } else {
            ServiceMode sm = new ServiceMode(ds);

            sm.run();
        }

    }

    public static void main(String[] args) {
        java.util.logging.Logger.getLogger(CalcLauncher.class.getName()).log(java.util.logging.Level.SEVERE, APP_INFO);
        CalcLauncher app = new CalcLauncher(args);
        app.execute();
    }

    private void run(CalcJobFactory calcJobCreator) {

        while (calcJobCreator.hasNextJob()) {
            bench = new Benchmark();
            try {
                CalcJob calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), ds);
                calcJob.execute();
                bench.printBechmark("Calculation (ID: " + calcJob.getCalcObjectID() + ") finished");
            } catch (Exception ex) {
                logger.error("error with calculation job, aborted", ex);
            }
        }
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
    }

    @Override
    protected void runSingle(Long id) {
        java.util.logging.Logger.getLogger(CalcLauncher.class.getName()).log(java.util.logging.Level.SEVERE, "Start Single Mode");
        try {
            List<JEVisObject> calcObjects = new ArrayList<>();

            calcObjects.add(ds.getObject(id));

            CalcJobFactory calcJobCreator = new CalcJobFactory(calcObjects);
            run(calcJobCreator);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(CalcLauncher.class.getName()).log(java.util.logging.Level.SEVERE, "JECalc: Single mode failed", ex);
        }
    }

    protected class Command {


    }

    @Override
    protected void runComplete() {
        java.util.logging.Logger.getLogger(CalcLauncher.class.getName()).log(java.util.logging.Level.SEVERE, "Start Complete Mode");
        List<JEVisObject> jevisCalcObjects = getCalcObjects();
        logger.info("{} calc jobs found", jevisCalcObjects.size());
        List<JEVisObject> filterForEnabledCalcObjects = getEnabledCalcJobs(jevisCalcObjects);
        logger.info("{} enabled calc jobs found", filterForEnabledCalcObjects.size());
        CalcJobFactory calcJobCreator = new CalcJobFactory(filterForEnabledCalcObjects);
        run(calcJobCreator);
    }

    private List<JEVisObject> getCalcObjects() {
        List<JEVisObject> jevisObjects = new ArrayList<>();
        try {
            JEVisClass calcClass = ds.getJEVisClass(CalcJobFactory.Calculation.CLASS.getName());
            jevisObjects = ds.getObjects(calcClass, false);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return jevisObjects;
    }

    private List<JEVisObject> getEnabledCalcJobs(List<JEVisObject> jevisCalcObjects) {
        List<JEVisObject> enabledObjects = new ArrayList<>();
        SampleHandler sampleHandler = new SampleHandler();
        for (JEVisObject curObj : jevisCalcObjects) {
            Boolean valueAsBoolean = sampleHandler.getLastSampleAsBoolean(curObj, CalcJobFactory.Calculation.ENABLED.getName(), false);
            if (valueAsBoolean) {
                enabledObjects.add(curObj);
            }
        }
        return enabledObjects;
    }
}
