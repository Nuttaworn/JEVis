/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.workflow;

import org.jevis.api.JEVisException;
import org.jevis.jecalc.data.ResourceManager;

/**
 * @author broder
 */
public interface ProcessStep {

    void run(ResourceManager resourceManager) throws JEVisException;

}
