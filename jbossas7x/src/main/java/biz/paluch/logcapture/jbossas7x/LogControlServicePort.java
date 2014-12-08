package biz.paluch.logcapture.jbossas7x;

import javax.jws.WebService;

import biz.paluch.logcapture.ws.AbstractLogControlServicePort;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@WebService(portName = "LogControlServicePort", name = "LogControlService", serviceName = "LogControlService", endpointInterface = "biz.paluch.logcapture.ws.LogControlService", targetNamespace = "http://logging.paluch.biz/logcapture")
public class LogControlServicePort extends AbstractLogControlServicePort {
}
