package x.loggy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static x.loggy.TestFixtures.newWebSocketMessage;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class NewWebSocketMessageJsonTest {
    private final ObjectMapper objectMapper;

    @Test
    void shouldRoundTrip()
            throws IOException {
        final var expected = newWebSocketMessage();

        final var json = objectMapper.writeValueAsString(expected);
        final var actual = objectMapper
                .readValue(json, NewWebSocketMessage.class);

        assertThat(actual).isEqualTo(expected);
    }
}
