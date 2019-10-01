package x.loggy.websocket;

import java.io.IOException;

@FunctionalInterface
public interface WebSocketUpdatePoster {
    void call()
            throws IOException, InterruptedException;
}
