/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.gap;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtuelSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jevis.api.JEVisConstants.GapFillingType;

/**
 * @author broder
 */
public class FillGapStep implements ProcessStep {

    private static final Logger logger = LoggerFactory.getLogger(FillGapStep.class);

    @Override
    public void run(ResourceManager resourceManager) {
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();
        if (!calcAttribute.getIsPeriodAligned()) { //no gap filling when there is no alignment
            return;
        }
        StopWatch stopWatch = new Slf4JStopWatch("gap_filling");
        List<CleanInterval> intervals = resourceManager.getIntervals();

        //identify gaps, gaps holds intervals
        List<Gap> gaps = identifyGaps(intervals, calcAttribute);
        logger.info("{} gaps identified", gaps.size());
        if (gaps.isEmpty()) { //no gap filling when there is no alignment
            return;
        }

        //mode NONE, INTERPOLATION, STATIC, set the interval values
        //GapMode gapMode = calcAttribute.getGapFillingMode().getGapMode();
        List<JsonGapFillingConfig> conf = calcAttribute.getGapFillingConfig();

//        logger.info("start filling with gapmode", gapMode.name());
//        switch (gapMode) {
//            case NONE:
//                break;
//            case STATIC:
//                fillStatic(gaps);
//                break;
//            case INTERPOLATION:
//                fillInterpolation(gaps);
//                break;
//            case DEFAULT:
//                Double defaultValue = Double.valueOf(calcAttribute.getGapFillingMode().getValue(Gap.GapAttribute.DEFAULT_VALUE));
//                fillDefault(gaps, defaultValue);
//                break;
//            default:
//                break;
//        }

        for (JsonGapFillingConfig c : conf) {
            logger.info("start filling with new Mode for " + c.getType());
            List<Gap> newGaps = new ArrayList<>();
            for (Gap g : gaps) {
                DateTime firstDate = g.getIntervals().get(0).getDate();
                DateTime lastDate = g.getIntervals().get(g.getIntervals().size() - 1).getDate();
                System.out.println("Diff: " + (lastDate.getMillis() - firstDate.getMillis()));
                if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getBoundary())) {
                    System.out.println("Gap: " + g.toString() + " FirstDate: " + firstDate + " LastDate: " + lastDate);
                    newGaps.add(g);
                }

                switch (c.getType()) {
                    case GapFillingType.NONE:
                        break;
                    case GapFillingType.STATIC:
                        fillStatic(newGaps);
                        break;
                    case GapFillingType.INTERPOLATION:
                        fillInterpolation(newGaps);
                        break;
                    case GapFillingType.DEFAULT_VALUE:
                        Double defaultValue = Double.valueOf(c.getDefaultvalue());
                        fillDefault(newGaps, defaultValue);
                        break;
                    case GapFillingType.AVERAGE:
                        break;
                    case GapFillingType.MAXIMUM:
                        break;
                    case GapFillingType.MINIMUM:
                        break;
                    case GapFillingType.MEDIAN:
                        break;
                    default:
                        break;
                }
            }
        }

        stopWatch.stop();
    }

    private Long defaultValue(String s) {
        Long l = 0L;
        if (Objects.nonNull(s)) {
            l = Long.parseLong(s);
        }
        return l;
    }

    private List<Gap> identifyGaps(List<CleanInterval> intervals, CleanDataAttribute calcAttribute) {
        List<Gap> gaps = new ArrayList<>();
        Gap currentGap = null;
        Double lastValue = calcAttribute.getLastCleanValue();
        for (CleanInterval currentInterval : intervals) {
            if (currentInterval.getTmpSamples().isEmpty()) { //could be the start of the gap or in a gap
                if (currentGap != null) {//current in a gap
                    currentGap.addInterval(currentInterval);
                } else { //start of a gap
                    currentGap = new GapJEVis();
                    currentGap.addInterval(currentInterval);
                    currentGap.setFirstValue(lastValue);
                }
            } else { //could be the end of the gap or no gap
                for (JEVisSample sample : currentInterval.getTmpSamples()) {
                    try {
                        Double rawValue = sample.getValueAsDouble();
                        if (currentGap != null) { //end of the gap
                            currentGap.setLastValue(rawValue);
                            gaps.add(currentGap);
                            currentGap = null;
                            lastValue = sample.getValueAsDouble();
                        } else { //not in a gap
                            lastValue = sample.getValueAsDouble();
                        }
                    } catch (JEVisException ex) {
                        logger.error(null, ex);
                    }
                }
            }
        }

        List<Gap> filteredGaps = new ArrayList<>();
        for (Gap gap : gaps) {
            List<CleanInterval> currentIntervals = gap.getIntervals();
            Double firstGapValue = gap.getFirstValue();
            Double lastGapValue = gap.getLastValue();
            if (!currentIntervals.isEmpty() && firstGapValue != null && lastGapValue != null) {
                filteredGaps.add(gap);
            }
        }
        return filteredGaps;
    }

    private void fillStatic(List<Gap> gaps) {
        for (Gap currentGap : gaps) {
            Double firstValue = currentGap.getFirstValue();
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    JEVisSample sample = new VirtuelSample(currentInterval.getDate(), firstValue);
                    String note = "gap(static)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillInterpolation(List<Gap> gaps) {
        for (Gap currentGap : gaps) {
            Double firstValue = currentGap.getFirstValue();
            Double lastValue = currentGap.getLastValue();
            int size = currentGap.getIntervals().size() + 1; //if there is a gap of 2, then you have 3 steps
            Double stepSize = (lastValue - firstValue) / (double) size;
            Double currenValue = firstValue + stepSize;
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    JEVisSample sample = new VirtuelSample(currentInterval.getDate(), firstValue);
                    sample.setValue(currenValue);
                    currenValue += stepSize;
                    String note = "gap(interpolation)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillDefault(List<Gap> gaps, Double value) {
        for (Gap currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    JEVisSample sample = new VirtuelSample(currentInterval.getDate(), value);
                    String note = "gap(default)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

}
