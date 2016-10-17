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

package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.reader.RawDataSource;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class VariablePrototypeList {

    private final HashMap<String, List<VariablePrototype>> prototypesMap;
    private final HashMap<String, RawDataSourceContainer> sourceContainerMap;
    private final ReaderFactory readerFactory;

    VariablePrototypeList(ReaderFactory readerFactory) {
        prototypesMap = new HashMap<>();
        sourceContainerMap = new HashMap<>();
        this.readerFactory = readerFactory;
    }

    public void setRawDataSourceContainer(String sensorName, RawDataSourceContainer container) {
        sourceContainerMap.put(sensorName, container);
    }

    public RawDataSourceContainer getRawDataSourceContainer(String sensorName) {
        final RawDataSourceContainer container = sourceContainerMap.get(sensorName);
        if (container == null) {
            throw new RuntimeException("Requested RawDataSourceContainer of unconfigured type: " + sensorName);
        }
        return container;
    }

    public void setDataSourcePath(String sensorName, Path path) throws IOException {
        final RawDataSourceContainer container = sourceContainerMap.get(sensorName);
        if (container == null) {
            throw new RuntimeException("Invalid sensor name requested: " + sensorName);
        }

        final RawDataSource source = container.getSource();
        source.close();
        source.open(path.toFile());
    }

    public void close() throws IOException {
        final Set<Map.Entry<String, RawDataSourceContainer>> entrySet = sourceContainerMap.entrySet();
        for(final Map.Entry<String, RawDataSourceContainer> entry : entrySet) {
            final RawDataSourceContainer container = entry.getValue();
            container.getSource().close();
        }
    }

    void extractPrototypes(Sensor sensor, Path filePath, Dimension dimension) throws IOException {
        final String sensorName = sensor.getName();

        final List<VariablePrototype> prototypes;
        if (!prototypesMap.containsKey(sensorName)) {
            prototypesMap.put(sensorName, new ArrayList<>());
        }
        prototypes = prototypesMap.get(sensorName);
        final RawDataSourceContainer rawDataSourceContainer = new RawDataSourceContainer();
        setRawDataSourceContainer(sensorName, rawDataSourceContainer);

        try (final Reader reader = readerFactory.getReader(sensorName)) {
            reader.open(filePath.toFile());
            rawDataSourceContainer.setSource(reader);
            final String dimensionNames = createDimensionNames(dimension);
            final List<Variable> variables = reader.getVariables();
            for (final Variable variable : variables) {
                final VariablePrototype prototype = new VariablePrototype(rawDataSourceContainer);
                prototype.setSourceVariableName(variable.getShortName());
                prototype.setTargetVariableName(sensorName + "_" + variable.getShortName());
                prototype.setDataType(variable.getDataType().toString());
                prototype.setDimensionNames(dimensionNames);
                prototype.setAttributes(getAttributeClones(variable));

                prototypes.add(prototype);
            }
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }
    }

    static List<Attribute> getAttributeClones(Variable variable) {
        final List<Attribute> attributes = variable.getAttributes();
        final ArrayList<Attribute> newAttributes = new ArrayList<>();
        for (Attribute attribute : attributes) {
            final String attName = attribute.getShortName();
            final Array attVals = attribute.getValues().copy();
            final Attribute newAttribute = new Attribute(attName, attVals);
            newAttributes.add(newAttribute);
        }
        return newAttributes;
    }

    List<VariablePrototype> get() {
        final ArrayList<VariablePrototype> allPrototypes = new ArrayList<>();
        for (List<VariablePrototype> prototypes : prototypesMap.values()) {
            allPrototypes.addAll(prototypes);
        }
        return allPrototypes;
    }

    /**
     * Returns a list of {@link VariablePrototype} associated with the given sensor name.
     * If there are no associated {@link VariablePrototype}s, an empty list will be returned.
     *
     * @param sensorName the name of the sensor
     * @return a list of {@link VariablePrototype}
     */
    List<VariablePrototype> getPrototypesFor(String sensorName) {
        if (prototypesMap.containsKey(sensorName)) {
            return prototypesMap.get(sensorName);
        }
        return new ArrayList<>();
    }

    void add(VariablePrototype prototype, String sensorName) {
        List<VariablePrototype> sensorPrototypes = prototypesMap.get(sensorName);
        if (sensorPrototypes == null) {
            sensorPrototypes = new ArrayList<>();
            prototypesMap.put(sensorName, sensorPrototypes);
        }

        sensorPrototypes.add(prototype);
    }

    List<String> getSensorNames() {
        final ArrayList<String> sensorNamesList = new ArrayList<>();
        final Set<String> keySet = prototypesMap.keySet();
        sensorNamesList.addAll(keySet);
        return sensorNamesList;
    }

    // package access for testing only tb 2016-04-12
    static String createDimensionNames(Dimension dimension) {
        final String dimensionName = dimension.getName();
        return "matchup_count " + dimensionName + "_ny " + dimensionName + "_nx";
    }
}
