package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.util.MarkdownHtmlUtil;

@Data
@Builder
public class ProductTypeResponse {
    private Long id;
    private String name;
    private String description;

    public static ProductTypeResponse of(ProductType productType) {
        return ProductTypeResponse.builder()
                .id(productType.getId())
                .name(productType.getName())
                .description(MarkdownHtmlUtil.markdownToStringHtml(productType.getDescription()))
                .build();
    }
}
