package net.ihe.gazelle.tm.tee.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expected runtime exceptions during test execution.
 *
 * @author tnabeel
 */
public class TestExecutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(TestExecutionException.class);
    private String messageDetail;

    public TestExecutionException() {
        super();
    }

    public TestExecutionException(String message, String messageDetail, Throwable cause) {
        super(message, cause);
        this.messageDetail = messageDetail;
    }

    public TestExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestExecutionException(String message, String messageDetail) {
        super(message);
        this.messageDetail = messageDetail;
    }

    public TestExecutionException(String message) {
        super(message);
    }

    public TestExecutionException(Throwable cause) {
        super(cause);
    }

    public String getMessageDetail() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageDetail");
        }
        return messageDetail;
    }

    @Override
    public String toString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("toString");
        }
        String localizedMessage = getLocalizedMessage();
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        if (localizedMessage != null) {
            builder.append(": ");
            builder.append(localizedMessage);
        }
        if (getMessageDetail() != null) {
            builder.append(". ");
            builder.append(getMessageDetail());
        }
        return builder.toString();
    }

}
