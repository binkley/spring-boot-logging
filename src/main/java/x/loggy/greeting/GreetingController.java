package x.loggy.greeting;

import lombok.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Controller
public class GreetingController {
    @MessageMapping("/hello")
    @SendTo("/websocket/greetings")
    public Greeting greeting(final HelloMessage message)
            throws InterruptedException {
        sleep(1000); // simulated delay
        return new Greeting(format("Hello, %s!", htmlEscape(message.name)));
    }

    @Value
    public static class Greeting {
        public String content;
    }

    @Value
    public static class HelloMessage {
        public String name;
    }
}
