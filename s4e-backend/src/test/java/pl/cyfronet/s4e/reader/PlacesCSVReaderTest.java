package pl.cyfronet.s4e.reader;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Place;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@BasicTest
class PlacesCSVReaderTest {
    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void shouldReadAllPlaces() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        resourceLoader.getResource("classpath:db/places-test.csv").getInputStream()));

        PlacesCSVReader reader = new PlacesCSVReader();
        List<Place> places = reader.readAll(bufferedReader);

        assertThat(places, hasSize(4));
    }
}
