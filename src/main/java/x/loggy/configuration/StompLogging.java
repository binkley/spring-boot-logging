package x.loggy.configuration;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import javax.annotation.Nonnull;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StompLogging
        implements WebSocketHandlerDecoratorFactory {
    private final Logger logger;

    private static String stompFrameWithoutEnding(final String payload) {
        final var buf = new StringBuilder(payload);
        final var len = buf.length();
        // We expect "\n\n\0", but if a frame is partial, do not strip
        if ('\n' == buf.charAt(len - 3)
                && '\n' == buf.charAt(len - 2)
                && '\0' == buf.charAt(len - 1))
            buf.delete(len - 3, len);
        return buf.toString();
    }

    @EventListener
    public void handleBrokerAvailabilityEvents(
            final BrokerAvailabilityEvent event) {
        if (event.isBrokerAvailable())
            logger.debug("Web socket broker available");
        else
            logger.warn("Web socket broker not available");
    }

    @Nonnull
    @Override
    public WebSocketHandler decorate(
            @Nonnull final WebSocketHandler handler) {
        return new StompWebSocketHandlerDecorator(handler);
    }

    private class StompWebSocketHandlerDecorator
            extends WebSocketHandlerDecorator {
        public StompWebSocketHandlerDecorator(
                final WebSocketHandler handler) {
            super(handler);
        }

        @Override
        public void handleMessage(
                @Nonnull final WebSocketSession session,
                final WebSocketMessage<?> message)
                throws Exception {
            final var payloadForLogging =
                    stompFrameWithoutEnding((String) message.getPayload());

            logger.trace("{} -> {}:\n{}",
                    session.getRemoteAddress(),
                    session.getUri(),
                    payloadForLogging);

            super.handleMessage(session, message);
        }
    }
}
