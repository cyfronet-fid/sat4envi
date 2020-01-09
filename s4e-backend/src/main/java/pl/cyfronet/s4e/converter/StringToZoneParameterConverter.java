package pl.cyfronet.s4e.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.controller.request.ZoneParameter;

import java.time.DateTimeException;
import java.time.ZoneId;

@Component
public class StringToZoneParameterConverter implements Converter<String, ZoneParameter> {
    @Override
    public ZoneParameter convert(String source) {
        try {
            return new ZoneParameter(ZoneId.of(source));
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
