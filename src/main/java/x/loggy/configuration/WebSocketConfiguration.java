package x.loggy.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Import(StompLogging.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebSocketConfiguration
        implements WebSocketMessageBrokerConfigurer {
    private final StompLogging stompLogging;

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket").withSockJS();
    }

    @Override
    public void configureWebSocketTransport(
            final WebSocketTransportRegistration registry) {
        registry.addDecoratorFactory(stompLogging);
    }

    @Override
    public boolean configureMessageConverters(
            final List<MessageConverter> messageConverters) {
        return true;
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/websocket");
    }
}
