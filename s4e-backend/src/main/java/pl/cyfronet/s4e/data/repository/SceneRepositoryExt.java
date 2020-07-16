package pl.cyfronet.s4e.data.repository;

import pl.cyfronet.s4e.ex.QueryException;
import pl.cyfronet.s4e.api.MappedScene;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

//an interface with the methods you wish to use with EntityManger.
public interface SceneRepositoryExt {
    List<MappedScene> findAllByParamsMap(Map<String, Object> params) throws SQLException, QueryException;
}