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

package pl.cyfronet.s4e.sync.step.metadata;

import jakarta.json.JsonObject;
import lombok.Builder;
import lombok.val;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;
import pl.cyfronet.s4e.sync.step.Step;
import pl.cyfronet.s4e.util.GeometryUtil;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.sync.Error.ERR_METADATA_FOOTPRINT_TRANSFORM_FAILED;

@Builder
public class IngestFootprint<T extends BaseContext> implements Step<T, Error> {
    public static final String METADATA_FOOTPRINT_PROPERTY = "polygon";

    private final Supplier<GeometryUtil> geometryUtil;

    private final Function<T, JsonObject> metadataJson;
    private final BiConsumer<T, Geometry> update;

    @Override
    public Error apply(T context) {
        val error = context.getError();
        GeometryUtil geometryUtil = this.geometryUtil.get();

        JsonObject metadataJson = this.metadataJson.apply(context);

        String polygonString = metadataJson.getString(METADATA_FOOTPRINT_PROPERTY);
        Coordinate[] shell = geometryUtil.parseCoordinatesFromMetadata(polygonString);
        Coordinate[] closedShell = geometryUtil.closeCoords(shell);
        Geometry polygon = geometryUtil.createPolygon(closedShell, GeometryUtil.FACTORY_3857);
        Geometry polygon3857;
        try {
            polygon3857 = geometryUtil.transform(polygon, "EPSG:4326", "EPSG:3857");
        } catch (FactoryException e) {
            throw new IllegalStateException(e);
        } catch (TransformException e) {
            return error.code(ERR_METADATA_FOOTPRINT_TRANSFORM_FAILED).cause(e)
                    .parameter("metadata_polygon", polygonString).build();
        }

        update.accept(context, polygon3857);

        return null;
    }
}
