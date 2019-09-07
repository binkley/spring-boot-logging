package x.loggy;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import static x.loggy.AlertMessage.Severity.MEDIUM;

@FeignClient(name = "conflict",
        url = "http://localhost:8080/feign/conflict")
public interface ConflictRemote {
    @AlertMessage(message = "CONFLICTED", severity = MEDIUM)
    @PostMapping
    @Timed("conflict.remote")
    void postConflict();
}
