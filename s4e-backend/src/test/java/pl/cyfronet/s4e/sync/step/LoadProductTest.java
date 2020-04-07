package pl.cyfronet.s4e.sync.step;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import javax.json.JsonObject;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static pl.cyfronet.s4e.sync.Error.ERR_PRODUCT_NOT_FOUND;
import static pl.cyfronet.s4e.sync.step.LoadProduct.SCENE_PRODUCT_TYPE_PROPERTY;

class LoadProductTest extends BaseStepTest<BaseContext> {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private BiConsumer<BaseContext, LoadProduct.ProductProjection> update;

    @Mock
    private JsonObject json;

    private String productType = "prod-name";

    private LoadProduct step;

    @BeforeEach
    public void beforeEach() {
        stubContext();

        when(context.getError()).thenReturn(Error.builder(sceneKey));

        step = LoadProduct.builder()
                .productRepository(() -> productRepository)
                .json(c -> json)
                .update(update)
                .build();

        when(json.getString(SCENE_PRODUCT_TYPE_PROPERTY)).thenReturn(productType);
    }

    @Test
    public void shouldWork() {
        LoadProduct.ProductProjection productProjection = mock(LoadProduct.ProductProjection.class);
        when(productRepository.findByName(productType, LoadProduct.ProductProjection.class))
                .thenReturn(Optional.of(productProjection));

        Error error = step.apply(context);

        assertThat(error, is(nullValue()));
        verify(update).accept(context, productProjection);
        verifyNoMoreInteractions(update);
    }

    @Test
    public void shouldHandleMissingProduct() {
        when(productRepository.findByName(any(), any())).thenReturn(Optional.empty());

        Error error = step.apply(context);

        assertThat(error, is(notNullValue()));
        assertThat(error.getCode(), is(equalTo(ERR_PRODUCT_NOT_FOUND)));
        assertThat(error.getParameters(), hasEntry("product_type", productType));
        verifyNoMoreInteractions(update);
    }
}
