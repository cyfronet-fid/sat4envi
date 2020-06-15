package pl.cyfronet.s4e.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SentinelSearchConfig {
    private final Common common;
    private final List<Section> sections;

    @Getter
    @RequiredArgsConstructor
    public static class Common {
        private final List<Param> params;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Section {
        private final String name;
        private final List<Param> params;
    }

    @Getter
    @RequiredArgsConstructor
    @Schema(anyOf = { SelectParam.class, FloatParam.class, TextParam.class, DatetimeParam.class })
    public abstract static class Param {
        private final String queryParam;
        private final String type;
    }

    @Getter
    public static class SelectParam extends Param {
        public SelectParam(String queryParam, List<String> values) {
            super(queryParam, "select");
            this.values = values;
        }

        private final List<String> values;
    }

    @Getter
    public static class FloatParam extends Param {
        public FloatParam(String queryParam, Double min, Double max) {
            super(queryParam, "float");
            this.min = min;
            this.max = max;
        }

        private final Double min;
        private final Double max;
    }

    @Getter
    public static class TextParam extends Param {
        public TextParam(String queryParam) {
            super(queryParam, "text");
        }
    }

    @Getter
    public static class DatetimeParam extends Param {
        public DatetimeParam(String queryParam) {
            super(queryParam, "datetime");
        }
    }
}
