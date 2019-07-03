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
import net.ihe.gazelle.common.filecache.FileCache;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.util.Md5Encryption;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author jean-francois
 * @version $Revision: 1.0 $
 */
@Name("documentManager")
@Scope(ScopeType.PAGE)
public class DocumentManager implements Serializable {

    /**
     * Field serialVersionUID. (value is -6206009935429463603)
     */
    private static final long serialVersionUID = -6206009935429463603L;
    private static final Logger LOG = LoggerFactory.getLogger(DocumentManager.class);
    /**
     * Field entityManager.
     */
    @In
    private EntityManager entityManager;
    /**
     * Field renderAddPanel.
     */
    private Boolean renderAddPanel;
    /**
     * Field renderFilterPanel.
     */
    private Boolean renderFilterPanel;
    /**
     * Field selectedDocument.
     */
    private Document selectedDocument;
    /**
     * Field filter.
     */
    private DocumentFilter filter;

    /**
     * Field documents.
     */
    private FilterDataModel<Document> documents;

    /**
     * Field sectionFilter.
     */
    private DocumentSectionsFilter sectionFilter;

    /**
     * Field documentSections.
     */
    private FilterDataModel<DocumentSection> documentSections;

    private String analyseUrlOk;

    private String selectedDocumentUrl;

    /**
     * Constructor for DocumentManager.
     */
    public DocumentManager() {
        setRenderAddPanel(false);
        setRenderFilterPanel(true);
    }

    public static String showPDFWebservice(String encodedUrl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String showPDFWebservice");
        }
        HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        String url = null;
        try {
            url = request.getContextPath() + "/pdfjs/web/viewer.html?file=/" + ApplicationPreferenceManager.instance().getApplicationUrlBaseName()
                    + "/document/getPDFdocument.seam%3Furl%3D"
                    + URLEncoder.encode(encodedUrl, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            LOG.warn("showPDFWebservice(): cannot encode url, " + encodedUrl);
        }
        return url;
    }

