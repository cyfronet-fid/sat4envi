package pl.cyfronet.s4e.ex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@RequiredArgsConstructor
@Component
@Slf4j
public class AsyncUncaughtExceptionHandlerImpl implements AsyncUncaughtExceptionHandler {
    private final AsyncUncaughtExceptionHandler defaultHandler;

    @Override
    public void handleUncaughtException(Throwable e, Method method, Object... params) {
        if (e instanceof IllegalStateException) {
            log.info("An uncaught exception was thrown from "+method, e);
        } else {
            defaultHandler.handleUncaughtException(e, method, params);
        }
    }
}
