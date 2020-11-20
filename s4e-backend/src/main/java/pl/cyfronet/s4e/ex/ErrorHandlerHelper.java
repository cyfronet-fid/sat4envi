package pl.cyfronet.s4e.ex;

import com.github.mkopylec.recaptcha.validation.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ErrorHandlerHelper {
    private final Environment env;
    private final MessageSource messageSource;

    public Map<String, Object> toResponseMap(Exception e) {
        val map = new LinkedHashMap<String, Object>();
        map.put("__general__", e.getMessage());
        if (e instanceof BindingResultException) {
            addBindingResultInformation(map, ((BindingResultException) e).getBindingResult());
        }
        optionallyAddDevelopmentInformation(map, e);
        return map;
    }

    public Map<String, Object> toResponseMap(MethodArgumentNotValidException e) {
        val map = new LinkedHashMap<String, Object>();
        addBindingResultInformation(map, e.getBindingResult());
        optionallyAddDevelopmentInformation(map, e);
        return map;
    }

    public Map<String, Object> toResponseMap(RecaptchaException e) {
        val map = new LinkedHashMap<String, Object>();

        map.put("recaptcha", e.getErrorCodes().stream()
                .map(ErrorCode::getText)
                .collect(Collectors.toUnmodifiableList()));

        optionallyAddDevelopmentInformation(map, e);
        return map;
    }

    private void addBindingResultInformation(Map<String, Object> map, BindingResult bindingResult) {
        // populate general errors
        if (bindingResult.hasGlobalErrors()) {
            map.put("__general__", bindingResult.getGlobalErrors().stream()
                    .map(this::getMessage)
                    .collect(Collectors.toUnmodifiableList()));
        }

        // populate field errors
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            map.putIfAbsent(fieldError.getField(), new ArrayList<>());
            ((List) map.get(fieldError.getField())).add(getMessage(fieldError));
        }
    }

    private String getMessage(DefaultMessageSourceResolvable error) {
        return messageSource.getMessage(error, LocaleContextHolder.getLocale());
    }

    private void optionallyAddDevelopmentInformation(Map<String, Object> map, Exception e) {
        if (env.acceptsProfiles(Profiles.of("development"))) {
            map.put("__exception__", e.getClass().getName());
            map.put("__stacktrace__", getStacktraceString(e));
        }
    }

    private static String getStacktraceString(Exception e) {
        val sw = new StringWriter();
        val pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
