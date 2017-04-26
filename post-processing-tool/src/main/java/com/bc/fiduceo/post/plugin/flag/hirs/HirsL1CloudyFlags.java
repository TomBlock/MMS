/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.post.plugin.flag.hirs;

import static com.bc.fiduceo.util.NetCDFUtils.*;

import com.bc.fiduceo.post.Constants;
import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.ReaderCache;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

class HirsL1CloudyFlags extends PostProcessing {

    public final static float DELTA_1_LAND_OR_ICE_COVERED = 6.5f;
    public final static byte SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE = 1;
    public final static byte SPACE_CONTRAST_TEST_WARNING = 2;
    public final static byte SPACE_CONTRAST_TEST_CLOUDY = 4;
    public final static byte INTERCHANNEL_TEST_CLOUDY = 8;
    private final static DataType FLAG_VAR_DATA_TYPE = DataType.BYTE;
    final String flagVarName;
    final String btVarName_11_1_µm;
    final String btVarName_6_5_µm;
    final String latVarName;
    final String lonVarName;
    final String sourceFileVarName;
    final DistanceToLandMap distanceToLandMap;
    private float fillValue_11_1;
    private float fillValue_6_5;
    private Variable var11_1µm;
    private Variable var6_5µm;
    private Variable varFlags;
    private Variable sourceFileVar;
    private int[] levelShape;
    private Array data11_1;
    private Array data6_5;
    private Array flags;
    private Index index;
    private Array lats;
    private Array lons;
    private int[] shape;
    private ReaderCache readerCache;

    public HirsL1CloudyFlags(final String btVarName_11_1_µm,
                             final String btVarName_6_5_µm,
                             final String flagVarName,
                             final String latVarName,
                             final String lonVarName,
                             final String sourceFileVarName,
                             final DistanceToLandMap distanceToLandMap) {
        this.flagVarName = flagVarName;
        this.btVarName_11_1_µm = btVarName_11_1_µm;
        this.btVarName_6_5_µm = btVarName_6_5_µm;
        this.latVarName = latVarName;
        this.lonVarName = lonVarName;
        this.sourceFileVarName = sourceFileVarName;
        this.distanceToLandMap = distanceToLandMap;
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable variable = getVariable(reader, btVarName_11_1_µm);
        final String dimensions = variable.getDimensionsString();
        final Variable flagVar = writer.addVariable(null, flagVarName, FLAG_VAR_DATA_TYPE, dimensions);

        // add flag meanings and other Attributes to flagVar
        Array masks = new ArrayByte(new int[]{4});
        masks.setByte(0, SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE);
        masks.setByte(1, SPACE_CONTRAST_TEST_WARNING);
        masks.setByte(2, SPACE_CONTRAST_TEST_CLOUDY);
        masks.setByte(3, INTERCHANNEL_TEST_CLOUDY);
        final String Separator = "\t";

        flagVar.addAttribute(new Attribute("flag_meanings", Arrays.asList("sc_all", "sc_warning", "sc_cloudy", "ic_cloudy")));
        flagVar.addAttribute(new Attribute("flag_masks", masks));
        flagVar.addAttribute(new Attribute("flag_coding_name", "hirs_cloudy_flags"));
        flagVar.addAttribute(new Attribute("flag_descriptions", "space contrast test, all pixels are usable"
                                                                + Separator +
                                                                "space contrast test, warning, less than 99 percent are usable"
                                                                + Separator +
                                                                "space contrast test, cloudy"
                                                                + Separator +
                                                                "interchannel test, cloudy"));
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        initDataForComputing(reader, writer);
        int[] origin3D = {0, 0, 0};

        for (int z = 0; z < shape[0]; z++) {
            final boolean land = isLand(distanceToLandMap, lons.getDouble(z), lats.getDouble(z));
            final boolean iceCoveredWater = !land && isIceCoveredWater();
            final boolean water = !land && !iceCoveredWater;
            if (land || iceCoveredWater) {
                origin3D[0] = z;
                final Array domainData_11_1 = data11_1.section(origin3D, levelShape);
                final MaximumAndFlags mf = getMaximumAndFlags(domainData_11_1, fillValue_11_1, 1);
                final double spaceContrastThreshold = mf.maximum - DELTA_1_LAND_OR_ICE_COVERED;
                index.set0(z);
                for (int y = 0; y < shape[1]; y++) {
                    index.set1(y);
                    for (int x = 0; x < shape[2]; x++) {
                        index.set2(x);
                        byte flagsByte = mf.flags;
                        flagsByte += getCloudy_SpaceContrastTest(spaceContrastThreshold);
                        flagsByte += getCloudy_InterChannelTest();
                        flags.setByte(index, flagsByte);
                    }
                }
            } else {
                String fileName = "";
                String sensorType = "hirs-n18";
                String processingVersion = "";
                Reader srcReader = readerCache.getFileOpened(fileName, sensorType, processingVersion);
            }
        }
        writer.write(varFlags, flags);
    }

