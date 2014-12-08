package biz.paluch.logcapture.redis;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.RedisConnection;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.04.14 08:01
 */
public class RedisSet implements Set<String> {

    private RedisConnection<String, String> connection;
    private String name;

    public RedisSet(RedisConnection<String, String> connection, String name) {
        this.connection = connection;
        this.name = name;
    }

    @Override
    public int size() {
        if (!connection.isOpen()) {
            return 0;
        }

        return connection.scard(name).intValue();

    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (!connection.isOpen()) {
            return false;
        }
        return connection.sismember(name, o.toString());

    }

    @Override
    public Iterator<String> iterator() {

        if (!connection.isOpen()) {
            return Collections.EMPTY_LIST.iterator();
        }

        return connection.smembers(name).iterator();
    }

    @Override
    public Object[] toArray() {

        if (!connection.isOpen()) {
            return new Object[0];
        }

        return connection.smembers(name).toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {

        if (!connection.isOpen()) {
            return a;
        }
        return connection.smembers(name).toArray(a);
    }

    @Override
    public boolean add(String e) {
        if (!connection.isOpen()) {
            return false;
        }

        return connection.sadd(name, e) > 0;
    }

    @Override
    public boolean remove(Object o) {
        if (!connection.isOpen()) {
            return false;
        }
        return connection.srem(name, o.toString()) > 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object object : c) {
            if (!contains(object)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        if (!connection.isOpen()) {
            return false;
        }
        return connection.sadd(name, (String[]) c.toArray()) > 0;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (Object object : this) {
            if (!c.contains(object)) {
                remove(object);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (!connection.isOpen()) {
            return false;
        }
        return connection.srem(name, (String[]) c.toArray()) > 0;
    }

    @Override
    public void clear() {
        if (!connection.isOpen()) {
            return;
        }
        connection.del(name);
    }

    public void expire(int expiry, TimeUnit timeUnit) {
        if (!connection.isOpen()) {
            return;
        }
        connection.expire(name, (int) TimeUnit.SECONDS.convert(expiry, timeUnit));
    }
}
