package biz.paluch.logcapture.common.log4j;

import biz.paluch.logcapture.common.LogEntry;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import biz.paluch.logcapture.common.MessageCollectionStore;
import biz.paluch.logcapture.common.Service;
import biz.paluch.logcapture.common.StackTraceFilter;
import biz.paluch.logcapture.common.StaticCollectionStoreInstance;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class Log4jCollectingAppender extends AppenderSkeleton {

    private MessageCollectionStore messageCollectionStore;
    private String target;
    private ThreadLocal<Boolean> activity = new ThreadLocal<Boolean>();

    /**
     * Hold the last instance here.
     */
    public Log4jCollectingAppender() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unused")
    @Override
    protected void append(LoggingEvent event) {

        if ((activity.get() != null && activity.get().booleanValue()) || isRecursiveLogEvent(event)) { // discard
                                                                                                       // recursion/reentrant
            return;
        }

        try {
            activity.set(Boolean.TRUE);

            if (getMessageCollectionStore().isLogCollectorOn() && !getMessageCollectionStore().getActiveCollectors().isEmpty()) {

                StringBuffer message = new StringBuffer();

                message.append(layout.format(event));
                if (layout.ignoresThrowable() && event.getThrowableInformation() != null
                        && event.getThrowableInformation().getThrowable() != null) {

                    String filteredStackTrace = StackTraceFilter.getFilteredStackTrace(event.getThrowableInformation()
                            .getThrowable());

                    message.append(filteredStackTrace);
                }
                LogEntry entry = new LogEntry(event.getTimeStamp(), message.toString());

                getMessageCollectionStore().append(entry);
            }
        } catch (Exception e) {
            // ignore silently
        } finally {

            activity.remove();

        }
    }

    private boolean isRecursiveLogEvent(LoggingEvent event) {
        if (event.getLoggerName().contains("netty") || event.getLoggerName().contains("redis")) {
            return true;
        }

        return false;
    }

    @Override
    public void close() {

        try {
            if (messageCollectionStore != null) {
                messageCollectionStore.close();
                messageCollectionStore = null;
            }
        } catch (Exception e) {
            // ignore silently.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresLayout() {
        return true;
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
        close();
        this.target = target;
    }
}
