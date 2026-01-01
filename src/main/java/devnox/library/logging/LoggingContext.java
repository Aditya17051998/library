
package devnox.library.logging;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class LoggingContext {

    private static final org.slf4j.Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");
    private static final org.slf4j.Logger dbLogger = LoggerFactory.getLogger("DB_LOGGER");
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("MESSAGE_LOGGER");
    private static final org.apache.logging.log4j.Logger filteredLogger = LogManager.getLogger(LoggingContext.class);

    private static final int MAX_LOG_BODY_SIZE = 5000; // 5KB

    @Autowired
    GlobalLoggingContext globalContext;

    private void putMdc(String action) {
        globalContext.applyToMdc();
        RequestLoggingContext.getCurrentContext().applyToMdc(action);
    }

    public static String truncateIfLong(String message) {
        if(message.length() > MAX_LOG_BODY_SIZE){
            return message.substring(0, MAX_LOG_BODY_SIZE) + "...[TRUNCATED]" ;
        }
        return message;
    }

    public void apiLog(String action, String message) {
        if (!apiLogger.isInfoEnabled()) return;
        putMdc(action);
        apiLogger.info(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public void dbLog(String action, String message) {
        if (!dbLogger.isInfoEnabled()) return;

        putMdc(action);
        dbLogger.info(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    @Description("Extended log")
    public void infoE(String action, String message, Object ...params) {
        if(!logger.isInfoEnabled()) return;
        putMdc(action);
        logger.info(message, params);
        MDC.remove("action");
    }

    public void infoLogT(String action, String message) {
        if (!logger.isInfoEnabled()) return;
        putMdc(action);
        logger.info(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    @Description("Extended log")
    public void debugE(String action, String message, Object ...params) {
        if(!logger.isDebugEnabled()) return;
        putMdc(action);
        logger.info(message, params);
        MDC.remove("action");
    }

    public void debugLogT(String action, String message) {
        if (!logger.isDebugEnabled()) return;
        putMdc(action);
        logger.debug(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones
    }

    @Description("Extended log")
    public void errorE(String action, String message, Object ...params) {
        if(!logger.isErrorEnabled()) return;
        putMdc(action);
        logger.error(message, params);
        MDC.remove("action");
    }

    public void errorLogT(String action, String message) {
        if (!logger.isErrorEnabled()) return;
        putMdc(action);
        logger.error(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    @Description("Extended log")
    public void warnE(String action, String message, Object ...params) {
        if(!logger.isWarnEnabled()) return;
        putMdc(action);
        logger.warn(message, params);
        MDC.remove("action");
    }

    public void warnLogT(String action, String message) {
        if (!logger.isWarnEnabled()) return;
        putMdc(action);
        logger.warn(truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    @Description("Extended log")
    public void criticalE(String action, String message, Object ...params) {
        putMdc(action);
        logger.error("[CRITICAL] " + message, params);
        MDC.remove("action");
    }

    public void criticalLogT(String action, String message) {
        putMdc(action);
        logger.error("[CRITICAL] " + truncateIfLong(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public <T> void infoLogV(String action, T message) {
        if (!filteredLogger.isInfoEnabled()) return;
        putMdc(action);
        filteredLogger.info(WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public <T> void debugLogV(String action, T message) {
        if (!filteredLogger.isDebugEnabled()) return;
        putMdc(action);
        filteredLogger.debug(WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public <T> void errorLogV(String action, T message) {
        if (!filteredLogger.isErrorEnabled()) return;
        putMdc(action);
        filteredLogger.error(WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public <T> void warnLogV(String action, T message) {
        if (!filteredLogger.isWarnEnabled()) return;
        putMdc(action);
        filteredLogger.warn(WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

    public <T> void criticalLogV(String action, T message) {
        putMdc(action);
        filteredLogger.error("[CRITICAL] " + WhiteListedKeys.filteredWhiteListedFields(message));
        MDC.remove("action");  // 👈 only remove action, keep global ones

    }

}
