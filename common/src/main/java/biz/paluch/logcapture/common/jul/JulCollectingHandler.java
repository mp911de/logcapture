package biz.paluch.logcapture.common.jul;

import biz.paluch.logcapture.common.LogEntry;
import biz.paluch.logcapture.common.MessageCollectionStore;
import biz.paluch.logcapture.common.Service;
import biz.paluch.logcapture.common.StackTraceFilter;
import biz.paluch.logcapture.common.StaticCollectionStoreInstance;

import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;

/**
 * Proxy for Log4j Appender.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class JulCollectingHandler extends MemoryHandler {

    private MessageCollectionStore messageCollectionStore;
    private String target;
    private ThreadLocal<Boolean> activity = new ThreadLocal<Boolean>();

    /**
     *
     */
    public JulCollectingHandler() {
        super(new NullHandler(), 1, Level.ALL);
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();
        setTarget(manager.getProperty(cname + ".configString"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(LogRecord record) {

        if (!isLoggable(record)) {
            return;
        }

        if ((activity.get() != null && activity.get().booleanValue()) || isRecursiveLogEvent(record)) { // discard
                                                                                                        // recursion/reentrant
            return;
        }

        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        if (record.getThrown() != null) {

            String filteredStackTrace = StackTraceFilter.getFilteredStackTrace(record.getThrown());
            msg += filteredStackTrace;
        }

        try {
            activity.set(Boolean.TRUE);
            if (getMessageCollectionStore().isLogCollectorOn() && !getMessageCollectionStore().getActiveCollectors().isEmpty()) {

                LogEntry entry = new LogEntry(record.getMillis(), msg);

                messageCollectionStore.append(entry);
            }
        } catch (Exception ex) {
            // We don't want to throw an exception here, but we
            // report the exception to any registered ErrorManager.
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        } finally {
            activity.remove();
        }

    }

    private boolean isRecursiveLogEvent(LogRecord event) {
        if (event.getLoggerName().contains("netty") || event.getLoggerName().contains("redis")) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SecurityException {
        if (messageCollectionStore != null) {
            messageCollectionStore.close();
            messageCollectionStore = null;
        }
    }

    public MessageCollectionStore getMessageCollectionStore() {
        if (messageCollectionStore == null) {
            setMessageCollectionStore(Service.getFactory().createStore(target));
        }

        return messageCollectionStore;
    }

    public void setMessageCollectionStore(MessageCollectionStore messageCollectionStore) {
        close();
        StaticCollectionStoreInstance.setMessageCollectionStore(messageCollectionStore);
        this.messageCollectionStore = messageCollectionStore;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
