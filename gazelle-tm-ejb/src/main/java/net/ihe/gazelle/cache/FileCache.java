package net.ihe.gazelle.cache;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

/**
 * Created by gthomazon on 09/09/16.
 */
@Name("fileCache")
@Scope(ScopeType.EVENT)
public class FileCache {

    public void clearCache() {
        net.ihe.gazelle.common.filecache.FileCache.clearCache();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "File cache cleared !");
    }
}

