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

package com.bc.fiduceo.geometry;


import java.util.List;

public class GeometryUtil {

    public static String toKml(Polygon polygon) {
        final StringBuilder builder = new StringBuilder();

        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
        builder.append("<Document>\n");
        builder.append("  <Style id=\"polygonStyle\">\n");
        builder.append("    <LineStyle>\n");
        builder.append("      <color>7f00ffff</color>\n");
        builder.append("      <width>1</width>\n");
        builder.append("    </LineStyle>\n");
        builder.append("    <PolyStyle>\n");
        builder.append("      <color>7f00ff00</color>\n");
        builder.append("    </PolyStyle>\n");
        builder.append("  </Style>\n");
        builder.append("  <Placemark>\n");
        builder.append("  <name>default_name</name>\n");
        builder.append("  <styleUrl>#polygonStyle</styleUrl>\n");
        builder.append("  <Polygon>\n");
        builder.append("    <altitudeMode>clampToGround</altitudeMode>\n");
        builder.append("    <outerBoundaryIs>\n");
        builder.append("      <LinearRing>\n");
        builder.append("        <coordinates>\n");
        final Point[] coordinates = polygon.getCoordinates();
        for (final Point coordinate : coordinates) {
            builder.append("          ");
            builder.append(coordinate.getLon());
            builder.append(",");
            builder.append(coordinate.getLat());
            builder.append(",0\n");
        }
        builder.append("        </coordinates>\n");
        builder.append("      </LinearRing>\n");
        builder.append("    </outerBoundaryIs>\n");
        builder.append("  </Polygon>\n");
        builder.append("  </Placemark>\n");
        builder.append("</Document>\n");
        builder.append("</kml>");

        return builder.toString();
    }

    // @todo 2 tb/mb this functionality should not be necessary. Find usages and replace with multiPolygon - then use geometry factory to format the geometry 2006-03-10
    public static String formatPolygonListToMultiPolygon(List<Polygon> polygonList) {
        final StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("MULTIPOLYGON(");

        for (int j = 0; j < polygonList.size(); j++) {
            Polygon polygon = polygonList.get(j);
            final Point[] points = polygon.getCoordinates();
            stringBuffer.append("((");
            for (int i = 0; i < points.length; i++) {
                Point coordinate = points[i];
                stringBuffer.append(coordinate.getLon());
                stringBuffer.append(" ");
                stringBuffer.append(coordinate.getLat());
                if (i < points.length - 1) {
                    stringBuffer.append(",");
                }
            }
            stringBuffer.append("))");
            if (j < polygonList.size() - 1) {
                stringBuffer.append(",");
            }
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    // @todo 2 tb/tb reconsider if this is the right location for the method 2016-03-10
    public static String formatToMultipoint(Geometry geometry) {
        final Point[] coordinates = geometry.getCoordinates();

        final StringBuilder builder = new StringBuilder();
        builder.append("MULTIPOINT(");
        for (int i = 0; i < coordinates.length; i++) {
            final Point coordinate = coordinates[i];
            builder.append(coordinate.getLon());
            builder.append(" ");
            builder.append(coordinate.getLat());
            if (i < coordinates.length - 1) {
                builder.append(",");
            }
        }
        builder.append(")");

        return builder.toString();
    }
}
