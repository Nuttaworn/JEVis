/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig;

import java.util.List;
import java.util.concurrent.RecursiveAction;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;

/**
 *
 * @author fs
 */
public class DataPreload extends RecursiveAction {

    private JEVisDataSource ds;
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(DataPreload.class);

    public DataPreload(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    protected void compute() {
        try {
            this.ds.preload();//will load all classes
            ds.getObjects();
        } catch (JEVisException ex) {
            logger.catching(ex);
        }

    }

}
