package x.loggy.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

import static org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace.ALWAYS;

@Configuration
@EnableAutoConfiguration(exclude = ErrorMvcAutoConfiguration.class)
public class ProblemConfiguration {
    private static boolean includeStackTrace(final ServerProperties server) {
        return ALWAYS == server.getError().getIncludeStacktrace();
    }

    @Bean
    public ProblemModule problemModule(final ServerProperties server) {
        return new ProblemModule()
                .withStackTraces(includeStackTrace(server));
    }

    @Bean
    public ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }
}
