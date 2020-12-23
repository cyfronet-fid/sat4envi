/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.cyfronet.s4e.sync.context.Context;
import pl.cyfronet.s4e.sync.step.Step;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class SceneAcceptorImpl implements SceneAcceptor {
    private final PipelineFactory pipelineFactory;

    @Override
    public Error accept(String sceneKey) {
        Error result = null;
        Context context = new Context(sceneKey);
        try {
            List<Step<Context, Error>> pipeline = pipelineFactory.build();
            for (Step<Context, Error> step : pipeline) {
                result = step.apply(context);
                if (result != null) {
                    break;
                }
            }
        } catch (RuntimeException e) {
            result = context.getError().cause(e).build();
        }
        return result;
    }
}
