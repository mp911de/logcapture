package biz.paluch.logcapture.common;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.04.14 13:30
 */
public class StaticCollectionStoreInstance {

    private final static StaticCollectionStoreInstance INSTANCE = new StaticCollectionStoreInstance();

    private MessageCollectionStore messageCollectionStore;

    private StaticCollectionStoreInstance() {

    }

    public static MessageCollectionStore getMessageCollectionStore() {
        return INSTANCE.messageCollectionStore;
    }

    public static void setMessageCollectionStore(MessageCollectionStore messageCollectionStore) {
        INSTANCE.messageCollectionStore = messageCollectionStore;
    }
}
