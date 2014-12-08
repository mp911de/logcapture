package biz.paluch.logcapture.inmemory;

import biz.paluch.logcapture.common.MessageCollectionFactory;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class MemoryMessageCollectionFactory implements MessageCollectionFactory {
    @Override
    public MemoryMessageCollectionStore createStore(String configurationString) {
        return new MemoryMessageCollectionStore();
    }

    @Override
    public boolean accept(String target) {
        return true;
    }

    /**
     *
     * @return {@literal Double.MIN_VALUE} always the last choice.
     */
    @Override
    public double priority() {
        return Double.MIN_VALUE;
    }
}
