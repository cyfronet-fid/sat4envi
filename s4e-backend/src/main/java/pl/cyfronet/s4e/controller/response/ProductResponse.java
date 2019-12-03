package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.Legend;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.util.MarkdownHtmlUtil;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Legend legend;

    public static ProductResponse of(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(MarkdownHtmlUtil.markdownToStringHtml(product.getDescription()))
                .legend(product.getLegend())
                .build();
    }
}
