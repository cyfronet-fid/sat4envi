package pl.cyfronet.s4e.data.repository.query;

import pl.cyfronet.s4e.bean.Scene;

import java.util.List;
import java.util.Map;

public class QueryBuilderImpl implements QueryBuilder{
    @Override
    public void prepareQueryAndParameters(Map<String, Object> params,
                                          List<Object> parameters,
                                          StringBuilder resultQuery) {
        resultQuery.append("SELECT " + Scene.COLUMN_ID + "," + Scene.COLUMN_PRODUCT_ID + "  FROM Scene WHERE true ");
    }
}
