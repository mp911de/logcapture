package biz.paluch.logcapture.common.log4j;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.04.14 08:55
 */

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstallLog4jCollector {

    private Log4jCollectingAppender appender;

    public InstallLog4jCollector(String configString, String formatPattern) {
        appender = new Log4jCollectingAppender();
        appender.setTarget(configString);

        if (formatPattern != null && !formatPattern.trim().equals("")) {
            PatternLayout formatter = new PatternLayout();
            formatter.setConversionPattern(formatPattern);
            appender.setLayout(formatter);
        }

        // trigger initialization
        appender.getMessageCollectionStore();
    }

    /**
     * Install appender.
     */
    public void install() {
        uninstall();
        List<Logger> loggers = getLoggers();

        for (Logger logger : loggers) {
            logger.info("Added " + appender.getClass().getSimpleName() + " for Logger " + logger.getName());
        }
    }

    /**
     * Uninstall appender.
     */
    public void uninstall() {
        List<Logger> loggers = getLoggers();

        for (Logger logger : loggers) {
            removeAppender(logger);
        }
    }

    protected List<Logger> getLoggers() {
        Set<Logger> loggers = new HashSet<Logger>();
        loggers.add(LogManager.getRootLogger());
        Enumeration currentLoggers = LogManager.getCurrentLoggers();

        while (currentLoggers.hasMoreElements()) {
            Logger logger = (Logger) currentLoggers.nextElement();

            if (!logger.getAdditivity() || logger.getParent() == null) {
                loggers.add(logger);
            }
        }

        return new ArrayList<Logger>(loggers);
    }

    private void removeAppender(Logger logger) {
        Enumeration appenders = logger.getAllAppenders();
        while (appenders.hasMoreElements()) {
            Appender appender = (Appender) appenders.nextElement();
            if (appender.getClass().getName().equals(this.appender.getClass().getName())) {
                logger.removeAppender(appender);
                logger.info("Removed " + this.appender.getClass().getSimpleName() + " for Logger " + logger.getName());
            }
        }
    }
}
