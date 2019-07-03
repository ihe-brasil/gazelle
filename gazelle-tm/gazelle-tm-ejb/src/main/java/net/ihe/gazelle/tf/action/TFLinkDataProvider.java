package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.common.LinkDataProvider;
import net.ihe.gazelle.tf.model.*;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@MetaInfServices(LinkDataProvider.class)
public class TFLinkDataProvider implements LinkDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TFLinkDataProvider.class);
    private static List<Class<?>> supportedClasses;

    static {
        supportedClasses = new ArrayList<Class<?>>();
        supportedClasses.add(Domain.class);
        supportedClasses.add(IntegrationProfileOption.class);
        supportedClasses.add(IntegrationProfile.class);
        supportedClasses.add(Transaction.class);
        supportedClasses.add(Actor.class);
    }

    @Override
    public List<Class<?>> getSupportedClasses() {
        return supportedClasses;
    }

    @Override
    public String getLabel(Object o, boolean detailed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel");
        }
        if (o instanceof Domain) {
            Domain c = (Domain) o;
            if (!detailed) {
                return c.getKeyword();
            } else {
                return c.getKeyword() + " - " + c.getName();
            }
        }
        if (o instanceof IntegrationProfileOption) {
            IntegrationProfileOption c = (IntegrationProfileOption) o;
            if (!detailed) {
                return c.getKeyword();
            } else {
                return c.getKeyword() + " - " + c.getName();
            }
        }
        if (o instanceof IntegrationProfile) {
            IntegrationProfile c = (IntegrationProfile) o;
            if (!detailed) {
                return c.getKeyword();
            } else {
                return c.getKeyword() + " - " + c.getName();
            }
        }
        if (o instanceof Transaction) {
            Transaction c = (Transaction) o;
            if (!detailed) {
                return c.getKeyword();
            } else {
                return c.getKeyword() + " - " + c.getName();
            }
        }
        if (o instanceof Actor) {
            Actor c = (Actor) o;
            if (!detailed) {
                return c.getKeyword();
            } else {
                return c.getKeyword() + " - " + c.getName();
            }
        }
        return "";
    }

    @Override
    public String getLink(Object o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLink");
        }
        if (o instanceof Domain) {
            Domain c = (Domain) o;
            return "domain.seam?keyword=" + c.getKeyword();
        }
        if (o instanceof IntegrationProfileOption) {
            IntegrationProfileOption c = (IntegrationProfileOption) o;
            return "profileOption.seam?keyword=" + c.getKeyword();
        }
        if (o instanceof IntegrationProfile) {
            IntegrationProfile c = (IntegrationProfile) o;
            return "profile.seam?keyword=" + c.getKeyword();
        }
        if (o instanceof Transaction) {
            Transaction c = (Transaction) o;
            return "transaction.seam?keyword=" + c.getKeyword();
        }
        if (o instanceof Actor) {
            Actor c = (Actor) o;
            return "actor.seam?keyword=" + c.getKeyword();
        }
        return "";
    }

    @Override
    public String getTooltip(Object o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTooltip");
        }
        return "";
    }

}
