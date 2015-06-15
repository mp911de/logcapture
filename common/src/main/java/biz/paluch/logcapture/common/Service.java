package biz.paluch.logcapture.common;

import java.util.Collection;
import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.TreeSet;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class Service {
    private static Collection<MessageCollectionFactory> getFactories() {
        ServiceLoader<MessageCollectionFactory> loader = ServiceLoader.load(MessageCollectionFactory.class);

        TreeSet<MessageCollectionFactory> factories = new TreeSet<MessageCollectionFactory>(
                new Comparator<MessageCollectionFactory>() {
                    @Override
                    public int compare(MessageCollectionFactory o1, MessageCollectionFactory o2) {
                        if (o1.priority() > o2.priority()) {
                            return -1;
                        }
                        if (o1.priority() < o2.priority()) {
                            return 1;
                        }
                        return 0;
                    }
                });

        for (MessageCollectionFactory messageCollectionFactory : loader) {
            factories.add(messageCollectionFactory);
        }

        if (factories.isEmpty()) {
            throw new IllegalStateException("Cannot retrieve a " + MessageCollectionFactory.class.getName()
                    + ". Please add logcapture modules or create a service extension.");
        }
        return factories;
    }

    public static MessageCollectionStore createStore(String target) {
        for (MessageCollectionFactory messageCollectionFactory : getFactories()) {
            if (messageCollectionFactory.accept(target)) {
                return messageCollectionFactory.createStore(target);
            }
        }

        throw new IllegalStateException("Cannot create store instance for target " + target);
    }
}
