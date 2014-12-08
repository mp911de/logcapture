package biz.paluch.logcapture.jbossas7x;

import biz.paluch.logcapture.common.jul.SimpleFormatter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import biz.paluch.logcapture.common.LogEntry;
import biz.paluch.logcapture.common.MessageCollectionFactory;
import biz.paluch.logcapture.common.MessageCollectionStore;
import biz.paluch.logcapture.common.Service;
import biz.paluch.logcapture.common.StackTraceFilter;
import biz.paluch.logcapture.common.StaticCollectionStoreInstance;
import biz.paluch.logcapture.common.jul.InstallJulCollector;
import biz.paluch.logcapture.common.jul.JulCollectingHandler;
import biz.paluch.logcapture.common.jul.NullHandler;
import biz.paluch.logcapture.inmemory.MemoryMessageCollectionFactory;
import biz.paluch.logcapture.inmemory.MemoryMessageCollectionStore;
import biz.paluch.logcapture.redis.RedisMessageCollectionFactory;
import biz.paluch.logcapture.ws.AbstractLogControlServicePort;
import biz.paluch.logcapture.ws.LogControlService;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class AbstractContainerTest {

    public static WebArchive createDeploymentImpl() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addClasses(LogControlServicePort.class, LogControlService.class, AbstractLogControlServicePort.class,
                        InstallLogCollectorContextListener.class, LogEntry.class, biz.paluch.logcapture.ws.LogEntry.class,
                        Service.class, StackTraceFilter.class, StaticCollectionStoreInstance.class, InstallJulCollector.class,
                        MessageCollectionStore.class, JulCollectingHandler.class, MessageCollectionFactory.class,
                        MemoryMessageCollectionFactory.class, MemoryMessageCollectionStore.class,
                        RedisMessageCollectionFactory.class, NullHandler.class, LogProducer.class, SimpleFormatter.class)
                .addAsServiceProvider(MemoryMessageCollectionFactory.class).setWebXML("web.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

}
