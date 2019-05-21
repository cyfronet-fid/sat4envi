package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Place;

@Data
@Builder
public class PlaceResponse {
    private String name;
    private String type;
    private double latitude;
    private double longitude;
    private String voivodeship;

    public static PlaceResponse of(Place place) {
        return PlaceResponse.builder()
                .name(place.getName())
                .type(place.getType())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .voivodeship(place.getVoivodeship())
                .build();
    }
}
