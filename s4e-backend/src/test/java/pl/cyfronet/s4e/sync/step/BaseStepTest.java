package pl.cyfronet.s4e.sync.step;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BaseStepTest<T extends BaseContext> {
    @Mock
    protected T context;

    protected String sceneKey = "scene_key";

    protected void stubContext() {
        when(context.getError()).thenReturn(Error.builder(sceneKey));
    }
}
