package net.ihe.gazelle.documents;

import net.ihe.gazelle.common.filecache.FileCache;
import net.ihe.gazelle.common.filecache.FileCacheRenderer;
import net.ihe.gazelle.tf.model.Document;
import net.ihe.gazelle.tf.model.DocumentQuery;
import net.ihe.gazelle.util.Md5Encryption;
import org.apache.commons.io.IOUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

@Name("openPDFDocument")
@Scope(ScopeType.PAGE)
@Synchronized(timeout = 10000)
public class OpenPDFDocument implements Serializable {

    private static final long serialVersionUID = 3849914701380989881L;
    private static final Logger LOG = LoggerFactory.getLogger(OpenPDFDocument.class);

    public void getIsStream() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIsStream");
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, String> params = externalContext.getRequestParameterMap();
        String documentUrl = "";

        try {
            documentUrl = params.get("url");
            DocumentQuery query = new DocumentQuery();
            query.url().eq(documentUrl);
            Document document = query.getUniqueResult();
            if (document != null) {
                documentUrl = document.getUrl();
            }
        } catch (NumberFormatException e) {

        }

        try {
            int documentId = Integer.parseInt(params.get("doc"));
            DocumentQuery query = new DocumentQuery();
            query.id().eq(documentId);
            Document document = query.getUniqueResult();

            if (document != null) {
                documentUrl = document.getUrl();
            }
        } catch (NumberFormatException e) {
        }

        FileCache.getFile("document" + Md5Encryption.hashPassword(documentUrl), documentUrl, new FileCacheRenderer() {
            @Override
            public void render(OutputStream out, String value) throws Exception {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("render");
                }
                URL url = new URL(value);
                final InputStream is = url.openStream();
                try {
                    IOUtils.copyLarge(is, out);
                } finally {
                    is.close();
                }
            }

            @Override
            public String getContentType() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getContentType");
                }
                return "application/pdf";
            }
        });
    }
}
