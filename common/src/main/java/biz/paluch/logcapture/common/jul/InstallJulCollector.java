package biz.paluch.logcapture.common.jul;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.04.14 08:55
 */

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class InstallJulCollector {

    private JulCollectingHandler julCollectingHandler;

    public InstallJulCollector(String configString, String formatPattern) {
        julCollectingHandler = new JulCollectingHandler();
        julCollectingHandler.setTarget(configString);

        if (formatPattern != null && !formatPattern.trim().equals("")) {
            SimpleFormatter formatter = new SimpleFormatter();
            formatter.setFormat(formatPattern);
            julCollectingHandler.setFormatter(formatter);
        }

        // trigger initialization
        julCollectingHandler.getMessageCollectionStore();

    }

    /**
     * Install Collector.
     * 
     */
    public void install() {
        List<Logger> loggers = getLoggers();

        for (Logger logger : loggers) {
            removeAppender(logger);
        }

        for (Logger logger : loggers) {
            logger.addHandler(julCollectingHandler);
            logger.info("Added " + julCollectingHandler.getClass().getSimpleName() + " for Logger " + logger.getName());
        }
    }

    /**
     * Uninstall collector
     */
    public void uninstall() {
        List<Logger> loggers = getLoggers();

        for (Logger logger : loggers) {
            removeAppender(logger);
        }
    }

    protected List<Logger> getLoggers() {
        Set<Logger> loggers = new HashSet<Logger>();

        LogManager logManager = LogManager.getLogManager();
        loggers.add(logManager.getLogger(""));

        /*
         * Class cls = logManager.getClass(); while (!cls.equals(Object.class)) { try { Field rootLogger =
         * cls.getDeclaredField("rootLogger"); rootLogger.setAccessible(true); loggers.add((Logger) rootLogger.get(logManager));
         * break; } catch (Exception e) { cls = cls.getSuperclass(); } }
         */

        Enumeration<String> currentLoggers = logManager.getLoggerNames();
        while (currentLoggers.hasMoreElements()) {
            Logger logger = logManager.getLogger(currentLoggers.nextElement());

            if (!logger.getUseParentHandlers() || logger.getParent() == null) {
                loggers.add(logger);
            }
        }

        return new ArrayList<Logger>(loggers);
    }

    private void removeAppender(Logger logger) {
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            if (handler.getClass().getName().equals(julCollectingHandler.getClass().getName())) {
                logger.removeHandler(handler);
                logger.info("Removed " + julCollectingHandler.getClass().getSimpleName() + " for Logger " + logger.getName());
            }
        }
    }
}
