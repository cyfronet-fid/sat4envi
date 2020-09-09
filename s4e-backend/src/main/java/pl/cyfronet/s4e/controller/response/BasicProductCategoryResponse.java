package pl.cyfronet.s4e.controller.response;

import org.springframework.beans.factory.annotation.Value;

public interface BasicProductCategoryResponse {
    Long getId();
    String getLabel();

    @Value("#{@productCategoryService.getIconPath(target.iconName)}")
    String getIconPath();
}
