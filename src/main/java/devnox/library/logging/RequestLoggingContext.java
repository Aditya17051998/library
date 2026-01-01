package devnox.library.logging;


import org.slf4j.MDC;
import java.util.ArrayList;
import java.util.List;


public class RequestLoggingContext {

    private static final ThreadLocal<RequestLoggingContext> contextHolder =
            ThreadLocal.withInitial(RequestLoggingContext::new);

    private String userName;
    private String flowName;

    // NEW: message counter
    private int messageCounter = 0;

    public static RequestLoggingContext getCurrentContext() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
        MDC.clear(); // cleanup MDC too
    }

    public void applyToMdc(String action) {

        if(WhiteListedKeys.checkIfWhiteListed("userName")){
                MDC.put("userName", userName);
        }else{
            MDC.put("userName", "FILTERED");
        }

        if(WhiteListedKeys.checkIfWhiteListed("flowName")){
                MDC.put("flowName", flowName);
        }else{
            MDC.put("flowName", "FILTERED");
        }
        
        // increment message counter every log
        messageCounter++;
        MDC.put("message_number", String.valueOf(messageCounter));

        MDC.put("action", action);
    }

    // --- Getters ---
    public String getMerchantName() { return userName; }
    public String getFlowName() { return flowName; }

    // --- Setters ---
    public void setMerchantName(String userName) { this.userName = userName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }

}
