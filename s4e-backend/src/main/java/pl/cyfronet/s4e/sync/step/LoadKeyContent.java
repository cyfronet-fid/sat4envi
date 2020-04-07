package pl.cyfronet.s4e.sync.step;

import lombok.Builder;
import lombok.val;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.service.SceneStorage;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static pl.cyfronet.s4e.sync.Error.ERR_FILE_NOT_FOUND;
import static pl.cyfronet.s4e.sync.Error.ERR_S3_CLIENT_EXCEPTION;

@Builder
public class LoadKeyContent<T extends BaseContext> implements Step<T, Error> {
    private final Function<T, String> key;
    private final BiConsumer<T, String> update;

    private final Supplier<SceneStorage> sceneStorage;

    public Error apply(T context) {
        val error = context.getError();
        try {
            update.accept(context, sceneStorage.get().get(key.apply(context)));
        } catch (NotFoundException e) {
            return error.code(ERR_FILE_NOT_FOUND).cause(e).build();
        } catch (S3ClientException e) {
            return error.code(ERR_S3_CLIENT_EXCEPTION).cause(e).build();
        }
        return null;
    }
}
