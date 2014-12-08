package biz.paluch.logcapture.redis;

import com.lambdaworks.redis.RedisConnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 29.04.14 08:07
 */
public class RedisList implements List<String> {

    private int batchSize = 50;
    private RedisConnection<String, String> connection;
    private String name;

    public RedisList(RedisConnection<String, String> connection, String name) {
        this.connection = connection;
        this.name = name;
    }

    @Override
    public int size() {
        if (!connection.isOpen()) {
            return 0;
        }

        return connection.llen(name).intValue();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public Iterator<String> iterator() {
        return listIterator();
    }

    @Override
    public Object[] toArray() {
        List<String> list = subList(0, size());
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        List<String> list = subList(0, size());
        return list.toArray(a);
    }

    @Override
    public boolean add(String e) {
        return addAll(Collections.singleton(e));
    }

    @Override
    public boolean remove(Object o) {
        if (!connection.isOpen()) {
            return false;
        }
        return connection.lrem(name, 1, o.toString()) > 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (isEmpty()) {
            return false;
        }

        Collection<Object> copy = new ArrayList<Object>(c);
        int to = div(size(), batchSize);
        for (int i = 0; i < to; i++) {
            List<String> range = subList(i * batchSize, i * batchSize + batchSize - 1);
            for (Iterator<Object> iterator = copy.iterator(); iterator.hasNext();) {
                Object obj = iterator.next();
                int index = range.indexOf(obj);
                if (index != -1) {
                    iterator.remove();
                }
            }
        }

        return copy.isEmpty();

    }

    @Override
    public boolean addAll(Collection<? extends String> c) {

        String[] strings = c.toArray(new String[c.size()]);
        if (!connection.isOpen()) {
            return false;
        }
        return connection.lpush(name, strings) > 0;
    }

    @Override
    public boolean addAll(int index, Collection<? extends String> coll) {
        if (!connection.isOpen()) {
            return false;
        }

        checkPosition(index);
        if (index < size()) {
            while (true) {

                connection.watch(name);
                List<String> tail = connection.lrange(name, index, size());

                connection.multi();
                connection.ltrim(name, 0, index - 1);
                connection.rpush(name, (String[]) coll.toArray());
                connection.rpush(name, (String[]) tail.toArray());
                connection.exec();
            }
        } else {
            return addAll(coll);
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {

        if (!connection.isOpen()) {
            return false;
        }
        boolean result = false;
        for (Object object : c) {
            boolean res = connection.lrem(name, 0, object.toString()) > 0;
            if (!result) {
                result = res;
            }
        }
        return result;

    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (Iterator<String> iterator = iterator(); iterator.hasNext();) {
            String object = iterator.next();
            if (!c.contains(object)) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear() {
        if (!connection.isOpen()) {
            return;
        }
        connection.del(name);
    }

    @Override
    public String get(int index) {
        if (!connection.isOpen()) {
            return null;
        }
        checkIndex(index);
        return connection.lindex(name, index);
    }

    private void checkIndex(int index) {
        int size = size();
        if (!isInRange(index, size))
            throw new IndexOutOfBoundsException("index: " + index + " but current size: " + size);
    }

    private boolean isInRange(int index, int size) {
        return index >= 0 && index < size;
    }

    private void checkPosition(int index) {
        int size = size();
        if (!isPositionInRange(index, size))
            throw new IndexOutOfBoundsException("index: " + index + " but current size: " + size);
    }

    private boolean isPositionInRange(int index, int size) {
        return index >= 0 && index <= size;
    }

    @Override
    public String set(int index, String element) {
        if (!connection.isOpen()) {
            return null;
        }
        checkIndex(index);
        connection.watch(name);
        connection.lset(name, index, element);
        return null;
    }

    @Override
    public void add(int index, String element) {
        addAll(index, Collections.singleton(element));
    }

    private int div(int p, int q) {
        int div = p / q;
        int rem = p - q * div; // equal to p % q

        if (rem == 0) {
            return div;
        }

        return div + 1;
    }

    @Override
    public String remove(int index) {
        if (!connection.isOpen()) {
            return null;
        }
        checkIndex(index);

        if (index == 0) {
            return connection.lpop(name);
        }
        while (true) {
            connection.watch(name);
            String prev = (String) connection.lindex(name, index);
            List<String> tail = connection.lrange(name, index + 1, size());

            connection.multi();
            connection.ltrim(name, 0, index - 1);
            connection.rpush(name, (String[]) tail.toArray());
            if (connection.exec().size() == 2) {
                return prev;
            }
        }
    }

    @Override
    public int indexOf(Object o) {
        if (!connection.isOpen()) {
            return -1;
        }
        if (isEmpty()) {
            return -1;
        }

        int to = div(size(), batchSize);
        for (int i = 0; i < to; i++) {
            List<String> range = connection.lrange(name, i * batchSize, i * batchSize + batchSize - 1);
            int index = range.indexOf(o);
            if (index != -1) {
                return index + i * batchSize;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!connection.isOpen()) {
            return -1;
        }
        if (isEmpty()) {
            return -1;
        }

        int size = size();
        int to = div(size, batchSize);
        for (int i = 1; i <= to; i++) {
            int startIndex = -i * batchSize;
            List<String> range = connection.lrange(name, startIndex, size - (i - 1) * batchSize);
            int index = range.lastIndexOf(o);
            if (index != -1) {
                return Math.max(size + startIndex, 0) + index;
            }
        }

        return -1;
    }

    @Override
    public ListIterator<String> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<String> listIterator(final int ind) {
        return subList(0, ind).listIterator();
    }

    @Override
    public List<String> subList(int fromIndex, int toIndex) {
        if (!connection.isOpen()) {
            return null;
        }
        int size = size();
        if (fromIndex < 0 || toIndex > size) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + " size: " + size);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex: " + fromIndex + " toIndex: " + toIndex);
        }

        return connection.lrange(name, fromIndex, toIndex - 1);
    }

    public String toString() {
        Iterator<String> it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            String e = it.next();
            sb.append(e.equals(this) ? "(this Collection)" : e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

    public void expire(int expiry, TimeUnit timeUnit) {
        if (!connection.isOpen()) {
            return;
        }
        connection.expire(name, (int) TimeUnit.SECONDS.convert(expiry, timeUnit));
    }
}
