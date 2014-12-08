package biz.paluch.logcapture.common;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.04.14 13:06
 */
public interface MessageCollectionStore {

    void close();

    /**
     * Start collecting for a particular item.
     * 
     * @param item
     */
    void startCollect(String item);

    /**
     * @param item
     * @return List or null.
     */
    public List<LogEntry> getLogsFor(String item);

    /**
     * End collecting for a particular item.
     * 
     * @param item
     */
    void stopCollect(String item);

    /**
     * @return the collectForList
     */
    Set<String> getActiveCollectors();

    /**
     *
     * @return true if general collector is switched on
     */
    boolean isLogCollectorOn();

    /**
     * Append a message to the store.
     * 
     * @param message
     */
    void append(LogEntry logEntry);
}
