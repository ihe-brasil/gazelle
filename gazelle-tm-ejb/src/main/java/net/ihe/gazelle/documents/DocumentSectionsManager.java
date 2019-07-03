/*
 * Copyright 2009 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.documents;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.tf.model.Document;
import net.ihe.gazelle.tf.model.DocumentSection;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Name("documentSectionsManager")
@Scope(ScopeType.PAGE)
@Synchronized(timeout = 10000)
public class DocumentSectionsManager implements Serializable {
    /**
     * Field LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DocumentSectionsManager.class);

    private static final long serialVersionUID = -2550509330742486850L;

    private DocumentSection selectedSection;
    private Document documentContainingSection;

    public DocumentSection getSelectedSection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSection");
        }
        return selectedSection;
    }

    public void setSelectedSection(DocumentSection selectedSection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSection");
        }
        this.selectedSection = selectedSection;
    }

    public Document getDocumentContainingSection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentContainingSection");
        }
        return documentContainingSection;
    }

    public void setDocumentContainingSection(Document documentContainingSection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDocumentContainingSection");
        }
        this.documentContainingSection = documentContainingSection;
    }

    public String getPdfReference(DocumentSection section) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPdfReference");
        }
        return section.getPdfReference();
    }

    public String getSection(DocumentSection section) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSection");
        }
        return section.getSection();

    }

    /**
     * ShowPDF action : when a user clicks on ShowPDF button, it initializes variables to show a Document
     *
     * @param section Document
     * @return String
     */
    public void showPDFSection(DocumentSection section) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showPDFSection");
        }
        selectedSection = section;

        String encodedUrl;
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            encodedUrl = URLEncoder.encode(selectedSection.getDocument().getUrl(), StandardCharsets.UTF_8.name());

            String view = "/pdfjs/web/viewer.html?file=/"
                    + ApplicationPreferenceManager.instance().getApplicationUrlBaseName()
                    + "/document/getPDFdocument.seam%3Furl%3D" + URLEncoder.encode(encodedUrl, StandardCharsets.UTF_8.name()) + "#nameddest="
                    + section.getSection();
            ec.redirect(ec.getRequestContextPath() + view);
        } catch (UnsupportedEncodingException e) {
            LOG.warn("showPDF(): cannot encode url, " + selectedSection.getDocument().getUrl());
        } catch (IOException e) {
            LOG.error("" + e);
        }
    }
}
