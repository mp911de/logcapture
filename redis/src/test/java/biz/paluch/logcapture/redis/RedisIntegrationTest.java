package biz.paluch.logcapture.redis;

import static org.assertj.core.api.Assertions.assertThat;
import biz.paluch.logcapture.common.LogEntry;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RedisIntegrationTest {
    private static RedisMessageCollectionStore store;
    private static RedisClient redisClient;

    private static RedisConnection<String, String> redisConnection;

    @BeforeClass
    public static void beforeClass() {

        redisClient = new RedisClient("localhost", 6479);
        store = new RedisMessageCollectionFactory().createStore("redis://localhost:6479");
    }

    @Before
    public void before() throws Exception {

        redisConnection = redisClient.connect();
        redisConnection.flushdb();
        redisConnection.flushall();
    }

    @After
    public void after() throws Exception {
        redisConnection.close();

    }

    @AfterClass
    public static void afterClass() {

        store.close();
        redisClient.shutdown();
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
