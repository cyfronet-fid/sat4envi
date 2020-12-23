/*
 * Copyright 2020 ACC Cyfronet AGH
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

package pl.cyfronet.s4e.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BasicTest
@Slf4j
public class SearchConverterTest {
    @Autowired
    private SearchConverter searchConverter;

    @Test
    public void shouldThrowTwoManyKeys() {
        String query = "( beginPosition:[ * TO 2020-06-16T23:59:59.999Z] " +
                "AND endPosition:[* TO 2020-06-16T23:59:59.999Z ] ) " +
                "AND( ingestionDate:[2020-06-10T00:00:00.000Z TO NOW] ) " +
                "AND ( (platformname:Sentinel-1 AND producttype:GRD) " +
                "OR (platformname:Sentinel-2 AND producttype:S2MSI2A))";
        assertThrows(IllegalArgumentException.class, () -> searchConverter.convertQueryToParamMap(query));
    }

    @Test
    public void shouldReturnMap() {
        String query = "( beginPosition:[ * TO 2020-06-16T23:59:59.999Z] " +
                "AND endPosition:[* TO 2020-06-16T23:59:59.999Z ] ) " +
                "AND( ingestionDate:[2020-06-10T00:00:00.000Z TO NOW] ) " +
                "AND ( (platformname:Sentinel-1 AND producttype:GRD) )";
        assertThat(searchConverter.convertQueryToParamMap(query).entrySet(), hasSize(5));
    }
}
