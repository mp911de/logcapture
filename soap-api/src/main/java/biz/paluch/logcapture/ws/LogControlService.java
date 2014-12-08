package biz.paluch.logcapture.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@WebService(name = "LogControlService",targetNamespace = "http://logging.paluch.biz/logcapture")
public interface LogControlService {

    /**
     * @param id
     */
    @WebMethod
    public void startCollect(@WebParam(name = "id") String id);

    /**
     * @param id
     * @return String or null
     */
    @WebMethod
    public List<LogEntry> stopCollect(@WebParam(name = "id") String id);

    /**
     * @param id
     * @return String or null
     */
    @WebMethod
    public List<LogEntry> getLogsFor(@WebParam(name = "id") String id);

    /**
     * /**
     * 
     * @return true/false
     */
    @WebMethod
    public boolean isLogCollectorOn();

    /**
     * @return true/false
     */
    @WebMethod
    public List<String> getActiveCollectors();

}
