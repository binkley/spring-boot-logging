package x.loggy;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestFixtures {
    public static NewWebSocketMessage newWebSocketMessage() {
        return NewWebSocketMessage.builder()
                .message("I saw one yesterday!")
                .topic("unicorns")
                .build();
    }
}
