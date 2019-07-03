package net.ihe.gazelle.documents;

import net.ihe.gazelle.tf.model.Document;
import net.ihe.gazelle.tf.model.DocumentQuery;
import org.jboss.seam.annotations.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class BackgroundUrlChecker {

    private static final Logger LOG = LoggerFactory.getLogger(BackgroundUrlChecker.class);
    @In
    private EntityManager em;

    public void setEntityManager(EntityManager em) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEntityManager");
        }
        this.em = em;
    }

    public int checkDocumentsUrlPointsToPDF() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkDocumentsUrlPointsToPDF");
        }
        DocumentURLStatusType urlStatus = DocumentURLStatusType.CHECKING;

        DocumentQuery query = new DocumentQuery();
        List<Document> documentsToCheck = query.getList();
        int count = 0;
        for (Document document : documentsToCheck) {
            urlStatus = DocumentUrlManager.checkUrlIsPointingToPDF(document.getUrl());
            document.setLinkStatusDescription(urlStatus.getFriendlyName());
            if (urlStatus.equals(DocumentURLStatusType.THIS_URL_IS_POINTING_TO_A_PDF)) {
                document.setLinkStatus(0);
            } else {
                document.setLinkStatus(1);
            }
            writeObject(document);
            count += 1;
        }
        return count;
    }

    private Object writeObject(Object obj) {
        obj = em.merge(obj);
        em.flush();
        return obj;
    }
}
