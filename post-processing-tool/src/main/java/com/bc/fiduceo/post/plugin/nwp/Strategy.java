package com.bc.fiduceo.post.plugin.nwp;

import com.bc.fiduceo.core.TimeRange;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

abstract class Strategy {

    private static final int SEVENTY_TWO_HOURS_IN_SECONDS = 72 * 60 * 60;
    private static final int FOURTY_EIGHT_HOURS_IN_SECONDS = 48 * 60 * 60;

    abstract void prepare(Context context);

    abstract void compute(Context context) throws IOException, InvalidRangeException;

    abstract File writeGeoFile(Context context) throws IOException, InvalidRangeException;

    // package access for testing only tb 2017-01-06
    static List<String> toDirectoryNamesList(TimeRange timeRange) {
        final Date startDate = timeRange.getStartDate();
        final Date extractStartDate = TimeUtils.addSeconds(-SEVENTY_TWO_HOURS_IN_SECONDS, startDate);
        final Date beginningOfDay = TimeUtils.getBeginningOfDay(extractStartDate);

        final Date stopDate = timeRange.getStopDate();
        final Date extractStopDate = TimeUtils.addSeconds(FOURTY_EIGHT_HOURS_IN_SECONDS, stopDate);

        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(beginningOfDay);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        final List<String> directoryNameList = new ArrayList<>();
        while (!utcCalendar.getTime().after(extractStopDate)) {
            directoryNameList.add(simpleDateFormat.format(utcCalendar.getTime()));
            utcCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return directoryNameList;
    }

    // package access for testing only tb 2017-01-06
    static TimeRange extractTimeRange(Array timesArray, Number fillValue) {
        int startTimeSeconds = Integer.MAX_VALUE;
        int endTimeSeconds = Integer.MIN_VALUE;
        final int fill = fillValue.intValue();

        for (int i = 0; i < timesArray.getSize(); i++) {
            final int currentTime = timesArray.getInt(i);
            if (currentTime == fill) {
                continue;
            }

            if (currentTime > endTimeSeconds) {
                endTimeSeconds = currentTime;
            }
            if (currentTime < startTimeSeconds) {
                startTimeSeconds = currentTime;
            }
        }

        final Date startDate = TimeUtils.create(startTimeSeconds * 1000L);
        final Date endDate = TimeUtils.create(endTimeSeconds * 1000L);
        return new TimeRange(startDate, endDate);
    }
}
