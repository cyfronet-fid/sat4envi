package pl.cyfronet.s4e.util;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeometryUtil {
    public Geometry parseWKT(String wkt) throws ParseException {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 3857);
        WKTReader reader = new WKTReader(factory);
        return reader.read(wkt);
    }
}
