/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.geoserver.op;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.cyfronet.s4e.geoserver.op.request.CreateS3CoverageRequest;
import pl.cyfronet.s4e.geoserver.op.request.CreateStyleRequest;
import pl.cyfronet.s4e.geoserver.op.request.CreateWorkspaceRequest;
import pl.cyfronet.s4e.geoserver.op.request.SetLayerDefaultStyleRequest;
import pl.cyfronet.s4e.geoserver.op.response.LayerResponse;
import pl.cyfronet.s4e.properties.GeoServerProperties;
import pl.cyfronet.s4e.util.ResourceReader;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoServerOperations {
    private final GeoServerProperties geoServerProperties;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private RestTemplate restTemplate() {
        return restTemplateBuilder
                .basicAuthentication(geoServerProperties.getUsername(), geoServerProperties.getPassword())
                .setConnectTimeout(Duration.ofSeconds(geoServerProperties.getTimeoutConnect()))
                .setReadTimeout(Duration.ofSeconds(geoServerProperties.getTimeoutRead()))
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
            for (val node : tree.path(type + "s").path(type)) {
                out.add(node.path("name").asText());
            }
        } catch (IOException e) {
            log.warn("Exception when reading response", e);
        }
        return out;
    }


    public List<String> listWorkspaces() {
        URI url = UriComponentsBuilder.fromHttpUrl(geoServerProperties.getBaseUrl() + "/workspaces")
                .build().toUri();
        return list("workspace", url);
    }

    public void createWorkspace(String workspace) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoServerProperties.getBaseUrl() + "/workspaces")
                .build().toUri();
        val entity = httpEntity(new CreateWorkspaceRequest(workspace));

        restTemplate().postForObject(url, entity, String.class);
    }

    public void deleteWorkspace(String workspace, boolean recurse) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoServerProperties.getBaseUrl() + "/workspaces/{workspace}")
                .queryParam("recurse", recurse)
                .buildAndExpand(workspace).toUri();
        restTemplate().delete(url);
    }


    public List<String> listDataStores(String workspace) {
        URI url = UriComponentsBuilder.fromHttpUrl(geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/datastores")
                .buildAndExpand(workspace).toUri();
        return list("dataStore", url);
    }

    /**
     * @param workspace
     * @param dataStoreName
     * @param path          the path to shapefiles, including the "file://" prefix
     */
    public void createExternalShpDataStore(String workspace, String dataStoreName, String path) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/datastores/{dataStoreName}/external.shp")
                .queryParam("configure", "all")
                .buildAndExpand(workspace, dataStoreName).toUri();
        val entity = httpEntity(path, "text/plain");
        restTemplate().put(url, entity);
    }


    public List<String> listCoverageStores(String workspace) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/coveragestores")
                .buildAndExpand(workspace).toUri();
        return list("coverageStore", url);
    }

    public void createS3CoverageStore(String workspace, String coverageStore) {
        InputStream file = getArchive(coverageStore);

        Unirest.put(geoServerProperties.getBaseUrl() + "/workspaces/{ws}/coveragestores/{cs}/file.imagemosaic?configure=none")
                .header("Content-Type", "application/zip")
                .routeParam("ws", workspace)
                .routeParam("cs", coverageStore)
                .basicAuth(geoServerProperties.getUsername(), geoServerProperties.getPassword())
                .connectTimeout(geoServerProperties.getTimeoutConnect().intValue() * 1000)
                .field("file", file)
                .asString()
                .ifFailure(String.class, r -> {
                    throw new RestClientException(r.getBody());
                });
    }

    public InputStream getArchive(String coverageStore) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String mosaic = "Name=" + coverageStore + "\n" +
                "TypeName=scene_" + coverageStore + "\n" +
                "Levels=1,1\n" +
                "LevelsNum=1\n";

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (
                InputStream streamMosaic = new SequenceInputStream(new ByteArrayInputStream(mosaic.getBytes()), classloader.getResourceAsStream("geoserver/mosaic.properties"));
                InputStream streamStore = classloader.getResourceAsStream("geoserver/datastore.properties");
                ZipOutputStream zos = new ZipOutputStream(bos)) {
            zos.putNextEntry(new ZipEntry(coverageStore + ".properties"));
            zos.write(streamMosaic.readAllBytes());
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("datastore.properties"));
            zos.write(streamStore.readAllBytes());
            zos.closeEntry();
        } catch (FileNotFoundException ex) {
            log.warn("Couldn't read properties file", ex);
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            log.warn("Couldn't get archive", ex);
            throw new RuntimeException(ex);
        }

        return new ByteArrayInputStream(bos.toByteArray());
    }

    public void deleteCoverageStore(String workspace, String coverageStore, boolean recurse) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/coveragestores/{coveragestore}")
                .queryParam("recurse", recurse)
                .buildAndExpand(workspace, coverageStore).toUri();
        restTemplate().delete(url);
    }


    public List<String> listDataStoreFeatureTypes(String workspace, String dataStoreName) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/datastores/{dataStoreName}/featuretypes")
                .buildAndExpand(workspace, dataStoreName).toUri();
        return list("featureType", url);
    }


    public List<String> listCoverages(String workspace, String coverageStore) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/coveragestores/{coveragestore}/coverages")
                .buildAndExpand(workspace, coverageStore).toUri();
        return list("coverage", url);
    }

    public void createS3Coverage(String workspace, String coverageStore, String coverage) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/coveragestores/{coveragestore}/coverages")
                .buildAndExpand(workspace, coverageStore).toUri();
        val entity = httpEntity(new CreateS3CoverageRequest(workspace, coverageStore, coverage));
        restTemplate().postForObject(url, entity, String.class);
    }

    public void deleteCoverage(String workspace, String coverageStore, String coverage) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/coveragestores/{coveragestore}/coverages/{coverage}")
                // Set recurse=true to avoid having to manage layers
                .queryParam("recurse", true)
                .buildAndExpand(workspace, coverageStore, coverage).toUri();
        restTemplate().delete(url);
    }

    public boolean layerExists(String workspace, String layerName) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/layers/{layerName}")
                .buildAndExpand(workspace, layerName).toUri();
        try {
            ResponseEntity<String> responseEntity = restTemplate().getForEntity(url, String.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (RestClientResponseException e) {
            return false;
        }
    }

    public LayerResponse getLayer(String workspace, String layerName) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/layers/{layerName}")
                .buildAndExpand(workspace, layerName).toUri();
        return restTemplate().getForObject(url, LayerResponse.class);
    }

    public void setLayerDefaultStyle(String workspace, String layerName, String defaultStyle) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/layers/{layerName}")
                .buildAndExpand(workspace, layerName).toUri();
        val entity = httpEntity(new SetLayerDefaultStyleRequest(workspace, defaultStyle));
        restTemplate().put(url, entity);
    }

    public void createTileLayer(String workspace, String layerName) {
        String baseUrl = geoServerProperties.getBaseUrl().replace("/rest", "");
        URI url = UriComponentsBuilder.fromHttpUrl(
                baseUrl + "/gwc/rest/layers/{workspace}:{layerName}.xml")
                .buildAndExpand(workspace, layerName).toUri();
        String payload = loadResource("classpath:geoserver/request/post-tile-layer.xml")
                .replace("{workspace}", workspace)
                .replace("{layerName}", layerName);
        val entity = httpEntity(payload, MediaType.APPLICATION_XML_VALUE);
        restTemplate().put(url, entity);
    }

    public boolean tileLayerExists(String workspace, String layerName) {
        String baseUrl = geoServerProperties.getBaseUrl().replace("/rest", "");
        URI url = UriComponentsBuilder.fromHttpUrl(
                baseUrl + "/gwc/rest/layers/{workspace}:{layerName}")
                .buildAndExpand(workspace, layerName).toUri();
        try {
            ResponseEntity<String> responseEntity = restTemplate().getForEntity(url, String.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (RestClientResponseException e) {
            return false;
        }
    }

    public void deleteTileLayer(String workspace, String layerName) {
        String baseUrl = geoServerProperties.getBaseUrl().replace("/rest", "");
        URI url = UriComponentsBuilder.fromHttpUrl(
                baseUrl + "/gwc/rest/layers/{workspace}:{layerName}")
                .buildAndExpand(workspace, layerName).toUri();
        restTemplate().delete(url);
    }

    public List<String> listStyles(String workspace) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/styles")
                .buildAndExpand(workspace).toUri();
        return list("style", url);
    }

    public void createStyle(String workspace, String style) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/styles")
                .buildAndExpand(workspace).toUri();
        val entity = httpEntity(new CreateStyleRequest(workspace, style));
        restTemplate().postForObject(url, entity, String.class);
    }

    public void uploadSld(String workspace, String style, String sld) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/styles/{style}")
                .buildAndExpand(workspace, style).toUri();
        String sldFileContents = loadResource("classpath:geoserver/" + sld + ".sld");
        val entity = httpEntity(sldFileContents, "application/vnd.ogc.sld+xml");
        restTemplate().put(url, entity);
    }

    public void deleteStyle(String workspace, String style) {
        URI url = UriComponentsBuilder.fromHttpUrl(
                geoServerProperties.getBaseUrl() + "/workspaces/{workspace}/styles/{style}")
                // The purge parameter specifies whether the underlying SLD file for the style should be deleted on disk
                .queryParam("purge", true)
                // The recurse parameter removes references to the specified style in existing layers
                .queryParam("recurse", true)
                .buildAndExpand(workspace, style).toUri();
        restTemplate().delete(url);
    }


    private String loadResource(String path) {
        try {
            return ResourceReader.asString(resourceLoader.getResource(path));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            log.warn("Couldn't read resource: '" + path + "'", e);
            throw new RuntimeException(e);
        }
    }
}
