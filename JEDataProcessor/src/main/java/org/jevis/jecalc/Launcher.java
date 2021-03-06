/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc;

import org.jevis.jecalc.workflow.ProcessManager;
import org.jevis.jecalc.workflow.ProcessManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author broder
 */
public class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private int cycleTime = 1800000;

    public static void main(String[] args) {
        //parse Commandline
        CommandLineParser cmd = CommandLineParser.getInstance();
        cmd.parse(args);

        Launcher launcher = new Launcher();

        if (!cmd.isServiceMode()) {
            launcher.run();
        } else {
            if (cmd.getCycleTime() != null) {
                ServiceMode sm = new ServiceMode(cmd.getCycleTime());
                sm.run();
            } else {
                ServiceMode sm = new ServiceMode();
                sm.run();
            }
        }
    }

    private void run() {
        List<ProcessManager> processes = ProcessManagerFactory.getProcessManagerList();

        logger.info("{} cleaning jobs found", processes.size());
        processes.stream().forEach((currentProcess) -> {
            try {
                currentProcess.start();
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                logger.debug(ex.getMessage(), ex);
            }
        });
    }


}
