package x.loggy.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import x.loggy.Alerter;
import x.loggy.configuration.LoggingConfiguration;
import x.loggy.configuration.WebSocketConfiguration;

import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static x.loggy.TestFixtures.newWebSocketMessage;

@Import({
        Alerter.class,
        LoggingConfiguration.class,
        WebSocketConfiguration.class})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@WebMvcTest(WebSocketController.class)
class WebSocketControllerTest {
    private final MockMvc mvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private SimpMessagingTemplate message;

    @Test
    void shouldPostUpdate()
            throws Exception {
        final var newMessage = newWebSocketMessage();

        mvc.perform(post("/message/new-message")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(newMessage)))
                .andExpect(status().isAccepted());

        verify(message).convertAndSend(
                "/websocket/new-message/" + newMessage.getTopic(),
                newMessage);
    }
}
