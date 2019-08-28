package pl.cyfronet.s4e.db.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.Place;
import pl.cyfronet.s4e.data.repository.PlaceRepository;
import pl.cyfronet.s4e.reader.PlacesCSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Profile({"development", "run-seed-places"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedPlaces implements ApplicationRunner {
    private final PlaceRepository placeRepository;
    private final ResourceLoader resourceLoader;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        log.info("Reading places...");

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        resourceLoader.getResource("classpath:db/places.csv").getInputStream()))) {
            List<Place> places = new PlacesCSVReader().readAll(bufferedReader);

            placeRepository.deleteAll();
            placeRepository.saveAll(places);
            log.info("Registered "+places.size()+" places");
        } catch (IOException e) {
            log.error("Couldn't read source file", e);
        }
    }
}
