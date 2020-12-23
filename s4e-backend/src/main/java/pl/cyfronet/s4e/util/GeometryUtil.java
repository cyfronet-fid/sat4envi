/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.util;

import lombok.extern.slf4j.Slf4j;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class GeometryUtil {
    public static GeometryFactory FACTORY_3857 = new GeometryFactory(new PrecisionModel(), 3857);
    public static GeometryFactory FACTORY_4326 = new GeometryFactory(new PrecisionModel(), 4326);

    private static final String COORDINATE_FORMAT = "%.5f";

    public Geometry parseWKT(String wkt, GeometryFactory factory) throws ParseException {
        WKTReader reader = new WKTReader(factory);
        return reader.read(wkt);
    }

    public Coordinate[] parseCoordinatesFromMetadata(String value) {
        String[] elements = value.split(" ");
        Coordinate[] coords = new Coordinate[elements.length];
        for (int i = 0; i < elements.length; i++) {
            String[] split = elements[i].split(",", 2);
            double lat = Double.parseDouble(split[0]);
            double lon = Double.parseDouble(split[1]);
            coords[i] = new Coordinate(lat, lon);
        }
        return coords;
    }

    public Coordinate[] closeCoords(Coordinate[] coords) {
        if (coords == null || coords.length < 3 || coords[0].equals(coords[coords.length - 1])) {
            return coords;
        }
        Coordinate[] corrected = Arrays.copyOf(coords, coords.length + 1);
        corrected[coords.length] = coords[0];
        return corrected;
    }

    public Geometry createPolygon(Coordinate[] shell, GeometryFactory factory) {
        LinearRing linearRing = factory.createLinearRing(shell);
        if (!linearRing.isSimple()) {
            throw new IllegalArgumentException("Shell is not simple");
        }
        return factory.createPolygon(linearRing);
    }

    public Geometry transform(Geometry geometry, String from, String to) throws FactoryException, TransformException {
        CoordinateReferenceSystem toCRS = CRS.decode(to);
        CoordinateReferenceSystem fromCRS = CRS.decode(from);
        MathTransform transform = CRS.findMathTransform(fromCRS, toCRS);
        return JTS.transform(geometry, transform);
    }

    public String toWkt(Geometry geometry) {
        if (!(geometry instanceof Polygon)) {
            throw new IllegalArgumentException("Passed geometry must be an instance of Polygon");
        }
        Polygon polygon = (Polygon) geometry;
        CoordinateSequence shell = polygon.getExteriorRing().getCoordinateSequence();
        StringBuilder sb = new StringBuilder();
        sb.append("POLYGON((");
        for (int i = 0; i < shell.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            // Append exactly: "<lat> <long>", as WKT assumes long/lat axis ordering (contrary to JTS).
            sb
                    .append(String.format(COORDINATE_FORMAT, shell.getY(i)))
                    .append(' ')
                    .append(String.format(COORDINATE_FORMAT, shell.getX(i)));
        }
        sb.append("))");
        return sb.toString();
    }
}
