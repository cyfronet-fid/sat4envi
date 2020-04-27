package pl.cyfronet.s4e.data.repository;

import pl.cyfronet.s4e.bean.Scene;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

//an interface with the methods you wish to use with EntityManger.
public interface SceneRepositoryExt {
    List<Scene> findAllByParamsMap(Map<String, Object> params) throws SQLException;
}