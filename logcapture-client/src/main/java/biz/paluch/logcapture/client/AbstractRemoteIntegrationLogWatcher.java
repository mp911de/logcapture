package biz.paluch.logcapture.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.rules.TestName;
import org.junit.runner.Description;

import biz.paluch.logcapture.ws.LogControlService;
import biz.paluch.logcapture.ws.LogEntry;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public abstract class AbstractRemoteIntegrationLogWatcher extends TestName {

    private Map<String, LogControlService> services;

    /**
     *
     */
    public AbstractRemoteIntegrationLogWatcher() {
        super();
    }

    /**
     * /** {@inheritDoc}
     */
    @Override
    protected void starting(Description d) {
        super.starting(d);
        if (services == null) {
            services = createServices();
        }

        Map.Entry<String, LogControlService> lastEntry = null;
        try {

            String className = d.getClassName();

            for (Map.Entry<String, LogControlService> entry : services.entrySet()) {
                lastEntry = entry;
                LogControlService service = entry.getValue();
                try {
                    service.startCollect(d.getClassName() + "." + d.getMethodName());
                } catch (Exception e) {
                    warn("Cannot obtain Remote logs for: " + lastEntry.getKey() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // exceptions are not interesting, because when we get no logs we cannot make something about that.
        }

    }

    protected void warn(String message) {
        System.err.println(message);
    }

    protected void log(String message) {
        System.err.println(message);
    }

    protected abstract Map<String, LogControlService> createServices();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void failed(Throwable e, Description d) {
        super.failed(e, d);
        collectAndPrintLogs(d);
    }

    private void collectAndPrintLogs(Description d) {
        try {

            Map<String, String> results = endCollect(d);

            if (!results.isEmpty()) {
                log("--------------------------------------------------------");
                log("Remote Logs for Method " + d.getTestClass().getSimpleName() + "." + d.getMethodName());
                log("--------------------------------------------------------");
            }

            for (Map.Entry<String, String> entry : results.entrySet()) {

                String[] lines = entry.getValue().trim().split("\n");
                for (String logLine : lines) {
                    String[] lines2 = logLine.split("\r");
                    for (String logLine2 : lines2) {
                        log("[" + entry.getKey() + "] " + logLine2);
                    }
                }
            }

        } catch (Exception ex) {
            // exceptions are not interesting, because when we get no logs we cannot make something about that.: handle
            // exception
        }
    }

    private Map<String, String> endCollect(Description d) {
        Map<String, String> results = new HashMap<String, String>();

        for (Map.Entry<String, LogControlService> entry : services.entrySet()) {
            String key = entry.getKey();
            try {
                List<LogEntry> entries = entry.getValue().stopCollect(d.getClassName() + "." + d.getMethodName());
                StringBuilder builder = new StringBuilder();
                if (entries != null) {
                    for (LogEntry logEntry : entries) {
                        builder.append(logEntry.getMessage().replaceAll("\r\n", "\n").replaceAll("\r", "\n")
                                .replaceAll("\n\n", "\n"));
                    }
                }
                if (!builder.toString().equals("")) {
                    results.put(key, builder.toString());
                }
            } catch (Exception e) {
                results.put(key, "NO REMOTE LOGS: " + e.getMessage());
            }

        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void succeeded(Description d) {
        super.succeeded(d);
        collectAndPrintLogs(d);
        try {
            endCollect(d);
        } catch (Exception e) {
            // exceptions are not interesting, because when we get no logs we cannot make something about that.: handle
            // exception
        }

    }

    public Map<String, LogControlService> getServices() {
        return services;
    }

    public void setServices(Map<String, LogControlService> services) {
        this.services = services;
    }
}