    static MaximumAndFlags getMaximumAndFlags(Array domainData, float fillValue, int maxNumInvalidPixels) {
        final IndexIterator iterator = domainData.getIndexIterator();
        int invalidCount = 0;
        float max = fillValue;
        while (iterator.hasNext()) {
            final float v = iterator.getFloatNext();
            if (v == fillValue) {
                invalidCount++;
            } else {
                max = Math.max(max, v);
            }
        }
        byte flags = 0;
        if (invalidCount == 0) { // all pixels are valid
            flags = SPACE_CONTRAST_TEST_ALL_PIXELS_USABLE;
        } else if (invalidCount > maxNumInvalidPixels) { // warning flag
            flags = SPACE_CONTRAST_TEST_WARNING;
        }
        return new MaximumAndFlags(max, flags);
    }

    static boolean isLand(DistanceToLandMap distanceToLandMap, double lon, double lat) throws IOException {
        final double distanceToLand = distanceToLandMap.getDistance(lon, lat);
        return distanceToLand < 0.3;
    }

    static boolean isIceCoveredWater() {
        //@todo 1 se/** - the "ice covered" check must be implemented. Ask UHH (Imke Hans or Martin Burgdorf)
        return false;
    }

    private byte getCloudy_SpaceContrastTest(double spaceContrastThreshold) {
        final float bt11_1 = data11_1.getFloat(index);
        boolean usable = bt11_1 != fillValue_11_1;
        if (usable && spaceContrastThreshold > bt11_1) {
            return SPACE_CONTRAST_TEST_CLOUDY;
        }
        return 0;
    }

    private byte getCloudy_InterChannelTest() {
        final float bt11_1 = data11_1.getFloat(index);
        final float bt6_5 = data6_5.getFloat(index);
        boolean usable = bt11_1 != fillValue_11_1 && bt6_5 != fillValue_6_5;
        if (usable && bt11_1 - bt6_5 < 25) {
            return INTERCHANNEL_TEST_CLOUDY;
        }
        return 0;
    }

    private int[] initDataForComputing(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        readerCache = new CloudRC(getContext());

        var11_1µm = getVariable(writer, btVarName_11_1_µm);
        fillValue_11_1 = getFloatValueFromAttribute(var11_1µm, "_FillValue", 0);

        var6_5µm = getVariable(writer, btVarName_6_5_µm);
        fillValue_6_5 = getFloatValueFromAttribute(var6_5µm, "_FillValue", 0);

        varFlags = getVariable(writer, flagVarName);

        sourceFileVar = getVariable(writer, sourceFileVarName);

        shape = var11_1µm.getShape();
        levelShape = shape.clone();
        levelShape[0] = 1;

        data11_1 = var11_1µm.read();
        data6_5 = var6_5µm.read();
        flags = varFlags.read();
        index = flags.getIndex();

        lats = getCenterPosArrayFromMMDFile(reader, latVarName, null, null, Constants.MATCHUP_COUNT);
        lons = getCenterPosArrayFromMMDFile(reader, lonVarName, null, null, Constants.MATCHUP_COUNT);
        return shape;
    }

    static class MaximumAndFlags {

        final double maximum;
        final byte flags;

        MaximumAndFlags(double maximum, byte flags) {
            this.maximum = maximum;
            this.flags = flags;
        }
    }

    static class CloudRC extends ReaderCache {

        public CloudRC(final PostProcessingContext context) {
            super(context);
        }

        @Override
        protected int[] extractYearMonthDayFromFilename(String fileName) {
            final String[] strings = fileName.split("\\.");
            final String datePart = strings[4].substring(1);
            final Date yyDDD = TimeUtils.parse(datePart, "yyDDD");
            final Calendar utcCalendar = TimeUtils.getUTCCalendar();
            utcCalendar.setTime(yyDDD);
            return new int[] {
            utcCalendar.get(Calendar.YEAR),
            utcCalendar.get(Calendar.MONTH) +1,
            utcCalendar.get(Calendar.DAY_OF_MONTH),
            };
        }
    }
}