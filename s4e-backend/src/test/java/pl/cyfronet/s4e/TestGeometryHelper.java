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
            // 108m
            // lat/long
            polygon = reader.read("POLYGON((18.981890089820652 -36.95919177442794,74.10811836891402 -36.95919177442794,74.10811836891402 57.037586807964416,18.981890089820652 57.037586807964416,18.981890089820652 -36.95919177442794))");
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
