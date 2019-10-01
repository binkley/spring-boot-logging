package x.loggy.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.SimpleMessageConverter;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import javax.annotation.Nonnull;

import static org.springframework.core.NestedExceptionUtils.getMostSpecificCause;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StompLogging
        implements WebSocketHandlerDecoratorFactory {
    private final ObjectMapper objectMapper;
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

    private static void appendHeaders(final StringBuilder buf,
            final MessageHeaders headers) {
        headers.forEach((key, value) ->
                buf.append(key).append(": ").append(value).append('\n'));
        buf.append('\n');
    }

    private static <T> T letNextConverterReallyConvert() {
        return null;
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

    public LoggingStompMessageConverter loggingStompMessageConverter() {
        return new LoggingStompMessageConverter();
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

    private class LoggingStompMessageConverter
            extends SimpleMessageConverter {
        @Override
        public Object fromMessage(final Message<?> message,
                final Class<?> targetClass) {
            try {
                final var buf = new StringBuilder();
                appendHeaders(buf, message.getHeaders());
                buf.append(objectMapper.writeValueAsString(
                        message.getPayload()));

                // TODO: No session info?
                logger.trace("Received:\n{}", buf.toString());
            } catch (final JsonProcessingException e) {
                logger.error(
                        "This is not the payload you are looking for: {}: {}",
                        getMostSpecificCause(e), message.getPayload());
            }

            return letNextConverterReallyConvert();
        }

        @Override
        public Message<?> toMessage(final Object payload,
                final MessageHeaders headers) {
            try {
                final var buf = new StringBuilder();
                appendHeaders(buf, headers);
                buf.append(objectMapper.writeValueAsString(payload));

                // TODO: No session info?
                logger.trace("Sent:\n{}", buf);
            } catch (final JsonProcessingException e) {
                logger.error(
                        "This is not the payload you are looking for: {}: {}",
                        getMostSpecificCause(e), payload);
            }

            return letNextConverterReallyConvert();
        }
    }
}
