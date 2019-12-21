package x.loggy.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MessageConverter;
import x.loggy.NewWebSocketMessage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static x.loggy.TestFixtures.newWebSocketMessage;
import static x.loggy.websocket.WebSocketTester.webSocketTester;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = RANDOM_PORT,
        properties = "loggy.enable-demo=false")
class WebSocketLiveTest {
    private static final NewWebSocketMessage webSocketUpdate
            = newWebSocketMessage();
    private static final String subject = webSocketUpdate.getSubject();

    private final MessageConverter messageConverter;
    private final ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    @Test
    void shouldDeliverUpdateToWebSocketWhenControllerReceivesUpdate()
            throws InterruptedException, ExecutionException {
        final var tester = webSocketTester(webSocketUpdate,
                format("/websocket/new-message/%s", subject),
                this::postWebSocketUpdate, port, messageConverter);

        tester.assertDeliveryOfUpdate();
    }

    private void postWebSocketUpdate()
            throws IOException, InterruptedException {
        final var newMessageFromWebSocket = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(
                        objectMapper.writeValueAsString(webSocketUpdate),
                        UTF_8))
                .uri(URI.create(format(
                        "http://localhost:%d/message/new-message",
                        port)))
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .build();

        newHttpClient().send(newMessageFromWebSocket, discarding());
    }
}
