package pl.cyfronet.s4e.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Webhook;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.GeoServerService;
import pl.cyfronet.s4e.service.ProductService;

import static pl.cyfronet.s4e.Constants.API_PREFIX_S3;

@RestController
@RequestMapping(API_PREFIX_S3)
@Slf4j
@RequiredArgsConstructor
public class WebhooksController {

    private final ProductService productService;
    private final GeoServerService geoServerService;

    @PostMapping("/webhook")
    public void addedFile(@RequestBody Webhook webhook) {
        if (webhook.getEventName().contains("Post") || webhook.getEventName().contains("Put")) {
            Product product = null;
            try {
                product = productService.buildFromWebhook(webhook);
                productService.saveProduct(product);
                geoServerService.addLayer(product);
                product.setCreated(true);
                productService.saveProduct(product);
            } catch (NotFoundException e) {
                log.error("Error with files synch: [" + webhook.getKey() + "]" + e.getMessage(), e);
            }
        }
    }
}
