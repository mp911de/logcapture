package biz.paluch.logcapture.jbossas7x;

import biz.paluch.logcapture.ws.AbstractLogControlServicePort;

import javax.jws.WebService;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@WebService(portName = "LogControlServicePort", name = "LogControlService", serviceName = "LogControlService", endpointInterface = "biz.paluch.logcapture.ws.LogControlService")
public class LogControlServicePort extends AbstractLogControlServicePort {
}
