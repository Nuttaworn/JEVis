/**
 * Copyright (C) 2015 - 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEVis CSV-Driver.
 *
 * JEVis CSV-Driver is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEVis CSV-Driver is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEVis CSV-Driver. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEVis CSV-Driver is part of the OpenJEVis project, further project
 * information are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.csvparser;

/**
 *
 * @author broder
 */
public class DataPoint {

    private String mappingIdentifier;
    private Integer valueIndex;
    private Long target;

    public String getMappingIdentifier() {
        return mappingIdentifier;
    }

    public void setMappingIdentifier(String mappingIdentifier) {
        this.mappingIdentifier = mappingIdentifier;
    }

    public Integer getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(Integer valueIndex) {
        this.valueIndex = valueIndex;
    }

    public Long getTarget() {
        return target;
    }

    public void setTarget(Long target) {
        this.target = target;
    }

}
