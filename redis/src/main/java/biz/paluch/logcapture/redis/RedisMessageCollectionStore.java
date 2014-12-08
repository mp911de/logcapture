package biz.paluch.logcapture.redis;

import biz.paluch.logcapture.common.LogEntry;
import biz.paluch.logcapture.common.MessageCollectionStore;
import com.google.common.collect.Maps;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.04.14 13:15
 */
public class RedisMessageCollectionStore implements MessageCollectionStore {

    private RedisClient redisClient;
    private String listPrefix;
    private int expiry = 360;
    private RedisSet collectForList;
    private RedisConnection<String, String> connection;

    public RedisMessageCollectionStore(String listPrefix, URI redisAddress) {
        this.listPrefix = listPrefix;

        // discard any logs from the redis client to prevent infinite logging loops.
        InternalLoggerFactory.setDefaultFactory(new NoOpLoggerFactory());

        RedisURI.Builder builder = RedisURI.Builder.redis(redisAddress.getHost(), redisAddress.getPort());
        if (redisAddress.getUserInfo() != null) {
            builder.withPassword(redisAddress.getUserInfo());
        }

        redisClient = new RedisClient(builder.build());

        initialize();
    }

    protected void initialize() {
        connection = redisClient.connect();
        collectForList = new RedisSet(connection, getKey("Collectors"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

        connection.close();
        for (String collector : collectForList) {
            getCollector(collector).clear();
        }

        collectForList.clear();
        redisClient.shutdown();
    }

    private RedisList getCollector(String collector) {
        return new RedisList(connection, getKey("for_" + collector));
    }

    /**
     * @param item
     */
    public void startCollect(String item) {
        collectForList.add(item);
        collectForList.expire(expiry, TimeUnit.SECONDS);

    }

    private String getKey(String item) {
        return listPrefix + "_" + getClass().getSimpleName() + "_" + item;
    }

    public List<LogEntry> getLogsFor(String item) {
        List<LogEntry> result = new ArrayList<LogEntry>();
        getCollector(item).expire(expiry, TimeUnit.SECONDS);
        JSONParser parser = new JSONParser();
        for (String entry : getCollector(item)) {

            LogEntry logEntry = new LogEntry();

            try {
                Map<String, Object> map = (Map<String, Object>) parser.parse(entry);
                logEntry.setTimestamp(((Number) map.get("timestamp")).longValue());
                logEntry.setMessage((String) map.get("message"));
            } catch (ParseException e) {
                logEntry.setTimestamp(System.currentTimeMillis());
                logEntry.setMessage("Could not parse " + entry + " (" + e.toString() + ")");
            }
            result.add(logEntry);
        }

        Collections.sort(result, new Comparator<LogEntry>() {
            @Override
            public int compare(LogEntry o1, LogEntry o2) {
                if (o1.getTimestamp() > o2.getTimestamp()) {
                    return 1;
                }

                if (o1.getTimestamp() < o2.getTimestamp()) {
                    return -1;
                }
                return 0;
            }
        });

        return result;
    }

    @Override
    public void stopCollect(String item) {
        getCollector(item).clear();

        collectForList.expire(expiry, TimeUnit.SECONDS);
        collectForList.remove(item);

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
        Map<String, Object> map = Maps.newHashMap();
        map.put("timestamp", logEntry.getTimestamp());
        map.put("message", logEntry.getMessage());
        String json = JSONValue.toJSONString(map);

        for (String collector : collectForList) {
            RedisList list = getCollector(collector);

            list.add(json);
            list.expire(expiry, TimeUnit.SECONDS);
        }
    }
}
