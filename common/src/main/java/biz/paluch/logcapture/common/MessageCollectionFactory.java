package biz.paluch.logcapture.common;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface MessageCollectionFactory {

    /**
     * Create a {@link MessageCollectionStore} using {@literal configurationString}
     * 
     * @param target
     * @return an initialized {@link MessageCollectionStore} instance
     */
    MessageCollectionStore createStore(String target);

    /**
     *
     * @param target
     * @return true the factory accepts the config string in order to create a {@link MessageCollectionStore}
     */
    boolean accept(String target);

    /**
     *
     * @return relative priority. higher = first, lower = last.
     */
    double priority();

}
