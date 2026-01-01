package devnox.library.logging;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

public class StaticLoggingContext {

    @Value("${spring.boot.config.type:NULL}")
    private static String environment;

    @Value("${spring.boot.config.applicationName:NULL}")
    private static String applicationName;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("STATIC_LOGGER");
    private static final org.apache.logging.log4j.Logger filteredLogger = LogManager.getLogger(StaticLoggingContext.class);
    private static final int MAX_LOG_BODY_SIZE = 5000; // 5KB

    private static void applyToMdc() {
        MDC.put("applicationName", applicationName);
        MDC.put("environment", environment);
    }

    private static void putMdc(String action) {

        applyToMdc();
        MDC.put("action", action);
    }

    public static String truncateIfLong(String message) {
        if(message.length() > MAX_LOG_BODY_SIZE){
            return message.substring(0, MAX_LOG_BODY_SIZE) + "...[TRUNCATED]" ;
        }
        return message;
    }


    @Description("Extended log")
    public static void infoE(String action, String message, Object ...params) {
        if (!logger.isInfoEnabled()) return;
        putMdc(action);
        logger.info(message, params);
        MDC.remove("action");  // 👈 only remove action, keep global ones
    }

    public static void infoLogT(String action, String message) {
        if (!logger.isInfoEnabled()) return;
        putMdc(action);

        logger.info(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    @Description("Extended log")
    public static void debugE(String action, String message, Object ...params) {
        if (!logger.isDebugEnabled()) return;
        putMdc(action);
        logger.debug(message, params);
        MDC.remove("action");  // 👈 only remove action, keep global ones
    }

    public static void debugLogT(String action, String message) {
        if (!logger.isDebugEnabled()) return;
        putMdc(action);
        logger.debug(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    @Description("Extended log")
    public static void errorE(String action, String message, Object ...params) {
        if (!logger.isErrorEnabled()) return;
        putMdc(action);
        logger.error(message, params);
        MDC.remove("action");  // 👈 only remove action, keep global ones
    }

    public static void errorLogT(String action, String message) {
        if (!logger.isErrorEnabled()) return;
        putMdc(action);
        logger.error(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    @Description("Extended log")
    public static void warnE(String action, String message, Object ...params) {
        if (!logger.isWarnEnabled()) return;
        putMdc(action);
        logger.warn(message, params);
        MDC.remove("action");  // 👈 only remove action, keep global ones
    }

    public static void warnLogT(String action, String message) {
        if (!logger.isWarnEnabled()) return;
        putMdc(action);
        logger.warn(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    @Description("Extended log")
    public static void criticalE(String action, String message, Object ...params) {
        putMdc(action);
        logger.error("[CRITICAL] " + message, params);
        MDC.remove("action");  // 👈 only remove action, keep global ones
    }

    public static void criticalLogT(String action, String message) {
        putMdc(action);
        logger.error("[CRITICAL] " + truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public static <T> void infoLogV(String action, T message) {
        if (!filteredLogger.isInfoEnabled()) return;
        putMdc(action);
        filteredLogger.info(WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public static <T> void debugLogV(String action, T message) {
        if (!filteredLogger.isDebugEnabled()) return;
        putMdc(action);
        filteredLogger.debug(WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public static <T> void errorLogV(String action, T message) {
        if (!filteredLogger.isErrorEnabled()) return;
        putMdc(action);
        filteredLogger.error(WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public static <T> void warnLogV(String action, T message) {
        if (!filteredLogger.isWarnEnabled()) return;
        putMdc(action);
        filteredLogger.warn(WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public static <T> void criticalLogV(String action, T message) {
        putMdc(action);
        filteredLogger.error("[CRITICAL] " + WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

}

