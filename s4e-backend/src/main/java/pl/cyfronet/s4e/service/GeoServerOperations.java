package pl.cyfronet.s4e.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.cyfronet.s4e.service.payload.CreateS3CoveragePayload;
import pl.cyfronet.s4e.service.payload.CreateS3CoverageStorePayload;
import pl.cyfronet.s4e.service.payload.CreateWorkspacePayload;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper;

    private RestTemplate restTemplate() {
        return restTemplateBuilder.basicAuthentication(geoserverUsername, geoserverPassword).build();
    }

    private <T> HttpEntity<T> httpEntity(T payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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
        val entity = httpEntity(new CreateWorkspacePayload(workspace));

        restTemplate().postForObject(url, entity, String.class);
    }

    public void deleteWorkspace(String workspace, boolean recurse) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}")
                .queryParam("recurse", recurse)
                .buildAndExpand(workspace).toUri();
        restTemplate().delete(url);
    }


    public List<String> listCoverageStores(String workspace) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores")
                .buildAndExpand(workspace).toUri();
        return list("coverageStore", url);
    }

    public void createS3CoverageStore(String workspace, String coverageStore, String s3url) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores")
                .buildAndExpand(workspace).toUri();
        val entity = httpEntity(new CreateS3CoverageStorePayload(workspace, coverageStore, s3url));
        restTemplate().postForObject(url, entity, String.class);
    }

    public void deleteCoverageStore(String workspace, String coverageStore, boolean recurse) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores/{coveragestore}")
                .queryParam("recurse", recurse)
                .buildAndExpand(workspace, coverageStore).toUri();
        restTemplate().delete(url);
    }


    public List<String> listCoverages(String workspace, String coverageStore) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores/{coveragestore}/coverages")
                .buildAndExpand(workspace, coverageStore).toUri();
        return list("coverage", url);
    }

    public void createS3Coverage(String workspace, String coverageStore, String coverage) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores/{coveragestore}/coverages")
                .buildAndExpand(workspace, coverageStore).toUri();
        val entity = httpEntity(new CreateS3CoveragePayload(workspace, coverageStore, coverage));
        restTemplate().postForObject(url, entity, String.class);
    }

    public void deleteCoverage(String workspace, String coverageStore, String coverage) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoserverBaseUrl+"/workspaces/{workspace}/coveragestores/{coveragestore}/coverages/{coverage}")
                // Set recurse=true to avoid having to manage layers
                .queryParam("recurse", true)
                .buildAndExpand(workspace, coverageStore, coverage).toUri();
        restTemplate().delete(url);
    }
}
