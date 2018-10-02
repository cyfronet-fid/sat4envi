package pl.cyfronet.s4e.granules;

import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class GranuleService {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2018, 10, 2, 10, 0);
    private static final String LAYER_NAME = "test:201807051330_PL_HRV_gtif_mercator";

    public List<Granule> getGranules(Long productId) throws NotFoundException {
        if (productId == 1L) {
            return Arrays.asList(new Granule[] {
                    Granule.builder()
                            .id(1L)
                            .productId(productId)
                            .timestamp(BASE_TIME)
                            .layerName(LAYER_NAME)
                            .build(),
                    Granule.builder()
                            .id(2L)
                            .productId(productId)
                            .timestamp(BASE_TIME.minusHours(1))
                            .layerName(LAYER_NAME)
                            .build(),
                    Granule.builder()
                            .id(3L)
                            .productId(productId)
                            .timestamp(BASE_TIME.minusHours(2))
                            .layerName(LAYER_NAME)
                            .build(),
            });
        } else if (productId == 2L) {
            return Arrays.asList(new Granule[] {
                    Granule.builder()
                            .id(4L)
                            .productId(productId)
                            .timestamp(BASE_TIME)
                            .layerName(LAYER_NAME)
                            .build(),
                    Granule.builder()
                            .id(5L)
                            .productId(productId)
                            .timestamp(BASE_TIME.minusHours(1))
                            .layerName(LAYER_NAME)
                            .build(),
                    Granule.builder()
                            .id(6L)
                            .productId(productId)
                            .timestamp(BASE_TIME.minusHours(2))
                            .layerName(LAYER_NAME)
                            .build(),
            });
        } else {
            throw new NotFoundException();
        }
    }
}
