package biz.paluch.logcapture.inmemory;

import static org.assertj.core.api.Assertions.assertThat;
import biz.paluch.logcapture.common.LogEntry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class MemoryIntegrationTest {
    private static MemoryMessageCollectionStore store;

    @BeforeClass
    public static void beforeClass() {

        store = new MemoryMessageCollectionFactory().createStore("redis://localhost:6479");
    }

    @AfterClass
    public static void afterClass() {

        store.close();
    }

    @Test
    public void testActiveCollectors() throws Exception {
        assertThat(store.getActiveCollectors()).isEmpty();
        assertThat(store.isLogCollectorOn()).isFalse();

        store.startCollect("item");

        assertThat(store.getActiveCollectors()).hasSize(1).contains("item");
        assertThat(store.isLogCollectorOn()).isTrue();

        store.stopCollect("item");

        assertThat(store.getActiveCollectors()).isEmpty();
        assertThat(store.isLogCollectorOn()).isFalse();

    }

    @Test
    public void testAppendMessages() throws Exception {
        store.startCollect("item");

        store.append(new LogEntry(1, "message 1"));
        store.append(new LogEntry(2, "message 2"));
        List<LogEntry> entries = store.getLogsFor("item");

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getTimestamp()).isEqualTo(1);
        assertThat(entries.get(1).getTimestamp()).isEqualTo(2);

        store.stopCollect("item");

        entries = store.getLogsFor("item");
        assertThat(entries).hasSize(0);

    }
}
