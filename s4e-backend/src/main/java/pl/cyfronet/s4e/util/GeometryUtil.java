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
}
