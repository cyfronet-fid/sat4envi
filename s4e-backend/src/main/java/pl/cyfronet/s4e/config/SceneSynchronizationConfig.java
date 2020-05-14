package pl.cyfronet.s4e.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.cyfronet.s4e.sync.LoggingAcceptor;
import pl.cyfronet.s4e.sync.PipelineFactory;
import pl.cyfronet.s4e.sync.SceneAcceptor;
import pl.cyfronet.s4e.sync.SceneAcceptorImpl;

@Configuration
public class SceneSynchronizationConfig {
    @Bean
    public SceneAcceptor sceneAcceptor(PipelineFactory pipelineFactory) {
        return new LoggingAcceptor(new SceneAcceptorImpl(pipelineFactory));
    }
}
