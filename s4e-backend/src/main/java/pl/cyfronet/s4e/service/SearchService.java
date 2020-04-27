package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.SceneRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SceneRepository repository;

    public List<Scene> getScenesBy(Map<String, Object> params) throws SQLException {
        return repository.findAllByParamsMap(params);
    }
}
