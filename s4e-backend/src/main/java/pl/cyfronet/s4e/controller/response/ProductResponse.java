package pl.cyfronet.s4e.controller.response;

import org.springframework.beans.factory.annotation.Value;
import pl.cyfronet.s4e.bean.Legend;

public interface ProductResponse {
    Long getId();

    String getName();

    @Value("#{@markdownHtmlUtil.markdownToStringHtml(target.description)}")
    String getDescription();

    Legend getLegend();
}
