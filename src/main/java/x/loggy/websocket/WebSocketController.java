package x.loggy.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import x.loggy.NewWebSocketMessage;

import javax.validation.Valid;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.ACCEPTED;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/message")
public class WebSocketController {
    private final SimpMessagingTemplate messaging;

    //    @ApiResponses(@ApiResponse(code = 202, message =
    //            "Successfully posted new web socket push message"))
    @PostMapping("new-message")
    @ResponseStatus(ACCEPTED)
    public void newMessage(
            @RequestBody final @Valid NewWebSocketMessage newMessage) {
        messaging.convertAndSend(
                format("/websocket/new-message/%s", newMessage.getSubject()),
                newMessage);
    }
}
