package pl.cyfronet.s4e.config;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.cyfronet.s4e.data.repository.query.*;

@Configuration
public class QueryConfig {
    final SpringDataWebProperties springDataWebProperties;

    public QueryConfig(SpringDataWebProperties springDataWebProperties) {
        this.springDataWebProperties = springDataWebProperties;
    }

    @Bean
    public PreparedStatementBuilder preparedStatementBuilder() {
        return new PreparedStatementBuilder(queryBuilder());
    }

    @Bean
    public QueryBuilder queryBuilder() {
        return new QueryEnding(
                new QueryNumber(
                        new QueryText(
                                new QueryTime(new QueryBuilderImpl())
                        )
                ), springDataWebProperties
        );
    }
}