    public String getSelectedDocumentUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDocumentUrl");
        }
        return selectedDocumentUrl;
    }

    public void setSelectedDocumentUrl(String selectedDocumentUrl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDocumentUrl");
        }
        this.selectedDocumentUrl = selectedDocumentUrl;
    }

    /**
     * Method getFilter.
     *
     * @return DocumentFilter
     */
    @SuppressWarnings("unchecked")
    public DocumentFilter getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            final Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            filter = new DocumentFilter(requestParameterMap);
        }
        return filter;
    }

    /**
     * Method getDocuments.
     *
     * @return FilterDataModel<Document>
     */
    public FilterDataModel<Document> getDocuments() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocuments");
        }
        if (documents == null) {
            documents = new FilterDataModel<Document>(getFilter()) {
                @Override
                protected Object getId(Document t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return documents;
    }

    /**
     * Method getSectionFilter.
     *
     * @return DocumentSectionsFilter
     */
    public DocumentSectionsFilter getSectionFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSectionFilter");
        }
        if (sectionFilter == null) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            final Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            sectionFilter = new DocumentSectionsFilter(requestParameterMap);
        }
        return sectionFilter;
    }

    /**
     * Method getDocuments.
     *
     * @return FilterDataModel<Document>
     */
    public FilterDataModel<DocumentSection> getDocumentSections() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentSections");
        }
        if (documentSections == null) {
            documentSections = new FilterDataModel<DocumentSection>(getSectionFilter()) {
                @Override
                protected Object getId(DocumentSection t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return documentSections;
    }

    /**
     * Method newDocument.
     */
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void newDocument() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("newDocument");
        }

        setRenderAddPanel(true);
        setRenderFilterPanel(false);
        selectedDocument = new Document();
    }

    /**
     * Method cancelDocument.
     *
     * @return String
     */
    public String cancelDocument() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelDocument");
        }

        setRenderAddPanel(false);
        setRenderFilterPanel(true);
        selectedDocument = null;
        return "/tf/documents/document.xhtml";
    }

    /**
     * Method saveDocument.
     */
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String saveDocument() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveDocument");
        }

        try {
            entityManager.merge(selectedDocument);
            entityManager.flush();
            resetCache(selectedDocument.getUrl());
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Document created");
        } catch (Throwable e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This document already exists");
            LOG.error("" + e);
        }
        if (filter != null) {
            filter.modified();
        }
        setRenderAddPanel(false);
        setRenderFilterPanel(true);
        return "/tf/documents/document.xhtml";
    }

    /**
     * Edit action : when a user clicks on Edit button, it initializes variables to edit a Document
     *
     * @param doc Document
     * @return String
     */
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String edit(Document doc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("edit");
        }
        selectedDocument = doc;
        return "/tf/documents/editDocument.xhtml?doc=" + selectedDocument.getId();
    }

    public void clone(Document doc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clone");
        }

        setRenderAddPanel(true);
        setRenderFilterPanel(false);
        selectedDocument = new Document(doc);

    }

    /**
     * Show action : when a user clicks on Show button, it initializes variables to show a Document
     *
     * @param doc Document
     * @return String
     */
    public String show(Document doc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("show");
        }
        selectedDocument = doc;
        return "/tf/documents/showDocument.xhtml?doc=" + doc.getId();
    }

    /**
     * ShowPDF action : when a user clicks on ShowPDF button, it initializes variables to show a Document
     *
     * @param doc Document
     * @return String
     * @throws UnsupportedEncodingException
     */
    public void showPDF(Document doc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showPDF");
        }

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

        selectedDocument = doc;

        String encodedUrl;
        try {
            encodedUrl = URLEncoder.encode(selectedDocument.getUrl(), StandardCharsets.UTF_8.name());

            Contexts.getSessionContext().set("backPage", null);
            Contexts.getSessionContext().set("backMessage", null);
            ec.redirect(ec.getRequestContextPath() + "/pdfjs/web/viewer.html?file=/" + ApplicationPreferenceManager.instance()
                    .getApplicationUrlBaseName() + "/document/getPDFdocument.seam%3Furl%3D"
                    + URLEncoder.encode(encodedUrl, StandardCharsets.UTF_8.name()) + "#page=0");
        } catch (UnsupportedEncodingException e) {
            LOG.warn("showPDF(): cannot encode url, " + selectedDocument.getUrl());
        } catch (IOException e) {
            LOG.error("" + e);
        }

    }

    /**
     * Delete a selected Document
     */
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void delete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("delete");
        }

        selectedDocument = entityManager.find(Document.class, selectedDocument.getId());
        try {
            if (selectedDocument.isNotLinkedToOtherEntities()) {
                List<DocumentSection> sections = selectedDocument.getDocumentSection();
                if (sections != null) {
                    if (sections.size() != 0) {
                        for (DocumentSection section : sections) {
                            String sectionName = section.getSection();
                            entityManager.remove(section);
                            entityManager.flush();

                        }
                    }
                }
                entityManager.remove(selectedDocument);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Document removed");
            }

        } catch (Exception e) {
            List<DocumentSection> docSections = selectedDocument.getDocumentSection();

            if (docSections != null) {
                LOG.warn("Document" + selectedDocument.getName() + " cannot be deleted, because document sections are linked to this document");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "net.ihe.gazelle.tm.CannotDeleteDocument" + selectedDocument.getName
                        () + "net.ihe.gazelle.tm.BecauseDocumentSectionsAreLinkedToThisDocument");
            } else {
                LOG.warn("Document" + selectedDocument.getName() + " cannot be deleted");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "net.ihe.gazelle.tm.CannotDeleteDocument" + selectedDocument
                        .getName());
            }
        }
        filter.modified();
    }

    /**
     * Method getSelectedDocument.
     *
     * @return Document
     */
    public Document getSelectedDocument() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDocument");
        }
        return selectedDocument;
    }

    /**
     * Method setSelectedDocument.
     *
     * @param selectedDocument Document
     */
    public void setSelectedDocument(Document selectedDocument) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDocument");
        }
        this.selectedDocument = selectedDocument;
    }

    public String getAnalyseUrlOk() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAnalyseUrlOk");
        }
        return analyseUrlOk;
    }

    public void setAnalyseUrlOk(String analyseUrlOk) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAnalyseUrlOk");
        }
        this.analyseUrlOk = analyseUrlOk;
    }

    /**
     * Method getRenderAddPanel.
     *
     * @return Boolean
     */
    public Boolean getRenderAddPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRenderAddPanel");
        }
        return renderAddPanel;
    }

    /**
     * Method setRenderAddPanel.
     *
     * @param render Boolean
     */
    public void setRenderAddPanel(Boolean render) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRenderAddPanel");
        }
        this.renderAddPanel = render;
    }

    /**
     * Method getRenderFilterPanel.
     *
     * @return Boolean
     */
    public Boolean getRenderFilterPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRenderFilterPanel");
        }
        return renderFilterPanel;
    }

    /**
     * Method setRenderFilterPanel.
     *
     * @param renderFilterPanel Boolean
     */
    public void setRenderFilterPanel(Boolean renderFilterPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRenderFilterPanel");
        }
        this.renderFilterPanel = renderFilterPanel;
    }

    /**
     * Method analyzeUrl. Called from _Edit_document.xhtml
     */
    public void analyzeUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("analyzeUrl");
        }

        final DocumentUrlManager urlManager = new DocumentUrlManager(selectedDocument);
        if (urlManager.analyseUrl(selectedDocument.getUrl())) {
            analyseUrlOk = "Url Ok";
        }

    }

    /**
     * Method documentLifeCycleStatusList.
     *
     * @return SelectItem[]
     */
    public SelectItem[] documentLifeCycleStatusList() {
        final List<DocumentLifeCycleStatus> list = Arrays.asList(DocumentLifeCycleStatus.values());
        final List<SelectItem> result = new ArrayList<SelectItem>();
        for (DocumentLifeCycleStatus lifecyclestatus : list) {
            result.add(new SelectItem(lifecyclestatus, lifecyclestatus.getFriendlyName()));
        }
        return result.toArray(new SelectItem[result.size()]);
    }

    /**
     * Method documentTypeList.
     *
     * @return SelectItem[]
     */
    public SelectItem[] documentTypeList() {
        final List<DocumentType> list = Arrays.asList(DocumentType.values());
        final List<SelectItem> result = new ArrayList<SelectItem>();
        for (DocumentType type : list) {
            result.add(new SelectItem(type, type.getFriendlyName()));
        }
        return result.toArray(new SelectItem[result.size()]);
    }

    /**
     * Method redirectToShowDocumentPage.
     */
    public void redirectToShowDocumentPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("redirectToShowDocumentPage");
        }

        final FacesContext fc = FacesContext.getCurrentInstance();
        final String doc = fc.getExternalContext().getRequestParameterMap().get("doc");
        final Redirect redirect = Redirect.instance();
        Contexts.getSessionContext().set("backPage", null);
        Contexts.getSessionContext().set("backMessage", null);

        if (doc != null) {
            redirect.setParameter("doc", doc);
            redirect.setViewId("/tf/documents/showDocument.xhtml");
        } else {
            redirect.setViewId("/tf/documents/document.xhtml");
        }
        redirect.execute();
    }

    /**
     * Method getDocumentPermalink.
     *
     * @param doc Document
     * @return String
     */
    public String getDocumentPermalink(Document doc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentPermalink");
        }
        if (doc != null) {
            return ApplicationPreferenceManager.instance().getApplicationUrl() + "document/documentInstance.seam?doc=" + doc.getId();
        }
        return null;
    }

    /**
     * Method parseURLParameters.
     *
     * @param action String
     */
    public void parseURLParameters(String action) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("parseURLParameters");
        }
        // refresh list of status
        final FacesContext fc = FacesContext.getCurrentInstance();
        final String docId = fc.getExternalContext().getRequestParameterMap().get("doc");
        if (docId != null) {
            final EntityManager em = EntityManagerService.provideEntityManager();
            final Document document = em.find(Document.class, Integer.parseInt(docId));
            if (document != null) {
                if (selectedDocument == null) {
                    selectedDocument = document;
                }
                if (action.equalsIgnoreCase("view")) {
                    viewSelectedDocument();
                } else if (action.equalsIgnoreCase("edit")) {
                    editSelectedDocument();
                } else {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No Document found with the given id");
                    selectedDocument = null;
                }
            } else {
            }
        }
    }

    /**
     * Method viewSelectedDocument.
     *
     * @return String
     */
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String editSelectedDocument() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editSelectedDocument");
        }
        return "/tf/documents/editDocument.xhtml";
    }

    /**
     * Method viewSelectedDocument.
     *
     * @return String
     */
    public String viewSelectedDocument() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewSelectedDocument");
        }
        getSectionFilter().setDocumentRestriction(selectedDocument);
        return show(selectedDocument);
    }

    /**
     * Method getPdfReference.
     *
     * @param section DocumentSection
     * @return String
     */
    public String getPdfReference(DocumentSection section) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPdfReference");
        }
        return section.getPdfReference();
    }

    /**
     * Method getSection.
     *
     * @param section DocumentSection
     * @return String
     */
    public String getSection(DocumentSection section) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSection");
        }
        return section.getSection();
    }

    public void resetCache() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetCache");
        }
        DocumentQuery query = new DocumentQuery();
        List<String> urls = query.url().getListDistinct();

        for (String url : urls) {
            resetCache(url);
        }
    }

    private void resetCache(String url) {
        FileCache.resetCache("document" + Md5Encryption.hashPassword(url));
    }
}