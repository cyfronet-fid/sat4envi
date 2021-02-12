/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SearchPanelConfigResponse {
    private final Common common;
    private final List<Section> sections;

    @Getter
    @RequiredArgsConstructor
    public static class Common {
        private final List<Param> params;
    }

    @Value
    public static class SectionPrototype {
        String label;
        String metadataSchemaName;
        List<ParamPrototype> prefixParams;
        List<ParamPrototype> suffixParams;
    }

    @Getter
    @RequiredArgsConstructor
    public abstract static class ParamPrototype {
        private final String queryParam;
    }

    @Getter
    public static class SelectParamPrototype extends ParamPrototype {
        public SelectParamPrototype(String queryParam, List<String> values) {
            super(queryParam);
            this.values = values;
        }

        private final List<String> values;
    }

    @Getter
    public static class FloatParamPrototype extends ParamPrototype {
        public FloatParamPrototype(String queryParam, Double min, Double max) {
            super(queryParam);
            this.min = min;
            this.max = max;
        }

        private final Double min;
        private final Double max;
    }

    @Getter
    public static class TextParamPrototype extends ParamPrototype {
        public TextParamPrototype(String queryParam) {
            super(queryParam);
        }
    }

    @Getter
    public static class DatetimeParamPrototype extends ParamPrototype {
        public DatetimeParamPrototype(String queryParam) {
            super(queryParam);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Section {
        private final String name;
        private final String label;
        private final List<Param> params;
    }

    @Getter
    @RequiredArgsConstructor
    @Schema(anyOf = { SelectParam.class, FloatParam.class, TextParam.class, DatetimeParam.class })
    public abstract static class Param {
        private final String queryParam;
        private final String label;
        private final String type;
    }

    @Getter
    @RequiredArgsConstructor
    public static class SelectValue {
        private final String value;
        private final String label;
    }

    @Getter
    public static class SelectParam extends Param {
        public SelectParam(String queryParam, String label, List<SelectValue> values) {
            super(queryParam, label, "select");
            this.values = values;
        }

        private final List<SelectValue> values;
    }

    @Getter
    public static class FloatParam extends Param {
        public FloatParam(String queryParam, String label, Double min, Double max) {
            super(queryParam, label, "float");
            this.min = min;
            this.max = max;
        }

        private final Double min;
        private final Double max;
    }

    @Getter
    public static class TextParam extends Param {
        public TextParam(String queryParam, String label) {
            super(queryParam, label, "text");
        }
    }

    @Getter
    public static class DatetimeParam extends Param {
        public DatetimeParam(String queryParam, String label) {
            super(queryParam, label, "datetime");
        }
    }
}
