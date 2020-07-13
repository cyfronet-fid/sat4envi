package pl.cyfronet.s4e.admin.product;

import pl.cyfronet.s4e.bean.Legend;

import java.util.Map;

interface AdminProductResponse {
    interface SceneSchema {
        Long getId();

        String getName();
    }

    interface MetadataSchema {
        Long getId();

        String getName();
    }

    Long getId();

    String getName();

    String getDisplayName();

    String getDescription();

    Legend getLegend();

    String getLayerName();

    SceneSchema getSceneSchema();

    MetadataSchema getMetadataSchema();

    Map<String, String> getGranuleArtifactRule();
}
