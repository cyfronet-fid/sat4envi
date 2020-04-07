package pl.cyfronet.s4e.sync.context;

import pl.cyfronet.s4e.sync.Error;

public interface BaseContext {
    Error.ErrorBuilder getError();
}
