package biz.paluch.logcapture.redis;

import biz.paluch.logcapture.common.MessageCollectionFactory;

import java.net.URI;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RedisMessageCollectionFactory implements MessageCollectionFactory {
    public final static String REDIS_PREFIX = "redis://";

    @Override
    public RedisMessageCollectionStore createStore(String target) {

        URI uri = URI.create(target);
        String listPrefix = "logcapture";

        return new RedisMessageCollectionStore(listPrefix, uri);
    }

    @Override
    public boolean accept(String target) {
        return target != null && target.startsWith(REDIS_PREFIX);
    }

    @Override
    public double priority() {
        return 500;
    }
}
