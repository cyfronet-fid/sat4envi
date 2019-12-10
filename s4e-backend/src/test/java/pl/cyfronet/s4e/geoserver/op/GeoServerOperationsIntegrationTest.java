package pl.cyfronet.s4e.geoserver.op;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@BasicTest
@Tag("integration")
public class GeoServerOperationsIntegrationTest {

    @Autowired
    private GeoServerOperations geoServerOperations;

    @BeforeEach
    public void beforeEach() {
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
        geoServerOperations.createWorkspace("test");

        assertThat(geoServerOperations.listCoverageStores("test"), hasSize(0));

        geoServerOperations.createS3CoverageStore("test", "covDataStoreName", "cyfro://blahblah");

        assertThat(geoServerOperations.listCoverageStores("test"), contains("covDataStoreName"));
    }

    @Test
    public void shouldDeleteCoverageStore() {
        geoServerOperations.createWorkspace("test");
        geoServerOperations.createS3CoverageStore("test", "covDataStoreName", "cyfro://blahblah");

        assertThat(geoServerOperations.listCoverageStores("test"), contains("covDataStoreName"));

        geoServerOperations.deleteCoverageStore("test", "covDataStoreName", false);

        assertThat(geoServerOperations.listCoverageStores("test"), hasSize(0));
    }

    @Test
    public void shouldCreateS3Coverage() {
        geoServerOperations.createWorkspace("test");
        geoServerOperations.createS3CoverageStore("test", "covDataStoreName", "cyfro://s4e-test-1/201810040000_Merkator_Europa_ir_108_setvak.tif");

        assertThat(geoServerOperations.listCoverages("test", "covDataStoreName"), hasSize(0));

        geoServerOperations.createS3Coverage("test", "covDataStoreName", "covName");

        assertThat(geoServerOperations.listCoverages("test", "covDataStoreName"), contains("covName"));
    }

    @Test
    public void shouldDeleteCoverage() {
        geoServerOperations.createWorkspace("test");
        geoServerOperations.createS3CoverageStore("test", "covDataStoreName", "cyfro://s4e-test-1/201810040000_Merkator_Europa_ir_108_setvak.tif");
        geoServerOperations.createS3Coverage("test", "covDataStoreName", "covName");

        assertThat(geoServerOperations.listCoverages("test", "covDataStoreName"), contains("covName"));

        geoServerOperations.deleteCoverage("test", "covDataStoreName", "covName");

        assertThat(geoServerOperations.listCoverages("test", "covDataStoreName"), hasSize(0));
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
