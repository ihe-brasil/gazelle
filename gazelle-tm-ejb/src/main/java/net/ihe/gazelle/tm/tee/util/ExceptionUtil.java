package net.ihe.gazelle.tm.tee.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for handling exceptions
 *
 * @author tnabeel
 */
public class ExceptionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionUtil.class);

    /**
     * Appends messages of all wrapped exceptions.
     *
     * @param e
     * @param appendRootClassName     Whether or not to append the class name of main wrapper
     * @param appendWrappedClassNames Whether or not to append the wrapped exception class anem
     * @return
     */
    public static String getWrappedMessage(Throwable e, boolean appendWrapperClassName, boolean appendWrapperMsg, boolean appendWrappedClassNames) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getWrappedMessage");
        }
        StringBuilder msg = new StringBuilder();
        if (e != null) {
            if (appendWrapperMsg) {
                appendMessage(msg, e, appendWrapperClassName);
            }
            while (e.getCause() != null) {
                e = e.getCause();
                appendMessage(msg, e, appendWrappedClassNames);

            }
        }
        return msg.toString();
    }

    private static void appendMessage(StringBuilder msg, Throwable e, boolean appendClassName) {
        if (appendClassName) {
            msg.append(e.getClass().getSimpleName());
        }
        if (e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
            msg.append(':');
            msg.append(' ');
            msg.append(e.getMessage());
            if (msg.charAt(msg.length() - 1) != '.') {
                msg.append('.');
            }
        }
        msg.append(' ');
    }
}
