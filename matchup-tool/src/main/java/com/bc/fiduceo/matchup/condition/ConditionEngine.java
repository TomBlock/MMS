/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.matchup.MatchupSet;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConditionEngine {

    private final List<Condition> conditionsList;

    public ConditionEngine() {
        conditionsList = new ArrayList<>();
    }

    public void process(MatchupSet matchupSet) {
        for (final Condition condition : conditionsList) {
            condition.apply(matchupSet);
        }
    }

    public void configure(UseCaseConfig useCaseConfig, Date startDate, Date endDate) {
        final Element conditionsElem = useCaseConfig.getDomElement("conditions");
        if (conditionsElem != null) {
            final List<Element> children = conditionsElem.getChildren();
            for (Element child : children) {
                final Condition condition = ConditionFactory.get().getCondition(child);
                if (condition != null) {
                    conditionsList.add(condition);
                }
            }
        }

        if (startDate != null && endDate != null && startDate.before(endDate)) {
            final TimeRangeCondition timeRangeCondition = new TimeRangeCondition(startDate, endDate);
            conditionsList.add(timeRangeCondition);
        }
    }
}
