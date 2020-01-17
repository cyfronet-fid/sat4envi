package pl.cyfronet.s4e.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import pl.cyfronet.s4e.MailProperties;

@Service
@RequiredArgsConstructor
public class MailHelper {
    private final MailProperties mailProperties;

    public void injectCommonVariables(Context context) {
        context.setVariable("urlDomain", mailProperties.getUrlDomain());
    }

    public String prefixWithDomain(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path must have leading slash");
        }
        return mailProperties.getUrlDomain() + path;
    }
}
