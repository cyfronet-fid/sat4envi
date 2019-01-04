package pl.cyfronet.s4e.ex;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ErrorHandlerHelper {
    private final Environment env;

    public Map<String, String> toResponse(Exception e) {
        return toResponse(e.getMessage(), e.getClass().getName(), getStacktraceString(e));
    }

    public Map<String, String> toResponse(String message, String exception, String stacktrace) {
        val map = new LinkedHashMap<String, String>();
        map.put("message", message);
        if (Arrays.stream(env.getActiveProfiles()).anyMatch("development"::equals)) {
            map.put("exception", exception);
            map.put("stacktrace", stacktrace);
        }
        return map;
    }

    public static String getStacktraceString(Exception e) {
        val sw = new StringWriter();
        val pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
