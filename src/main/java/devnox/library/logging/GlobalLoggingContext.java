package devnox.library.logging;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class GlobalLoggingContext {

    @Value("${spring.boot.config.type:NULL}")
    private String environment;

    @Value("${spring.boot.config.applicationName:NULL}")
    private String applicationName;

    @PostConstruct
    public void init() {
        String hostName = System.getenv("HOSTNAME"); // try to get instance id or thread
        MDC.put("applicationName", applicationName);
        MDC.put("environment", environment);
        MDC.put("hostName", hostName);
    }

    public void applyToMdc() {
        String hostName = System.getenv("HOSTNAME"); // try to get instance id or thread
        MDC.put("applicationName", applicationName);
        MDC.put("environment", environment);
        MDC.put("hostName", hostName);
    }
}
