package biz.paluch.logcapture.jbossas7x;

import static org.assertj.core.api.Assertions.assertThat;
import biz.paluch.logcapture.ws.LogControlService;
import biz.paluch.logcapture.ws.LogEntry;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ErrorMessageFactory;
import org.assertj.core.groups.Tuple;
import org.assertj.core.internal.ComparisonStrategy;
import org.assertj.core.presentation.Representation;
import org.assertj.core.util.Iterables;
import org.assertj.core.util.introspection.IntrospectionError;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@RunWith(Arquillian.class)
public class InContainerTest extends AbstractContainerTest {

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        return createDeploymentImpl().addClass(AbstractContainerTest.class).addPackage(Assertions.class.getPackage())
                .addPackage(ErrorMessageFactory.class.getPackage()).addPackage(ComparisonStrategy.class.getPackage())
                .addPackage(IntrospectionError.class.getPackage()).addPackage(Representation.class.getPackage())
                .addPackage(Iterables.class.getPackage()).addPackage(Tuple.class.getPackage());
    }

    @Inject
    private LogProducer logProducer;

    @Test
    public void testSomeLogging() throws Exception {

        logProducer.log("before collect");

        LogControlService port = new LogControlServicePort();
        assertThat(port.getActiveCollectors()).isEmpty();
        port.startCollect("id");

        logProducer.log("collecting");

        assertThat(port.getActiveCollectors()).contains("id");
        List<LogEntry> entries = port.stopCollect("id");
        assertThat(port.getActiveCollectors()).isEmpty();

        assertThat(entries).hasSize(1);
        LogEntry entry = entries.get(0);

        assertThat(entry.getMessage()).contains("collecting");
        assertThat(entry.getTimestamp()).isAfterYear(1980);

        System.out.println(entry.getMessage());

    }
}
