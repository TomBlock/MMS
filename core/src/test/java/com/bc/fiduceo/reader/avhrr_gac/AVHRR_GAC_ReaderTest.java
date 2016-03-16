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

package com.bc.fiduceo.reader.avhrr_gac;


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.ClippingPixelLocator;
import com.bc.fiduceo.location.PixelLocator;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AVHRR_GAC_ReaderTest {

    private AVHRR_GAC_Reader reader;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        reader = new AVHRR_GAC_Reader();
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @Test
    public void testGetRegEx() {
        final String regEx = reader.getRegEx();
        assertEquals("[0-9]{14}-ESACCI-L1C-AVHRR([0-9]{2}|MTA)_G-fv\\d\\d.\\d.nc", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.AMBX.NK.D15365.S1249.E1420.B9169697.GC");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("L8912163.NSS.AMBX.NK.D08001.S0000.E0155.B5008586.GC.gz.l1c.h5");
        assertFalse(matcher.matches());
    }

    @Test
    public void testParseDateAttribute() throws Exception {
        final Attribute timeAttribute = mock(Attribute.class);
        when(timeAttribute.getStringValue()).thenReturn("20060526T054530Z");

        final Date date = AVHRR_GAC_Reader.parseDateAttribute(timeAttribute);
        TestUtil.assertCorrectUTCDate(2006, 5, 26, 5, 45, 30, date);
    }

    @Test
    public void testParseDateAttribute_NullAttribute() throws Exception {
        try {
            AVHRR_GAC_Reader.parseDateAttribute(null);
            fail("IO Exception expected");
        } catch (IOException e) {
        }
    }

    @Test
    public void testParseDateAttribute_Return_Null_Value() throws Exception {
        final Attribute timeAttribute = mock(Attribute.class);
        when(timeAttribute.getStringValue()).thenReturn("");

        try {
            AVHRR_GAC_Reader.parseDateAttribute(timeAttribute);
            fail("IO Exception expected");
        } catch (IOException e) {
        }
    }

    @Test
    public void testParseDateAttribute_Unparseable_Attribute() throws Exception {
        final Attribute timeAttribute = mock(Attribute.class);
        when(timeAttribute.getStringValue()).thenReturn("234390123T77");

        try {
            AVHRR_GAC_Reader.parseDateAttribute(timeAttribute);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testCheckForValidity_valid() {
        final Geometry geometry_1 = mock(Geometry.class);
        when(geometry_1.isValid()).thenReturn(true);

        final Geometry geometry_2 = mock(Geometry.class);
        when(geometry_2.isValid()).thenReturn(true);

        final GeometryCollection geometryCollection = createGeometryCollection(geometry_1, geometry_2);

        try {
            AVHRR_GAC_Reader.checkForValidity(geometryCollection);
        } catch (Exception e) {
            fail("No exception expected");
        }
    }

    @Test
    public void testCheckForValidity_invalid() {
        final Geometry geometry_1 = mock(Geometry.class);
        when(geometry_1.isValid()).thenReturn(true);

        final Geometry geometry_2 = mock(Geometry.class);
        when(geometry_2.isValid()).thenReturn(false);

        final GeometryCollection geometryCollection = createGeometryCollection(geometry_1, geometry_2);

        try {
            AVHRR_GAC_Reader.checkForValidity(geometryCollection);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetSubScenePixelLocator_firstScene() throws Exception {
        final Polygon polygon = mock(Polygon.class);
        when(polygon.getCentroid()).thenReturn(geometryFactory.createPoint(26, 40));
        final PixelLocator locator = mock(PixelLocator.class);
        when(locator.getGeoLocation(100.5, 2500.5, null)).thenReturn(new Point2D.Double(30, 45));
        when(locator.getGeoLocation(100.5, 7500.5, null)).thenReturn(new Point2D.Double(15, -45));

        final PixelLocator pixelLocator = AVHRR_GAC_Reader.getSubScenePixelLocator(polygon, 200, 9000, 5000, locator);

        verify(locator, times(1)).getGeoLocation(100.5, 2500.5, null);
        verify(locator, times(1)).getGeoLocation(100.5, 7500.5, null);
        verifyNoMoreInteractions(locator);
        verify(polygon, times(1)).getCentroid();
        verifyNoMoreInteractions(polygon);

        assertNotNull(pixelLocator);
        assertEquals(true, pixelLocator instanceof ClippingPixelLocator);
        final ClippingPixelLocator clipping = (ClippingPixelLocator) pixelLocator;
        assertEquals(0, clipping.minY);
        assertEquals(4999, clipping.maxY);
        assertSame(locator, clipping.pixelLocator);
    }

    @Test
    public void testGetSubScenePixelLocator_secondScene() throws Exception {
        final Polygon polygon = mock(Polygon.class);
        when(polygon.getCentroid()).thenReturn(geometryFactory.createPoint(17, -40));
        final PixelLocator locator = mock(PixelLocator.class);
        when(locator.getGeoLocation(100.5, 2500.5, null)).thenReturn(new Point2D.Double(30, 45));
        when(locator.getGeoLocation(100.5, 7500.5, null)).thenReturn(new Point2D.Double(15, -45));

        final PixelLocator pixelLocator = AVHRR_GAC_Reader.getSubScenePixelLocator(polygon, 200, 9000, 5000, locator);

        verify(locator, times(1)).getGeoLocation(100.5, 2500.5, null);
        verify(locator, times(1)).getGeoLocation(100.5, 7500.5, null);
        verifyNoMoreInteractions(locator);
        verify(polygon, times(1)).getCentroid();
        verifyNoMoreInteractions(polygon);

        assertNotNull(pixelLocator);
        assertEquals(true, pixelLocator instanceof ClippingPixelLocator);
        final ClippingPixelLocator clipping = (ClippingPixelLocator) pixelLocator;
        assertEquals(4999, clipping.minY);
        assertEquals(8999, clipping.maxY);
        assertSame(locator, clipping.pixelLocator);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeDouble() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.DOUBLE);

        final Number value = AVHRR_GAC_Reader.getDefaultFillValue(mock);

        assertEquals(Double.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeFloat() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.FLOAT);

        final Number value = AVHRR_GAC_Reader.getDefaultFillValue(mock);

        assertEquals(Float.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeLong() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.LONG);

        final Number value = AVHRR_GAC_Reader.getDefaultFillValue(mock);

        assertEquals(Long.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeInt() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.INT);

        final Number value = AVHRR_GAC_Reader.getDefaultFillValue(mock);

        assertEquals(Integer.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeShort() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.SHORT);

        final Number value = AVHRR_GAC_Reader.getDefaultFillValue(mock);

        assertEquals(Short.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forDataTypeByte() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.BYTE);

        final Number value = AVHRR_GAC_Reader.getDefaultFillValue(mock);

        assertEquals(Byte.MIN_VALUE, value);
    }

    @Test
    public void testFetchingTheDefaultFillValue_forUnknownType() throws Exception {
        final Variable mock = mock(Variable.class);
        when(mock.getDataType()).thenReturn(DataType.STRUCTURE);

        try {
            AVHRR_GAC_Reader.getDefaultFillValue(mock);
            fail("RuntimeException expected");
        } catch (NullPointerException notExpected) {
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testExtractFillValue_throwsRuntimeExceptionIfThereIsNoFillValueAndTheVariableHasAnUndefinedDataType() {
        final Variable mock = mock(Variable.class);
        when(mock.findAttribute("_FillValue")).thenReturn(null);
        when(mock.getDataType()).thenReturn(DataType.STRUCTURE);

        try {
            AVHRR_GAC_Reader.extractFillValue(mock);
            fail("RuntimeException expected");
        } catch (NullPointerException notExpected) {
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testExtractFillValue_FromVariable() throws Exception {
        final Variable mock = mock(Variable.class);
        final Attribute attributeMock = mock(Attribute.class);
        when(attributeMock.getNumericValue()).thenReturn(39.90);
        when(mock.findAttribute("_FillValue")).thenReturn(attributeMock);

        final Number number = AVHRR_GAC_Reader.extractFillValue(mock);

        assertEquals(39.90, number);
    }

    GeometryCollection createGeometryCollection(Geometry geometry_1, Geometry geometry_2) {
        final Geometry[] geometries = new Geometry[]{geometry_1, geometry_2};
        return geometryFactory.createGeometryCollection(geometries);
    }
}