package net.ihe.gazelle.menu;

import net.ihe.gazelle.common.pages.Page;
import net.ihe.gazelle.common.pages.PageLister;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

@MetaInfServices(PageLister.class)
public class GazellePageLister implements PageLister {

    private static final Logger LOG = LoggerFactory.getLogger(GazellePageLister.class);

    @Override
    public Collection<Page> getPages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPages");
        }
        Page[] pages = Pages.values();
        return Arrays.asList(pages);
    }

}
