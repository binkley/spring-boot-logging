package x.loggy;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = NewWebSocketMessage.Builder.class)
public class NewWebSocketMessage {
    private final @NotNull String topic;
    private final @NotNull String message;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
