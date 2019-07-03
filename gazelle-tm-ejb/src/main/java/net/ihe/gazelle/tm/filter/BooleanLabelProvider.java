package net.ihe.gazelle.tm.filter;

import net.ihe.gazelle.hql.LabelProvider;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BooleanLabelProvider implements LabelProvider<Boolean> {

    public static final BooleanLabelProvider INSTANCE = new BooleanLabelProvider();
    private static final Logger LOG = LoggerFactory.getLogger(BooleanLabelProvider.class);

    private BooleanLabelProvider() {
        super();
    }

    @Override
    public String getLabel(Boolean instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel");
        }
        if (instance == null) {
            return "?";
        } else {
            if (instance) {
                return StatusMessage.getBundleMessage("gazelle.common.Yes", "gazelle.common.Yes");
            } else {
                return StatusMessage.getBundleMessage("gazelle.common.No", "gazelle.common.No");
            }
        }
    }

}
