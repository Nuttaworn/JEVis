package org.jevis.jecalc;

import java.util.concurrent.TimeUnit;

public class JECalcShutdownHookThread extends Thread {
    public static final String THREAD_NAME = "JEDataProcessor Shutdown Hook";

    public static final int GRACE_PERIOD = 0;
    public static final TimeUnit GRACE_PERIOD_TIME_UNIT = TimeUnit.SECONDS;

    private Thread thread;

    /**
     * @param service The Thread to shut down
     */
    public JECalcShutdownHookThread(Thread service) {
        this.thread = service;
        setName(THREAD_NAME);
    }

    @Override
    public void run() {
        System.out.println("Interrupting current Thread");

        System.out.println("Waiting for thread to shut down... Grace period is " + GRACE_PERIOD + GRACE_PERIOD_TIME_UNIT);
        thread.interrupt();

        System.out.println("Thread stopped.");
    }
}
