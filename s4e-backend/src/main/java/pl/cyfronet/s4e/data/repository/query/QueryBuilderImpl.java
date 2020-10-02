package pl.cyfronet.s4e.data.repository.query;

import org.springframework.validation.Errors;
import pl.cyfronet.s4e.bean.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryBuilderImpl implements QueryBuilder {
    private List<String> columns;

    public QueryBuilderImpl() {
        columns = new ArrayList<>();
        columns.add(Scene.COLUMN_ID);
        columns.add(Scene.COLUMN_PRODUCT_ID);
        columns.add(Scene.COLUMN_SCENE_KEY);
        columns.add("ST_AsText(ST_Transform(" + Scene.COLUMN_FOOTPRINT + ",4326)) AS footprint");
        columns.add(Scene.COLUMN_METADATA);
        columns.add(Scene.COLUMN_CONTENT);
        columns.add(Scene.COLUMN_TIMESTAMP);
    }

    @Override
    public void prepareQueryAndParameters(Map<String, Object> params,
                                          List<Object> parameters,
                                          StringBuilder resultQuery,
                                          Errors errors) {
        resultQuery.append("SELECT ");
        resultQuery.append(String.join(",", columns));
        resultQuery.append(" FROM Scene WHERE true ");
    }
}
