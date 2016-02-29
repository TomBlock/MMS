
/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.google.common.geometry.S1Angle;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.List;

class BcS2Polygon implements Polygon {

    private final S2Polygon googlePolygon;

    BcS2Polygon(Object geometry) {
        this.googlePolygon = (S2Polygon) geometry;
    }

    public static ArrayList<Point> createS2Points(int numLoops, S2Polygon googlePolygon) {
        final ArrayList<Point> coordinates = new ArrayList<>();
        for (int i = 0; i < numLoops; i++) {
            final S2Loop loop = googlePolygon.loop(i);
            final int numVertices = loop.numVertices();
            for (int k = 0; k < numVertices; k++) {
                final S2Point googlePoint = loop.vertex(k);
                coordinates.add(new BcS2Point(new S2LatLng(googlePoint)));
            }
        }
        return coordinates;
    }

    @Override
    public Geometry getIntersection(Geometry other) {
        if (other instanceof BcS2Polygon) {
            final S2Polygon intersection = new S2Polygon();
            intersection.initToIntersection(googlePolygon, (S2Polygon) other.getInner());
            return new BcS2Polygon(intersection);
        } else if (other instanceof BcS2MultiLineString) {
            List<S2Polyline> s2PolylineList = (List<S2Polyline>) other.getInner();
            List<S2Polyline> intersectionResult = new ArrayList<>();
            for (final S2Polyline s2Polyline : s2PolylineList) {
                intersectionResult.addAll(googlePolygon.intersectWithPolyLine(s2Polyline));

            }
            return new BcS2MultiLineString(intersectionResult);
        }

        throw new RuntimeException("intersection type not implemented");
    }

    @Override
    public Polygon getDifference(Polygon polygon) {
        final S2Polygon difference = new S2Polygon();
        difference.initToDifference(googlePolygon, (S2Polygon) polygon.getInner());
        return new BcS2Polygon(difference);
    }

    @Override
    public Polygon getUnion(Polygon polygon) {
        final S2Polygon union = new S2Polygon();
        union.initToUnion(googlePolygon, (S2Polygon) polygon.getInner());
        return new BcS2Polygon(union);
    }

    @Override
    public boolean isEmpty() {
        return googlePolygon.numLoops() == 0;
    }

    @Override
    public void shiftLon(double lon) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isValid() {
        final int numLoops = googlePolygon.numLoops();
        final ArrayList<S2Loop> loops = new ArrayList<>();
        for (int i = 0; i < numLoops; i++) {
            final S2Loop loop = googlePolygon.loop(i);
            if (!loop.isValid()) {
                return false;
            }
            loops.add(loop);
        }

        return S2Polygon.isValid(loops);
    }

    @Override
    public Point[] getCoordinates() {
        final int numLoops = googlePolygon.numLoops();
        ArrayList<Point> pointArrayList = createS2Points(numLoops, googlePolygon);
        // @todo 2 tb/** the S2 loops do not contain the closing point. Check if we need to add this point here.
        // check what happens when the polygon contains more than one loop tb 2016-01-27
        return pointArrayList.toArray(new Point[pointArrayList.size()]);
    }

    @Override
    public String toString() {
        return googlePolygon.toString();
    }

    @Override
    public Object getInner() {
        return googlePolygon;
    }
}
