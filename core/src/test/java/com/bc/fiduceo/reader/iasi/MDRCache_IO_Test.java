/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.iasi;

import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MDRCache_IO_Test {

    private ImageInputStream iis;

    @Before
    public void setUp() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();

        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"iasi-mb", "v7-0N", "2014", "04", "IASI_xxx_1C_M01_20140425124756Z_20140425142652Z_N_O_20140425133911Z.nat"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());

        iis = new FileImageInputStream(file);
    }

    @After
    public void tearDown() throws IOException {
        iis.close();
    }

    @Test
    public void testReadOneRecord() throws IOException {
        final MDRCache mdrCache = new MDRCache(iis, 231818L);

        final MDR_1C mdr = mdrCache.getRecord(0);
        assertNotNull(mdr);
    }

    @Test
    public void testReadOneRecord_twiceReturnsTheSameObject() throws IOException {
        final MDRCache mdrCache = new MDRCache(iis, 231818L);

        final MDR_1C mdr_1 = mdrCache.getRecord(167);
        assertNotNull(mdr_1);

        final MDR_1C mdr_2 = mdrCache.getRecord(167);
        assertNotNull(mdr_2);

        assertSame(mdr_1, mdr_2);
    }

    @Test
    public void testReadOneRecord_coversTwoConsecutiveLines() throws IOException {
        final MDRCache mdrCache = new MDRCache(iis, 231818L);

        final MDR_1C mdr_216 = mdrCache.getRecord(216);
        assertNotNull(mdr_216);

        final MDR_1C mdr_217 = mdrCache.getRecord(217);
        assertNotNull(mdr_217);

        assertSame(mdr_216, mdr_217);
    }

    @Test
    public void testReadRecordsUntilCacheIsFull() throws IOException {
        final MDRCache mdrCache = new MDRCache(iis, 231818L);

        final MDR_1C mdr_first = mdrCache.getRecord(216);
        assertNotNull(mdr_first);

        for (int i = 0; i < MDRCache.CAPACITY + 2; i++) {
            final MDR_1C mdr_217 = mdrCache.getRecord(217 + i);
            assertNotNull(mdr_217);
        }

        final MDR_1C mdr_second = mdrCache.getRecord(216);
        assertNotNull(mdr_second);
        assertNotSame(mdr_first, mdr_second);
    }
}
