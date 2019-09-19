package x.loggy;

import org.slf4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class SimulationResponseFilter
        extends OncePerRequestFilter {
    private final Duration delay;
    private final Logger logger;

    public SimulationResponseFilter(
            final Duration delay,
            final Logger logger) {
        this.delay = delay;
        this.logger = logger;
    }

    @Override
    public void doFilterInternal(final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        logger.debug("Simulating slow response of {} to {}",
                delay, request.getRequestURL());

        try {
            // Duration is in seconds + leftover nanos of a second
            // Sleep is in millis + leftover nanos of a milli
            sleep(delay.getSeconds() * 1_000, delay.getNano() % 1_000_000);
        } catch (final InterruptedException e) {
            logger.error("Interrupted while sleeping in simulation");
            currentThread().interrupt();
        }

        chain.doFilter(request, response);
    }
}
