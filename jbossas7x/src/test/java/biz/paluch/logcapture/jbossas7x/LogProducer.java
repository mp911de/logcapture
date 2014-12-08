package biz.paluch.logcapture.jbossas7x;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@ApplicationScoped
public class LogProducer {
    private Logger log = Logger.getLogger(getClass().getName());

    public void log(String message) {
        log.info(message);
    }
}
