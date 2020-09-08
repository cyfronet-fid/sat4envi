package pl.cyfronet.s4e.controller.response;

import org.springframework.beans.factory.annotation.Value;

public interface BasicProductResponse {
    Long getId();
    String getName();
    String getDisplayName();

    @Value("#{@productService.isFavourite(target.id)}")
    boolean getFavourite();

    BasicProductCategoryResponse getProductCategory();
}