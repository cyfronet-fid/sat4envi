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

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeometryUtilTest {
    private static GeometryUtil geometryUtil = new GeometryUtil();

    @Test
    public void parseShouldWork() {
        String value = "55.612087,23.550383 56.030285,19.432180 57.522560,19.852150 57.099194,24.133862 55.612087,23.550383";
        Coordinate[] coordinates = geometryUtil.parseCoordinatesFromMetadata(value);

        assertThat(coordinates, arrayWithSize(5));
        assertThat(coordinates[0].getX(), equalTo(Double.parseDouble("55.612087")));
        assertThat(coordinates[0].getY(), equalTo(Double.parseDouble("23.550383")));
    }

    @Test
    public void parseShouldThrowNumberFormatException() {
        String value = "55.612087,23.550383,sth";
        assertThrows(NumberFormatException.class, () -> geometryUtil.parseCoordinatesFromMetadata(value));
    }

    @Test
    public void closeCoordsShouldReturnSameIfCorrect() {
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0,0),
                new Coordinate(0,1),
                new Coordinate(1,0),
                new Coordinate(0,0)
        };

        assertThat(geometryUtil.closeCoords(coords), sameInstance(coords));
    }

    @Test
    public void closeCoordsShouldCloseIfOpen() {
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0,0),
                new Coordinate(0,1),
                new Coordinate(1,0)
        };

        assertThat(geometryUtil.closeCoords(coords), equalTo(new Coordinate[] {
                new Coordinate(0,0),
                new Coordinate(0,1),
                new Coordinate(1,0),
                new Coordinate(0,0)
        }));
    }

    @Test
    public void createPolygonShouldWork() {
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0,0),
                new Coordinate(0,1),
                new Coordinate(1,0),
                new Coordinate(0,0)
        };

        Geometry polygon = geometryUtil.createPolygon(coords, GeometryUtil.FACTORY_3857);
        assertThat(polygon.isValid(), is(true));
    }

    @Test
    public void createPolygonShouldThrowIAEIfShellNotClosed() {
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0,0),
                new Coordinate(0,1),
                new Coordinate(1,0)
        };

        assertThrows(IllegalArgumentException.class, () -> geometryUtil.createPolygon(coords, GeometryUtil.FACTORY_3857));
    }

    @Test
    public void createPolygonShouldThrowIAEIfShellNotSimple() {
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(0,0),
                new Coordinate(1,1),
                new Coordinate(1,0),
                new Coordinate(0,1),
                new Coordinate(0,0)
        };

        assertThrows(IllegalArgumentException.class, () -> geometryUtil.createPolygon(coords, GeometryUtil.FACTORY_3857));
    }
}
