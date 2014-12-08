package biz.paluch.logcapture.jbossas7x;

import static org.assertj.core.api.Assertions.assertThat;
import biz.paluch.logcapture.ws.LogControlService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.namespace.QName;
import java.net.URL;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@RunWith(Arquillian.class)
public class ContainerClientTest extends AbstractContainerTest {

    @ArquillianResource
    private URL deploymentURL;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return createDeploymentImpl();
    }

    @Test
    public void testSomeLogging() throws Exception {
        javax.xml.ws.Service ws = javax.xml.ws.Service.create(new URL(deploymentURL, "ws/LogControlService?wsdl"), new QName(
                "http://jbossas7x.logcapture.paluch.biz/", "LogControlService"));

        LogControlService port = ws.getPort(LogControlService.class);
        assertThat(port.getActiveCollectors()).isEmpty();

        port.startCollect("id");

        assertThat(port.getActiveCollectors()).contains("id");
        port.stopCollect("id");
        assertThat(port.getActiveCollectors()).isEmpty();

    }
}
