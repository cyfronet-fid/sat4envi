package pl.cyfronet.s4e.ex;

import com.github.mkopylec.recaptcha.validation.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import pl.cyfronet.s4e.ex.product.ProductValidationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ErrorHandlerHelper {
    private final Environment env;
    private final MessageSource messageSource;

    public Map<String, Object> toResponseMap(Exception e) {
        String message = e.getMessage();
        val map = new LinkedHashMap<String, Object>();
        map.put("__general__", message);
        optionallyAddDevelopmentInformation(map, e);
        return map;
    }

    public Map<String, Object> toResponseMap(MethodArgumentNotValidException e) {
        val out = getStringObjectMap(e.getBindingResult());
        optionallyAddDevelopmentInformation(out, e);
        return out;
    }

    public Map<String, Object> toResponseMap(QueryException e) {
        val out = getStringObjectMap(e.getBindingResult());
        optionallyAddDevelopmentInformation(out, e);
        return out;
    }

    public Map<String, Object> toResponseMap(ProductValidationException e) {
        val out = getStringObjectMap(e.getBindingResult());
        optionallyAddDevelopmentInformation(out, e);
        return out;
    }

    public Map<String, Object> toResponseMap(RecaptchaException e) {
        val errorCodes = e.getErrorCodes();
        val map = new LinkedHashMap<String, Object>();

        map.put("recaptcha", errorCodes.stream()
                .map(ErrorCode::getText)
                .collect(Collectors.toList()));

        optionallyAddDevelopmentInformation(map, e);
        return map;
    }

    private LinkedHashMap<String, Object> getStringObjectMap(BindingResult bindingResult) {
        val map = new LinkedHashMap<String, Object>();

        // populate general errors
        if (bindingResult.hasGlobalErrors()) {
            map.put("__general__", bindingResult.getGlobalErrors().stream()
                    .map(objectError -> getMessage(objectError))
                    .collect(Collectors.toUnmodifiableList()));
        }

        // populate field errors
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            map.putIfAbsent(fieldError.getField(), new ArrayList<>());
            ((List) map.get(fieldError.getField())).add(
                    getMessage(fieldError));
        }

        return map;
    }

    private String getMessage(DefaultMessageSourceResolvable error) {
        return messageSource.getMessage(error, LocaleContextHolder.getLocale());
    }

    private void optionallyAddDevelopmentInformation(Map<String, Object> map, Exception e) {
        if (Arrays.stream(env.getActiveProfiles()).anyMatch("development"::equals)) {
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
