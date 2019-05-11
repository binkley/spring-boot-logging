package x.loggy;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Extractor;
import brave.propagation.TraceContext.Injector;
import brave.propagation.TraceContextOrSamplingFlags;
import org.slf4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TraceResponseFilter
        extends OncePerRequestFilter {
    private final Tracer tracer;
    private final Extractor<HttpServletRequest> extractor;
    private final Injector<HttpServletResponse> injector;
    private final Logger logger;

    public TraceResponseFilter(final Tracing tracing, final Tracer tracer,
            final Logger logger) {
        extractor = tracing.propagation().extractor(
                HttpServletRequest::getHeader);
        injector = tracing.propagation().injector(
                HttpServletResponse::setHeader);
        this.tracer = tracer;
        this.logger = logger;
    }

    @Override
    public void doFilterInternal(final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        // First, so Spring Cloud bits can setup Sleuth
        chain.doFilter(request, response);

        final var compoundContext = compoundContext(
                currentContext(),
                extractor.extract(request));

        injector.inject(compoundContext, response);
    }

    private static TraceContext compoundContext(
            final TraceContext currentContext,
            final TraceContextOrSamplingFlags extraction) {
        return TraceContext.newBuilder()
                .debug(currentContext.debug())
                .parentId(currentContext.parentId())
                .sampled(currentContext.sampled())
                .spanId(currentContext.spanId())
                .traceId(workingTraceId(extraction, currentContext))
                .build();
    }

    private TraceContext currentContext() {
        var currentSpan = tracer.currentSpan();
        if (null != currentSpan) {
            logger.trace("Current tracing span: {}", currentSpan);
            return currentSpan.context();
        }
        currentSpan = tracer.newTrace();
        logger.trace("No current span; created: {}", currentSpan);
        return currentSpan.context();
    }

    private static long workingTraceId(
            final TraceContextOrSamplingFlags extraction,
            final TraceContext currentContext) {
        final TraceContext requestContext = extraction.context();
        return null == requestContext
                ? currentContext.traceId()
                : requestContext.traceId();
    }
}
