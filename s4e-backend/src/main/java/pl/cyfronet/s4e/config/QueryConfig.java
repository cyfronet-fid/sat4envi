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

package pl.cyfronet.s4e.config;

import lombok.val;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.cyfronet.s4e.data.repository.query.*;

import java.time.Clock;
import java.util.List;
import java.util.function.Function;

@Configuration
public class QueryConfig {
    private final SpringDataWebProperties springDataWebProperties;
    private final Clock clock;

    public QueryConfig(SpringDataWebProperties springDataWebProperties, Clock clock) {
        this.springDataWebProperties = springDataWebProperties;
        this.clock = clock;
    }

    @Bean
    public PreparedStatementBuilder preparedStatementBuilder() {
        return new PreparedStatementBuilder(queryBuilder());
    }

    @Bean
    public QueryBuilder queryBuilder() {
        QueryBuilder queryBuilder = new QueryBuilderImpl();
        List<Function<QueryBuilder, QueryBuilder>> constructors = List.of(
                QueryProductType::new,
                qb -> new QueryTime(qb, clock),
                QueryGeometry::new,
                QueryText::new,
                QueryNumber::new
        );
        for (val constructor : constructors) {
            queryBuilder = constructor.apply(queryBuilder);
        }
        return new QueryEnding(
                queryBuilder,
                springDataWebProperties.getPageable().getMaxPageSize(),
                springDataWebProperties.getPageable().getDefaultPageSize(),
                0
        );
    }
}
