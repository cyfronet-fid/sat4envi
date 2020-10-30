package pl.cyfronet.s4e.geoserver.op;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.IntegrationTest;
import pl.cyfronet.s4e.TestDbHelper;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@IntegrationTest
@Slf4j
public class GeoServerOperationsIntegrationTest {
    @Autowired
    private GeoServerOperations geoServerOperations;

    @Autowired
    private SeedProductsTest seedProductsTest;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
        for (String workspace: geoServerOperations.listWorkspaces()) {
            geoServerOperations.deleteWorkspace(workspace, true);
        }
    }

    @Test
    public void shouldCreateWorkspace() {
        assertThat(geoServerOperations.listWorkspaces(), hasSize(0));

        geoServerOperations.createWorkspace("test");

        assertThat(geoServerOperations.listWorkspaces(), contains("test"));
    }

    @Test
    public void shouldDeleteWorkspace() {
        geoServerOperations.createWorkspace("test");

        assertThat(geoServerOperations.listWorkspaces(), contains("test"));

        geoServerOperations.deleteWorkspace("test", false);

        assertThat(geoServerOperations.listWorkspaces(), hasSize(0));
    }

    @Test
    public void shouldCreateExternalShpDataStore() {
        geoServerOperations.createWorkspace("test");

        assertThat(geoServerOperations.listDataStores("test"), hasSize(0));

        geoServerOperations.createExternalShpDataStore("test", "dataStoreName", "file://"+ Constants.GEOSERVER_PRG_PATH);

        assertThat(geoServerOperations.listDataStores("test"), contains("dataStoreName"));
        List<String> featureTypes = geoServerOperations.listDataStoreFeatureTypes("test", "dataStoreName");
        assertThat(
                "In case this assertion fails the PRG store baked into GeoServer image has to be verified, and" +
                        " all the PRG layer logic updated as required",
                featureTypes,
                containsInAnyOrder(
                        "Panstwo",
                        "gminy",
                        "jednostki_ewidencyjne",
                        "obreby_ewidencyjne",
                        "powiaty",
                        "wojewodztwa"
                )
        );
    }

    @Test
    public void shouldCreateS3CoverageStore() {
        seedProductsTest.prepareDb();
        geoServerOperations.createWorkspace("test");

        assertThat(geoServerOperations.listCoverageStores("test"), hasSize(0));

        geoServerOperations.createS3CoverageStore("test", "setvak");

        assertThat(geoServerOperations.listCoverageStores("test"), contains("setvak"));
    }

    @Test
    public void shouldDeleteCoverageStore() {
        seedProductsTest.prepareDb();
        geoServerOperations.createWorkspace("test");
        geoServerOperations.createS3CoverageStore("test", "setvak");

        assertThat(geoServerOperations.listCoverageStores("test"), contains("setvak"));

        geoServerOperations.deleteCoverageStore("test", "setvak", false);

        assertThat(geoServerOperations.listCoverageStores("test"), hasSize(0));
    }

    @Test
    public void shouldCreateS3Coverage() {
        seedProductsTest.prepareDb();
        geoServerOperations.createWorkspace("test");
        geoServerOperations.createS3CoverageStore("test", "setvak");

        assertThat(geoServerOperations.listCoverages("test", "setvak"), hasSize(0));

        geoServerOperations.createS3Coverage("test", "setvak", "setvak");

        assertThat(geoServerOperations.listCoverages("test", "setvak"), contains("setvak"));
    }

    @Test
    public void shouldDeleteCoverage() {
        seedProductsTest.prepareDb();
        geoServerOperations.createWorkspace("test");
        geoServerOperations.createS3CoverageStore("test", "setvak");
        geoServerOperations.createS3Coverage("test", "setvak", "setvak");

        assertThat(geoServerOperations.listCoverages("test", "setvak"), contains("setvak"));

        geoServerOperations.deleteCoverage("test", "setvak", "setvak");

        assertThat(geoServerOperations.listCoverages("test", "setvak"), hasSize(0));
    }

    @Nested
    class TileLayers {
        @BeforeEach
        public void beforeEach() {
            seedProductsTest.prepareDb();
            geoServerOperations.createWorkspace("test");
            geoServerOperations.createS3CoverageStore("test", "setvak");
            geoServerOperations.createS3Coverage("test", "setvak", "setvak");
        }

        @Test
        public void shouldCreateAndDeleteTileLayer() {
            assertThat(geoServerOperations.tileLayerExists("test", "setvak"), is(false));

            geoServerOperations.createTileLayer("test", "setvak");

            assertThat(geoServerOperations.tileLayerExists("test", "setvak"), is(true));

            geoServerOperations.deleteTileLayer("test", "setvak");

            assertThat(geoServerOperations.tileLayerExists("test", "setvak"), is(false));
        }
    }

    @Test
    public void shouldSetLayerDefaultStyle() {
        geoServerOperations.createWorkspace("test");
        geoServerOperations.createStyle("test", "styleOne");
        geoServerOperations.uploadSld("test", "styleOne", "styleOne");
        geoServerOperations.createExternalShpDataStore("test", "dataStoreName", "file://"+ Constants.GEOSERVER_PRG_PATH);

        assertThat(geoServerOperations.listStyles("test"), contains("styleOne"));
        assertThat(geoServerOperations.listDataStores("test"), contains("dataStoreName"));
        assertThat(geoServerOperations.getLayer("test", "wojewodztwa").getLayer().getDefaultStyle().getName(), not(equalTo("test:styleOne")));

        geoServerOperations.setLayerDefaultStyle("test", "wojewodztwa", "styleOne");

        assertThat(geoServerOperations.getLayer("test", "wojewodztwa").getLayer().getDefaultStyle().getName(), is(equalTo("test:styleOne")));
    }

    @Test
    public void shouldCreateStyle() {
        geoServerOperations.createWorkspace("test");

        assertThat(geoServerOperations.listStyles("test"), hasSize(0));

        geoServerOperations.createStyle("test", "styleOne");

        assertThat(geoServerOperations.listStyles("test"), contains("styleOne"));
    }

    @Test
    public void shouldUploadStyleSld() {
        geoServerOperations.createWorkspace("test");
        geoServerOperations.createStyle("test", "styleOne");

        assertThat(geoServerOperations.listStyles("test"), contains("styleOne"));

        geoServerOperations.uploadSld("test", "styleOne", "styleOne");

        assertThat(geoServerOperations.listStyles("test"), contains("styleOne"));
    }

    @Test
    public void shouldDeleteStyleSld() {
        geoServerOperations.createWorkspace("test");
        geoServerOperations.createStyle("test", "styleOne");
        geoServerOperations.uploadSld("test", "styleOne", "styleOne");

        assertThat(geoServerOperations.listStyles("test"), contains("styleOne"));

        geoServerOperations.deleteStyle("test", "styleOne");

        assertThat(geoServerOperations.listStyles("test"), hasSize(0));
    }
}
