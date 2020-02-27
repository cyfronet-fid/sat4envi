package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.controller.request.WebhookRequest;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.SceneService;

import static pl.cyfronet.s4e.Constants.API_PREFIX_S3;

@RestController
@RequestMapping(API_PREFIX_S3)
@Slf4j
@RequiredArgsConstructor
@Tag(name = "webhook", description = "The Webhook API")
public class WebhooksController {

    private final SceneService sceneService;

    @Operation(summary = "Create scene in db and add to GeoServer")
    @PostMapping("/webhook")
    public void addedFile(@RequestBody WebhookRequest webhookRequest) {
        if (webhookRequest.getEventName().contains("Post") || webhookRequest.getEventName().contains("Put")) {
            Scene scene = null;
            try {
                scene = sceneService.buildFromWebhook(webhookRequest);
                sceneService.saveScene(scene);
            } catch (NotFoundException e) {
                log.error("Error with files synch: [" + webhookRequest.getKey() + "]" + e.getMessage(), e);
            }
        }
    }
}
