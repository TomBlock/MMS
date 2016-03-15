package com.bc.fiduceo.reader;

import static org.junit.Assert.*;

import com.bc.fiduceo.core.Interval;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

public class RawDataReaderTest_context2D_FirstDimensionIsOne_byte {

    private Interval windowSize;
    private Number fillValue;
    private byte fv;
    private Array rawArray;

    @Before
    public void setUp() throws Exception {
        windowSize = new Interval(3, 3);
        fillValue = -2;
        fv = fillValue.byteValue();
        rawArray = getByteRawArray();
    }

    @Test
    public void testWindowCenter() throws Exception {

        final Array array = RawDataReader.read(3, 3, windowSize, fillValue, rawArray, 10);

        assertNotNull(array);
        assertEquals(byte.class, array.getElementType());
        assertEquals(9, array.getSize());
        final byte[] expecteds = {2, 2, 2, 3, 3, 3, 4, 4, 4};
        final byte[] actuals = (byte[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testTopRightWindowOut() throws Exception {

        final Array array = RawDataReader.read(9, 0, windowSize, fillValue, rawArray, 10);

        assertNotNull(array);
        assertEquals(byte.class, array.getElementType());
        assertEquals(9, array.getSize());
        final byte[] expecteds = {fv, fv, fv, 0, 0, fv, 1, 1, fv};
        final byte[] actuals = (byte[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testTopLeftWindowOut() throws Exception {

        final Array array = RawDataReader.read(0, 0, windowSize, fillValue, rawArray, 10);

        assertNotNull(array);
        assertEquals(byte.class, array.getElementType());
        assertEquals(9, array.getSize());
        final byte[] expecteds = {fv, fv, fv, fv, 0, 0, fv, 1, 1};
        final byte[] actuals = (byte[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testBottomLeftWindowOut() throws Exception {
        final Array array = RawDataReader.read(0, 9, windowSize, fillValue, rawArray, 10);

        assertNotNull(array);
        assertEquals(byte.class, array.getElementType());
        assertEquals(9, array.getSize());
        final byte[] expecteds = {fv, 8, 8, fv, 9, 9, fv, fv, fv};
        final byte[] actuals = (byte[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testBottomRightWindowOut() throws Exception {
        final Array array = RawDataReader.read(9, 9, windowSize, fillValue, rawArray, 10);

        assertNotNull(array);
        assertEquals(byte.class, array.getElementType());
        assertEquals(9, array.getSize());
        final byte[] expecteds = {8, 8, fv, 9, 9, fv, fv, fv, fv};
        final byte[] actuals = (byte[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testRawArrayHasMoreThanTwoDimensions() throws InvalidRangeException {
        final Array rawArray = Array.factory(new byte[][][]{
                    {{11, 12, 13}, {14, 15, 16}, {17, 18, 19},},
                    {{21, 22, 23}, {24, 25, 26}, {27, 28, 29},},
                    {{31, 32, 33}, {34, 35, 36}, {37, 38, 39},}
        });

        try {
            RawDataReader.read(1, 1, new Interval(3, 3), -4d, rawArray, 10);
            fail("InvalidRangeException expected");
        } catch (InvalidRangeException expected) {
        }
    }


    @Test
    public void testRawArrayHasLessThanTwoDimensions() throws InvalidRangeException {
        final Array rawArray = Array.factory(new byte[]{11, 12, 13});

        try {
            RawDataReader.read(1, 1, new Interval(3, 3), -4d, rawArray, 10);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    private Array getByteRawArray() {
        final byte[][] bytes = new byte[1][];
        bytes[0] = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        return Array.factory(bytes);
    }
}