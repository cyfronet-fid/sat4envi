package pl.cyfronet.s4e.data.repository.query;

import pl.cyfronet.s4e.util.GeometryUtil;

import java.util.List;
import java.util.Map;

import static pl.cyfronet.s4e.api.SearchApiParams.FOOTPRINT;

public class QueryGeometry extends QueryDecorator {
    public QueryGeometry(QueryBuilder queryBuilder) {
        super(queryBuilder);
    }

    @Override
    protected void doPrepareQueryAndParameters(Map<String, Object> params,
                                               List<Object> parameters,
                                               StringBuilder resultQuery) {
        queryByPolygon(params, parameters, resultQuery);
    }

    private void queryByPolygon(Map<String, Object> params,
                                List<Object> parameters,
                                StringBuilder resultQuery) {
        if (params.containsKey(FOOTPRINT)) {

            resultQuery.append(" AND ST_Intersects(footprint, " +
                    "ST_Transform(" +
                    "ST_GeomFromText(?, "
                    + GeometryUtil.FACTORY_4326.getSRID() + "), "
                    + GeometryUtil.FACTORY_3857.getSRID() + "))");
            parameters.add(String.valueOf(params.get(FOOTPRINT)));
        }
    }
}
