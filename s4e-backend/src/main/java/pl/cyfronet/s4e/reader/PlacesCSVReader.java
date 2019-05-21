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
