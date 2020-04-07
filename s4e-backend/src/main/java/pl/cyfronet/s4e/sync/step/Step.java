package pl.cyfronet.s4e.sync.step;

import pl.cyfronet.s4e.sync.context.BaseContext;

import java.util.function.Function;

public interface Step<T extends BaseContext, E> extends Function<T, E> {
}
