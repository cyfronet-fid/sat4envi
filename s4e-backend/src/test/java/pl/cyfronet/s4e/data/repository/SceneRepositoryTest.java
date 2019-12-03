package pl.cyfronet.s4e.data.repository;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.bean.ProductType;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@BasicTest
class SceneRepositoryTest {

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @BeforeEach
    void setUp() {
        reset();
    }

    @AfterEach
    void tearDown() {
        reset();
    }

    private void reset() {
        sceneRepository.deleteAll();
        productTypeRepository.deleteAll();
    }

    @Test
    void findDatesWithData() {
        val productType = productTypeRepository.save(ProductType.builder()
                .name("108m")
                .description("sth")
                .build());

        /*
        We create data for the days marked with * and we will extract availability for October.
           Sep | Oct     | Nov
              *|**      *|*
         */
        val scenes = List.of(
                Scene.builder()
                        .productType(productType)
                        .layerName("testLayerName0")
                        .timestamp(LocalDateTime.of(2019, 9, 30, 23, 59, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .productType(productType)
                        .layerName("testLayerName1")
                        .timestamp(LocalDateTime.of(2019, 10, 1, 0, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .productType(productType)
                        .layerName("testLayerName2")
                        .timestamp(LocalDateTime.of(2019, 10, 2, 0, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .productType(productType)
                        .layerName("testLayerName3")
                        .timestamp(LocalDateTime.of(2019, 10, 2, 1, 0))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .productType(productType)
                        .layerName("testLayerName4")
                        .timestamp(LocalDateTime.of(2019, 10, 31, 23, 59, 59))
                        .s3Path("some/path")
                        .build(),
                Scene.builder()
                        .productType(productType)
                        .layerName("testLayerName5")
                        .timestamp(LocalDateTime.of(2019, 11, 1, 0, 0))
                        .s3Path("some/path")
                        .build()
        );
        sceneRepository.saveAll(scenes);

        List<Date> datesWithData = sceneRepository.findDatesWithData(productType.getId(), LocalDateTime.of(2019, 10, 1, 0, 0), LocalDateTime.of(2019, 11, 1, 0, 0));

        List<LocalDate> datesWithDataConverted = datesWithData.stream()
                .map(Date::toLocalDate)
                .collect(Collectors.toList());

        assertThat(datesWithDataConverted, contains(LocalDate.of(2019, 10, 1), LocalDate.of(2019, 10, 2), LocalDate.of(2019, 10, 31)));
    }
}
