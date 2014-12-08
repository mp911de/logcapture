package biz.paluch.logcapture.ws;

import biz.paluch.logcapture.common.MessageCollectionStore;
import biz.paluch.logcapture.common.StaticCollectionStoreInstance;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@WebService(portName = "LogControlServicePort", name = "LogControlService", serviceName = "LogControlService", endpointInterface = "biz.paluch.logcapture.ws.LogControlService")
public abstract class AbstractLogControlServicePort implements LogControlService {

    /**
     * @param id
     */
    @WebMethod
    public void startCollect(@WebParam(name = "id") String id) {
        getMessageCollectionStore().startCollect(id);
    }

    /**
     * @param id
     * @return String or null
     */
    @WebMethod
    public List<LogEntry> stopCollect(@WebParam(name = "id") String id) {

        List<LogEntry> entries = getLogsFor(id);

        getMessageCollectionStore().stopCollect(id);

        return entries;
    }

    /**
     * @param id
     * @return String or null
     */
    @WebMethod
    public List<LogEntry> getLogsFor(@WebParam(name = "id") String id) {

        List<biz.paluch.logcapture.common.LogEntry> entries = getMessageCollectionStore().getLogsFor(id);

        if (entries != null) {

            List<LogEntry> result = new ArrayList<LogEntry>();
            for (biz.paluch.logcapture.common.LogEntry entry : entries) {
                LogEntry logEntry = new LogEntry(new Date(entry.getTimestamp()), entry.getMessage());
                result.add(logEntry);
            }

            return result;
        }

        return new ArrayList<LogEntry>();

    }

    /**
     * /**
     * 
     * @return true/false
     */
    @WebMethod
    public boolean isLogCollectorOn() {
        return getMessageCollectionStore().isLogCollectorOn();

    }

    /**
     * @return true/false
     */
    @WebMethod
    public List<String> getActiveCollectors() {
        return new ArrayList<String>(getMessageCollectionStore().getActiveCollectors());
    }

    protected MessageCollectionStore getMessageCollectionStore() {

        MessageCollectionStore store = StaticCollectionStoreInstance.getMessageCollectionStore();
        if (store == null) {
            throw new IllegalStateException(StaticCollectionStoreInstance.class.getSimpleName() + " not initialized");
        }

        return store;
    }

}
