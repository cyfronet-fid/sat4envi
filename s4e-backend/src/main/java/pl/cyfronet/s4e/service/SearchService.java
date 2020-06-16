package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.api.SearchConverter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SceneRepository repository;
    private final SearchConverter converter;

    public List<Scene> getScenesBy(Map<String, Object> params) throws SQLException {
        return repository.findAllByParamsMap(params);
    }

    public Map<String, Object> parseToParamMap(String rowsSize, String rowStart, String orderby, String query) {
        Map<String, Object> result = new HashMap<>();
        if (query != null) {
            result.putAll(converter.convert(query));
        }
        result.putAll(converter.convertParams(rowsSize, rowStart, orderby));
        return result;
    }
}
