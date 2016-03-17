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

package com.bc.fiduceo.core;


import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UseCaseConfigValidationTest {

    @Test
    public void testValidation_valid() {
        final UseCaseConfig useCaseConfig = createValidConfig();

        final ValidationResult result = useCaseConfig.checkValid();
        assertTrue(result.isValid());
        final List<String> messages = result.getMessages();
        assertEquals(0, messages.size());
    }

    @Test
    public void testValidation_invalid_missingName() {
        final UseCaseConfig useCaseConfig = createValidConfig();

        useCaseConfig.setName("");

        final ValidationResult result = useCaseConfig.checkValid();
        assertFalse(result.isValid());
        final List<String> messages = result.getMessages();
        assertEquals(1, messages.size());
        assertEquals("Use case name not configured.", messages.get(0));
    }

    @Test
    public void testValidation_invalid_missingTimeDelta() {
        final UseCaseConfig useCaseConfig = createValidConfig();

        useCaseConfig.setTimeDelta(-1);

        final ValidationResult result = useCaseConfig.checkValid();
        assertFalse(result.isValid());
        final List<String> messages = result.getMessages();
        assertEquals(1, messages.size());
        assertEquals("Matchup time delta not configured.", messages.get(0));
    }

    @Test
    public void testValidation_invalid_missingPrimary() {
        final UseCaseConfig useCaseConfig = createValidConfig();

        final List<Sensor> sensors = useCaseConfig.getSensors();
        final Sensor removed = sensors.remove(1);
        assertTrue(removed.isPrimary());

        final ValidationResult result = useCaseConfig.checkValid();
        assertFalse(result.isValid());
        final List<String> messages = result.getMessages();
        assertEquals(1, messages.size());
        assertEquals("Primary sensor not configured.", messages.get(0));
    }

    @Test
    public void testValidation_invalid_missingAdditionalSensor() {
        final UseCaseConfig useCaseConfig = createValidConfig();

        final List<Sensor> sensors = useCaseConfig.getSensors();
        final Sensor removed = sensors.remove(0);
        assertFalse(removed.isPrimary());

        final ValidationResult result = useCaseConfig.checkValid();
        assertFalse(result.isValid());
        final List<String> messages = result.getMessages();
        assertEquals(1, messages.size());
        assertEquals("No additional sensor configured.", messages.get(0));
    }

    @Test
    public void testValidation_invalid_missingDimension() {
        final UseCaseConfig useCaseConfig = createValidConfig();

        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        final Dimension removed = dimensions.remove(1);
        assertEquals("secondary", removed.getName());

        final ValidationResult result = useCaseConfig.checkValid();
        assertFalse(result.isValid());
        final List<String> messages = result.getMessages();
        assertEquals(1, messages.size());
        assertEquals("No dimensions for sensor 'secondary' configured.", messages.get(0));
    }

    @Test
    public void testValidation_invalid_missingOutputPath() {
        final UseCaseConfig useCaseConfig = createValidConfig();

        useCaseConfig.setOutputPath(null);

        final ValidationResult result = useCaseConfig.checkValid();
        assertFalse(result.isValid());
        final List<String> messages = result.getMessages();
        assertEquals(1, messages.size());
        assertEquals("Output path not configured.", messages.get(0));
    }

    @Test
    public void testValidation_invalid_multipleErrors() {
        final UseCaseConfig useCaseConfig = createValidConfig();

        useCaseConfig.setOutputPath(null);
        useCaseConfig.setTimeDelta(-1);

        final ValidationResult result = useCaseConfig.checkValid();
        assertFalse(result.isValid());
        final List<String> messages = result.getMessages();
        assertEquals(2, messages.size());
        assertEquals("Matchup time delta not configured.", messages.get(0));
        assertEquals("Output path not configured.", messages.get(1));
    }

    private UseCaseConfig createValidConfig() {
        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        useCaseConfig.setName("config-name");
        useCaseConfig.setTimeDelta(345);

        final List<Sensor> sensorList = new ArrayList<>();
        sensorList.add(new Sensor("secondary"));
        final Sensor primary = new Sensor("primary");
        primary.setPrimary(true);
        sensorList.add(primary);
        useCaseConfig.setSensors(sensorList);

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("primary", 2, 3));
        dimensions.add(new Dimension("secondary", 4, 5));
        useCaseConfig.setDimensions(dimensions);

        useCaseConfig.setOutputPath("some/arbitrary/path");

        return useCaseConfig;
    }
}
