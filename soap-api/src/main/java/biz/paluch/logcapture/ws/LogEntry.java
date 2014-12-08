package biz.paluch.logcapture.ws;

import javax.xml.bind.annotation.XmlType;
import java.util.Date;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@XmlType(namespace = "http://logging.paluch.biz/logcapture/model")
public class LogEntry {
    private Date timestamp;
    private String message;

    public LogEntry() {
    }

    public LogEntry(Date timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
