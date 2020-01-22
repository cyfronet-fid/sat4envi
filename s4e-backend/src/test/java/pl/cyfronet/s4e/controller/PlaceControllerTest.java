package pl.cyfronet.s4e.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Place;
import pl.cyfronet.s4e.data.repository.PlaceRepository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@AutoConfigureMockMvc
@BasicTest
class PlaceControllerTest {
    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        resetPlaces();
    }

    private void resetPlaces() {
        placeRepository.deleteAll();
        Place.PlaceBuilder builder = Place.builder()
                .type("miasto")
                .longitude(0.1)
                .latitude(0.2)
                .voivodeship("śląskie");
        placeRepository.save(builder.name("Nazwa1").build());
        placeRepository.save(builder.name("Nazwa2").build());
        placeRepository.save(builder.name("Nazwa3").build());
        placeRepository.save(builder.name("Na4").build());
        placeRepository.save(builder.name("Nazwa5").build());
        placeRepository.save(builder.name("AaaNazwa5").build());
    }

    @Test
    public void shouldReturnAllMatches() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1+"/places?namePrefix=Naz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.length()", is(equalTo(4))));
    }

    @Test
    public void shouldReturnAllMatchesCaseInsensitive() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1+"/places?namePrefix=naz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.length()", is(equalTo(4))));
    }

    @Test
    public void shouldPage() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1+"/places?namePrefix=Naz&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.length()", is(equalTo(2))))
                .andExpect(jsonPath("totalElements", is(equalTo(4))))
                .andExpect(jsonPath("totalPages", is(equalTo(2))));
    }

    @Test
    public void shouldSortAndPage() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1+"/places?namePrefix=Naz&size=2&sort=name&name.dir=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.length()", is(equalTo(2))))
                .andExpect(jsonPath("content[0].name", is(equalTo("Nazwa1"))))
                .andExpect(jsonPath("content[1].name", is(equalTo("Nazwa2"))));
    }

    @Test
    public void shouldShowSecondPageSorted() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1+"/places?namePrefix=Naz&size=2&page=1&sort=name&name.dir=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.length()", is(equalTo(2))))
                .andExpect(jsonPath("content[0].name", is(equalTo("Nazwa3"))))
                .andExpect(jsonPath("content[1].name", is(equalTo("Nazwa5"))));
    }
}
