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

package pl.cyfronet.s4e.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.cyfronet.s4e.bean.Place;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class PlacesCSVReader {
    public List<Place> readAll(BufferedReader bufferedReader) throws IOException {
        List<Place> places = new ArrayList<>();

        int countCorrect = 0;
        int countIncorrect = 0;

        try {
            bufferedReader.readLine(); // skip the header

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split("\t");
                if (split.length != 5) {
                    countIncorrect++;
                    continue;
                }

                Place.PlaceBuilder builder = Place.builder();
                builder.name(split[0]);
                builder.type(split[1]);
                try {
                    builder.latitude(Double.parseDouble(split[2]));
                    builder.longitude(Double.parseDouble(split[3]));
                    builder.voivodeship(split[4]);

                    places.add(builder.build());
                    countCorrect++;
                } catch (NumberFormatException e) {
                    countIncorrect++;
                    log.error("Cannot parse longitude or latitude", e);
                }
            }
        } finally {
            log.debug("Correct rows: "+countCorrect+". Incorrect rows: "+countIncorrect);
        }

        return places;
    }
}
