package x.loggy.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor(access = PRIVATE)
class WebSocketTester<T>
        extends StompSessionHandlerAdapter {
    private static final WebSocketHttpHeaders emptyHeaders
            = new WebSocketHttpHeaders();

    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicReference<Throwable> failure
            = new AtomicReference<>();

    private final T expectedUpdate;
    private final String websocketDestination;
    private final WebSocketUpdatePoster webSocketUpdate;

    // Only internally mutable -- not externally visible
    private ListenableFuture<StompSession> session;

    /**
     * Creates a new {@code WebSocketTester}, connects a web socket to the
     * push service, and subscribes to the web socket destination for
     * updates.
     *
     * @param expectedUpdate the expected update (JSON DTO)
     * @param webSocketDestination the subscription path for web socket
     * updates
     * @param webSocketUpdatePoster the callback for simulating webSocket
     * updates (REST call)
     * @param websocketPort the random port Spring Boot test set up for the
     * test
     * @param messageConverter injected from Spring Boot, for converting JSON
     * DTOs
     * @param <T> the type of expected update published to web sockets (JSON
     * DTO)
     *
     * @return the tester, never {@code null}
     *
     * @see #assertDeliveryOfUpdate() to publish webSocket update, and assert
     * the update
     */
    static <T> WebSocketTester<T> webSocketTester(
            final T expectedUpdate,
            final String webSocketDestination,
            final WebSocketUpdatePoster webSocketUpdatePoster,
            final int websocketPort,
            final MessageConverter messageConverter) {
        final var tester = new WebSocketTester<T>(expectedUpdate,
                webSocketDestination,
                webSocketUpdatePoster);

        final var sockJsClient = new SockJsClient(List.of(
                new WebSocketTransport(new StandardWebSocketClient())));
        final var stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(messageConverter);

        tester.session = stompClient.connect(
                format("ws://localhost:%d/websocket", websocketPort),
                emptyHeaders, tester);

        return tester;
    }

    void assertDeliveryOfUpdate()
            throws InterruptedException, ExecutionException {
        try {
            if (latch.await(5, SECONDS)) {
                if (failure.get() != null) {
                    throw new AssertionError(
                            "Update does not correspond to webSocket "
                                    + "update",
                            failure.get());
                }
            } else {
                fail("Did not receive update after webSocket sent update");
            }
        } finally {
            session.get().disconnect();
        }
    }

    @Override
    public void handleFrame(final StompHeaders headers,
            final Object payload) {
        failure.set(new Exception(headers.toString()));
    }

    @Override
    public void afterConnected(final StompSession session,
            final StompHeaders connectedHeaders) {
        session.subscribe(websocketDestination,
                new AssertDeliveryOfUpdate(session));

        try {
            webSocketUpdate.call();
        } catch (final Throwable t) {
            failure.set(t);
            latch.countDown();
        }
    }

    @Override
    public void handleException(final StompSession s, final StompCommand c,
            final StompHeaders h, final byte[] p, final Throwable ex) {
        failure.set(ex);
    }

    @Override
    public void handleTransportError(final StompSession session,
            final Throwable ex) {
        failure.set(ex);
    }

    @RequiredArgsConstructor
    private class AssertDeliveryOfUpdate
            implements StompFrameHandler {
        private final StompSession session;

        @Override
        public Type getPayloadType(final StompHeaders headers) {
            return expectedUpdate.getClass();
        }

        @Override
        public void handleFrame(final StompHeaders headers,
                final Object receivedMessage) {
            try {
                assertThat(receivedMessage).isEqualTo(expectedUpdate);
            } catch (final Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        }
    }
}
