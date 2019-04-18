package pl.cyfronet.s4e.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

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
