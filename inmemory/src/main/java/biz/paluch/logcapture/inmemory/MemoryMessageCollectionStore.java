package biz.paluch.logcapture.inmemory;

import biz.paluch.logcapture.common.LogEntry;
import biz.paluch.logcapture.common.MessageCollectionStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.04.14 13:07
 */
public class MemoryMessageCollectionStore implements MessageCollectionStore {

    private final Map<String, List<LogEntry>> collector = new ConcurrentHashMap<String, List<LogEntry>>();
    private final Set<String> collectForList = new HashSet<String>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        collectForList.clear();
        collector.clear();
    }

    /**
     * @param item
     */
    public void startCollect(String item) {
        synchronized (collectForList) {
            collector.put(item, new ArrayList<LogEntry>());
            collectForList.add(item);
        }
    }

    /**
     * @param item
     * @return StringBuffer or null.
     */
    public List<LogEntry> getLogsFor(String item) {
        List<LogEntry> result = new ArrayList<LogEntry>();
        if (collector.containsKey(item)) {
            result.addAll(collector.get(item));
        }

        return result;
    }

    @Override
    public void stopCollect(String item) {
        synchronized (collectForList) {
            collector.remove(item);
            collectForList.remove(item);
        }
    }

    /**
     * @return the collectForList
     */
    public Set<String> getActiveCollectors() {
        return new HashSet<String>(collectForList);
    }

    @Override
    public boolean isLogCollectorOn() {
        return !collectForList.isEmpty();
    }

    @Override
    public void append(LogEntry logEntry) {
        synchronized (collectForList) {
            for (String item : collectForList) {

                List<LogEntry> buffer = collector.get(item);
                if (buffer == null) {
                    continue;
                }

                buffer.add(logEntry);
            }
        }
    }
}
