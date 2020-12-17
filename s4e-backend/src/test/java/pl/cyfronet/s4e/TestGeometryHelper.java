package pl.cyfronet.s4e;

import lombok.extern.slf4j.Slf4j;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

@Slf4j
public class TestGeometryHelper {
    public static final Geometry ANY_POLYGON;
    static {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 3857);
        WKTReader reader = new WKTReader(factory);
        Geometry polygon = null;
        try {
            // Here we specifically use long/lat, as the JTS expects the EPSG:4326 coords to be in that order,
            // and it swaps the coords when transforming to EPSG:3857.
            polygon = reader.read("POLYGON((55 12.6,55.25 19,55 26,47.55 24.93,47.67 19,47.5 14,55 12.6))");
            polygon = transform(polygon, "EPSG:4326", "EPSG:3857");
        } catch (ParseException | FactoryException | TransformException e) {
            log.warn("Unexpected", e);
        }
        ANY_POLYGON = polygon;
    }

    public static Geometry transform(Geometry g, String from, String to) throws FactoryException, TransformException {
        CoordinateReferenceSystem toCRS = CRS.decode(to);
        CoordinateReferenceSystem fromCRS = CRS.decode(from);
        MathTransform transform = CRS.findMathTransform(fromCRS, toCRS);
        return JTS.transform(g, transform);
    }
}
