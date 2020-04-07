package pl.cyfronet.s4e.sync.context;

import lombok.Data;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.Prototype;
import pl.cyfronet.s4e.sync.step.LoadProduct;

@Data
public class Context implements BaseContext {
    private final SceneJsonFileContext scene = new SceneJsonFileContext();
    private final JsonFileContext metadata = new JsonFileContext();
    private LoadProduct.ProductProjection product;
    private Prototype.PrototypeBuilder prototype;
    private final Error.ErrorBuilder error;

    public Context(String sceneKey) {
        error = Error.builder(sceneKey);
        scene.setKey(sceneKey);
    }
}
