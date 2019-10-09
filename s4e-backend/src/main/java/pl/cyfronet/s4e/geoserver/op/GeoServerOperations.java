package pl.cyfronet.s4e.geoserver.op;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.cyfronet.s4e.geoserver.op.request.*;
import pl.cyfronet.s4e.geoserver.op.response.LayerResponse;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoServerOperations {
    @Value("${geoserver.username}")
    private String geoserverUsername;
    @Value("${geoserver.password}")
    private String geoserverPassword;
    @Value("${geoserver.baseUrl}")
    private String geoserverBaseUrl;

    @Value("${geoserver.timeout.connect:60}")
    private Long geoserverTimeoutConnect;
    @Value("${geoserver.timeout.read:60}")
    private Long geoserverTimeoutRead;

    private final RestTemplateBuilder restTemplateBuilder;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private RestTemplate restTemplate() {
        return restTemplateBuilder
                .basicAuthentication(geoserverUsername, geoserverPassword)
                .setConnectTimeout(Duration.ofSeconds(geoserverTimeoutConnect))
                .setReadTimeout(Duration.ofSeconds(geoserverTimeoutRead))
                .build();
    }

    private <T> HttpEntity<T> httpEntity(T payload) {
        return httpEntity(payload, MediaType.APPLICATION_JSON_VALUE);
    }

    private <T> HttpEntity<T> httpEntity(T payload, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", contentType);
        return new HttpEntity<>(payload, headers);
    }

    private List<String> list(String type, URI url) {
        String res = restTemplate().getForObject(url, String.class);
        /*
         * Example response:
         * {"workspaces":{"workspace":[{"name":"test","href":"http:\/\/localhost:8080\/geoserver\/rest\/workspaces\/test.json"}]}}
         */
        List<String> out = new ArrayList<>();
        try {
            JsonNode tree = objectMapper.readTree(res);
            for (val node: tree.path(type+"s").path(type)) {
                out.add(node.path("name").asText());
            }
        } catch (IOException e) {
            log.warn("Exception when reading response", e);
        }
        return out;
    }


    public List<String> listWorkspaces() {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces")
                .build().toUri();
        return list("workspace", url);
    }

    public void createWorkspace(String workspace) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces")
                .build().toUri();
        val entity = httpEntity(new CreateWorkspaceRequest(workspace));

        restTemplate().postForObject(url, entity, String.class);
    }

    public void deleteWorkspace(String workspace, boolean recurse) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}")
                .queryParam("recurse", recurse)
                .buildAndExpand(workspace).toUri();
        restTemplate().delete(url);
    }


    public List<String> listDataStores(String workspace) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/datastores")
                .buildAndExpand(workspace).toUri();
        return list("dataStore", url);
    }

    /**
     *
     * @param workspace
     * @param dataStoreName
     * @param path the path to shapefiles, including the "file://" prefix
     */
    public void createExternalShpDataStore(String workspace, String dataStoreName, String path) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/datastores/{dataStoreName}/external.shp")
                .queryParam("configure", "all")
                .buildAndExpand(workspace, dataStoreName).toUri();
        val entity = httpEntity(path, "text/plain");
        restTemplate().put(url, entity);
    }


    public List<String> listCoverageStores(String workspace) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores")
                .buildAndExpand(workspace).toUri();
        return list("coverageStore", url);
    }

    public void createS3CoverageStore(String workspace, String coverageStore, String s3url) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores")
                .buildAndExpand(workspace).toUri();
        val entity = httpEntity(new CreateS3CoverageStoreRequest(workspace, coverageStore, s3url));
        restTemplate().postForObject(url, entity, String.class);
    }

    public void deleteCoverageStore(String workspace, String coverageStore, boolean recurse) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores/{coveragestore}")
                .queryParam("recurse", recurse)
                .buildAndExpand(workspace, coverageStore).toUri();
        restTemplate().delete(url);
    }


    public List<String> listDataStoreFeatureTypes(String workspace, String dataStoreName) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/datastores/{dataStoreName}/featuretypes")
                .buildAndExpand(workspace, dataStoreName).toUri();
        return list("featureType", url);
    }


    public List<String> listCoverages(String workspace, String coverageStore) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores/{coveragestore}/coverages")
                .buildAndExpand(workspace, coverageStore).toUri();
        return list("coverage", url);
    }

    public void createS3Coverage(String workspace, String coverageStore, String coverage) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores/{coveragestore}/coverages")
                .buildAndExpand(workspace, coverageStore).toUri();
        val entity = httpEntity(new CreateS3CoverageRequest(workspace, coverageStore, coverage));
        restTemplate().postForObject(url, entity, String.class);
    }

    public void deleteCoverage(String workspace, String coverageStore, String coverage) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores/{coveragestore}/coverages/{coverage}")
                // Set recurse=true to avoid having to manage layers
                .queryParam("recurse", true)
                .buildAndExpand(workspace, coverageStore, coverage).toUri();
        restTemplate().delete(url);
    }

    public boolean layerExists(String workspace, String layerName) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/layers/{layerName}")
                .buildAndExpand(workspace, layerName).toUri();
        try {
            ResponseEntity<String> responseEntity = restTemplate().getForEntity(url, String.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    public LayerResponse getLayer(String workspace, String layerName) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/layers/{layerName}")
                .buildAndExpand(workspace, layerName).toUri();
        return restTemplate().getForObject(url, LayerResponse.class);
    }

    public void setLayerDefaultStyle(String workspace, String layerName, String defaultStyle) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/layers/{layerName}")
                .buildAndExpand(workspace, layerName).toUri();
        val entity = httpEntity(new SetLayerDefaultStyleRequest(workspace, defaultStyle));
        restTemplate().put(url, entity);
    }


    public List<String> listStyles(String workspace) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/styles")
                .buildAndExpand(workspace).toUri();
        return list("style", url);
    }

    public void createStyle(String workspace, String style) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/styles")
                .buildAndExpand(workspace).toUri();
        val entity = httpEntity(new CreateStyleRequest(workspace, style));
        restTemplate().postForObject(url, entity, String.class);
    }

    public void uploadSld(String workspace, String style, String sld) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/styles/{style}")
                .buildAndExpand(workspace, style).toUri();
        String sldFileContents = loadSldFile("classpath:geoserver/"+sld+".sld");
        val entity = httpEntity(sldFileContents, "application/vnd.ogc.sld+xml");
        restTemplate().put(url, entity);
    }

    public void deleteStyle(String workspace, String style) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/styles/{style}")
                // The purge parameter specifies whether the underlying SLD file for the style should be deleted on disk
                .queryParam("purge", true)
                // The recurse parameter removes references to the specified style in existing layers
                .queryParam("recurse", true)
                .buildAndExpand(workspace, style).toUri();
        restTemplate().delete(url);
    }


    private String loadSldFile(String path) {
        try (InputStream inputStream = resourceLoader.getResource(path).getInputStream()) {
            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            log.warn("Couldn't read SLD file", e);
            throw new RuntimeException(e);
        }
    }
}
