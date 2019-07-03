/*
 * Copyright 2008 IHE International (http://www.ihe.net)
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
package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.documents.DocumentSectionsFilter;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Name("integrationProfileOptionManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("IntegrationProfileOptionManagerLocal")
public class IntegrationProfileOptionManager implements Serializable, IntegrationProfileOptionManagerLocal {

    private static final long serialVersionUID = -1357012043456235656L;
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationProfileOptionManager.class);
    @In
    private EntityManager entityManager;
    private IntegrationProfileOption selectedIntegrationProfileOption;
    private List<IntegrationProfileOption> integrationProfileOptions;
    private DocumentSectionsFilter filter;
    private FilterDataModel<DocumentSection> documentSections;
    private ActorIntegrationProfileOption selectedAipo;
    private Filter<ActorIntegrationProfileOption> actorsAndIntegrationProfileForIntegrationProfileOptionFilter;

    // ************************************* Variable screen messages and labels
    @In
    private Map<String, String> messages;
    private String lstTitle;

    private String editTitle;
    private String editSubTitle;
    private String editDescription;
    private Boolean edit = false;
    private Integer aipoCount = 0;

    private String callerPage;

    private String selectedTab = "integrationProfiles";

    private Filter<IntegrationProfileOption> f;
    private int selectedAipoForDetail;

    @Override
    public Boolean getEdit() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEdit");
        }
        return edit;
    }

    @Override
    public Boolean getView() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getView");
        }
        return !edit;
    }

    @Override
    public Integer getAipoCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoCount");
        }
        return aipoCount;
    }

    @Override
    public Filter<IntegrationProfileOption> getF() {
        if (f == null) {
            IntegrationProfileOptionQuery q = new IntegrationProfileOptionQuery();
            f = new Filter<IntegrationProfileOption>(q.getHQLCriterionsForFilter());
        }
        return f;
    }

    @Override
    public FilterDataModel<IntegrationProfileOption> integrationProfileOptionsDataModel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("integrationProfileOptionsDataModel");
        }
        return new FilterDataModel<IntegrationProfileOption>(getF()) {
            @Override
            protected Object getId(IntegrationProfileOption integrationProfileOption) {
                return integrationProfileOption.getId();
            }
        };
    }

    @Override
    public List<IntegrationProfileOption> getIntegrationProfileOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfileOptions");
        }
        return integrationProfileOptions;
    }

    @Override
    public void setIntegrationProfileOptions(List<IntegrationProfileOption> integrationProfileOptions) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIntegrationProfileOptions");
        }
        this.integrationProfileOptions = integrationProfileOptions;
    }

    @Override
    public IntegrationProfileOption getSelectedIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfileOption");
        }
        return selectedIntegrationProfileOption;
    }

    @Override
    public FilterDataModel<ActorIntegrationProfileOption> getActorsAndIntegrationProfileForIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorsAndIntegrationProfileForIntegrationProfileOption");
        }
        return new FilterDataModel<ActorIntegrationProfileOption>(getActorsAndIntegrationProfileForIntegrationProfileOptionFilter()) {
            @Override
            protected Object getId(ActorIntegrationProfileOption actorIntegrationProfileOption) {
                return actorIntegrationProfileOption.getId();
            }
        };
    }

    private Filter<ActorIntegrationProfileOption> getActorsAndIntegrationProfileForIntegrationProfileOptionFilter() {
        if (actorsAndIntegrationProfileForIntegrationProfileOptionFilter == null) {
            ActorIntegrationProfileOptionQuery q = new ActorIntegrationProfileOptionQuery();
            HQLCriterionsForFilter<ActorIntegrationProfileOption> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<ActorIntegrationProfileOption>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<ActorIntegrationProfileOption> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedIntegrationProfileOption != null) {
                        ActorIntegrationProfileOptionQuery q = new ActorIntegrationProfileOptionQuery();
                        hqlQueryBuilder.addRestriction(q.integrationProfileOption().id().eqRestriction(selectedIntegrationProfileOption.getId()));
                    }
                }
            });
            this.actorsAndIntegrationProfileForIntegrationProfileOptionFilter = new Filter<ActorIntegrationProfileOption>(result);
        }
        return this.actorsAndIntegrationProfileForIntegrationProfileOptionFilter;
    }

    @Override
    public String getLstTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLstTitle");
        }
        if (Identity.instance().hasPermission("MasterModel", "edit", null)) {
            lstTitle = "gazelle.tf.menu.IntegrationProfileOptionManagement";
        } else {
            lstTitle = "gazelle.tf.menu.IntegrationProfileOptionBrowsing";
        }
        return messages.get(lstTitle);
    }

    @Override
    public String getEditTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditTitle");
        }
        if (selectedIntegrationProfileOption != null) {
            if (selectedIntegrationProfileOption.getId() == null) {
                return (messages.get(editTitle) + " : " + messages.get("gazelle.common.New"));
            } else {
                return (messages.get(editTitle) + " : " + selectedIntegrationProfileOption.getKeyword());
            }
        } else {
            return (messages.get(editTitle) + " : " + messages.get("gazelle.common.PleaseReselect"));
        }

    }

    @Override
    public String getEditSubTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditSubTitle");
        }
        return messages.get(editSubTitle);
    }

    @Override
    public String getEditDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditDescription");
        }
        return messages.get(editDescription);
    }

    @Override
    public void findIntegrationProfileOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findIntegrationProfileOptions");
        }

        this.integrationProfileOptions = IntegrationProfileOption.GetAllIntegrationProfileOptions();

    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void updateIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateIntegrationProfileOption");
        }
        try {
            selectedIntegrationProfileOption = entityManager.merge(selectedIntegrationProfileOption);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Selected integration profile option updated");
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem updated selected integration profile option : " + e.getMessage());
        }
    }

    @Override
    public String showIntegrationProfileOptionDeleteSideEffects(IntegrationProfileOption selectedIPO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showIntegrationProfileOptionDeleteSideEffects");
        }
        try {
            selectedIntegrationProfileOption = entityManager.find(IntegrationProfileOption.class, selectedIPO.getId());

            aipoCount = getActorsAndIntegrationProfileForIntegrationProfileOption().getAllItems(FacesContext.getCurrentInstance()).size();
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error : " + e.getMessage());
        }
        return "/tf/integrationProfileOption/integrationProfileOptionDeleteSideEffects.seam";
    }

    @Override
    public String deleteIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteIntegrationProfileOption");
        }

        if (selectedIntegrationProfileOption != null) {
            try {
                Query q = entityManager.createQuery(
                        "SELECT option from IntegrationProfileOption option join fetch option.listOfActorIntegrationProfileOption where option = " +
                                ":inOption");
                q.setParameter("inOption", selectedIntegrationProfileOption);

                List<IntegrationProfileOption> lipo = q.getResultList();
                IntegrationProfileOption td = null;
                if (lipo != null) {
                    if (lipo.size() > 0) {
                        td = lipo.get(0);
                    }
                }
                if (td == null) {
                    td = entityManager.find(IntegrationProfileOption.class, selectedIntegrationProfileOption.getId());
                }

                if (td != null) {
                    for (ActorIntegrationProfileOption aipo : td.getActorIntegrationProfileOption()) {
                        entityManager.remove(aipo);
                    }

                    entityManager.remove(td);
                    entityManager.flush();
                    FacesMessages.instance()
                            .add(StatusMessage.Severity.INFO, "Option " + selectedIntegrationProfileOption.getKeyword() + " successfully deleted");
                }
            } catch (Exception e) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to delete option : " + e.getMessage());
                LOG.error("", e);
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to delete option null");

        }

        selectedIntegrationProfileOption = null;

        return listIntegrationProfileOptions();
    }

    @Override
    public String showIntegrationProfileOption(IntegrationProfileOption inSelectedIntegrationProfileOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showIntegrationProfileOption");
        }
        return editIntegrationProfileOption(inSelectedIntegrationProfileOption, false);
    }

    @Override
    public String editIntegrationProfileOption(IntegrationProfileOption inSelectedIntegrationProfileOption, boolean e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editIntegrationProfileOption");
        }
        edit = e;
        if (edit) {

        } else {

        }

        selectedIntegrationProfileOption = entityManager
                .find(IntegrationProfileOption.class, inSelectedIntegrationProfileOption.getId());

        aipoCount = getActorsAndIntegrationProfileForIntegrationProfileOption().getAllItems(FacesContext.getCurrentInstance()).size();
        // ** set edit/view titles ** //
        if (edit) {
            editTitle = "gazelle.tf.IntegrationProfileOption";
            editSubTitle = "gazelle.tf.integrationProfileOption.EditIntegrationProfileOptionInformationHeader";
            editDescription = "gazelle.tf.integrationProfileOption.EditIntegrationProfileOptionInformationDescription";
        } else {
            editTitle = "gazelle.tf.IntegrationProfileOption";
            editSubTitle = "gazelle.tf.integrationProfileOption.ShowIntegrationProfileOptionInformationHeader";
            editDescription = "gazelle.tf.integrationProfileOption.ShowIntegrationProfileOptionInformationDescription";
        }
        return "/tf/integrationProfileOption/editIntegrationProfileOption.seam";
    }

    @Override
    public String viewIntegrationProfileOption(IntegrationProfileOption selectedIntegrationProfileOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewIntegrationProfileOption");
        }
        return editIntegrationProfileOption(selectedIntegrationProfileOption, false);
    }

    @Override
    public String editIntegrationProfileOption(IntegrationProfileOption selectedIntegrationProfileOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editIntegrationProfileOption");
        }
        return editIntegrationProfileOption(selectedIntegrationProfileOption, true);
    }

    @Override
    public String editIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editIntegrationProfileOption");
        }
        return editIntegrationProfileOption(selectedIntegrationProfileOption, true);
    }

    @Override
    public String addNewIntegrationProfileOptionButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewIntegrationProfileOptionButton");
        }

        selectedIntegrationProfileOption = new IntegrationProfileOption();
        editTitle = "gazelle.tf.IntegrationProfileOption";
        editSubTitle = "gazelle.tf.integrationProfileOption.AddIntegrationProfileOptionInformationHeader";
        editDescription = "gazelle.tf.integrationProfileOption.AddIntegrationProfileOptionInformationDescription";
        aipoCount = 0;
        edit = true;
        return "/tf/integrationProfileOption/editIntegrationProfileOption.seam";
    }

    @Override
    public String listIntegrationProfileOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listIntegrationProfileOptions");
        }

        return "/tf/integrationProfileOption/listIntegrationProfileOptions.seam";
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public void getSpecificIntegrationProfileOptionFromKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSpecificIntegrationProfileOptionFromKeyword");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String ipokeyword = params.get("keyword");

        selectedIntegrationProfileOption = IntegrationProfileOption.findIntegrationProfileOptionWithKeyword(ipokeyword);
        this.editIntegrationProfileOption(selectedIntegrationProfileOption, false);
    }

    @Override
    public String addSectionPage(ActorIntegrationProfileOption aipo, String callerPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSectionPage");
        }

        filter = null;
        documentSections = null;
        selectedAipo = aipo;
        this.callerPage = callerPage;
        getFilter();

        return "/tf/integrationProfileOption/addDocumentSection.xhtml";
    }

    @Override
    public DocumentSectionsFilter getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            filter = new DocumentSectionsFilter(requestParameterMap);
        }
        return filter;
    }

    @Override
    public FilterDataModel<DocumentSection> getDocumentSections() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentSections");
        }
        if (documentSections == null) {
            documentSections = new FilterDataModel<DocumentSection>(getFilter()) {
                @Override
                protected Object getId(DocumentSection t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return documentSections;
    }

    @Override
    public String addThisSection(DocumentSection documentSection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addThisSection");
        }

        selectedAipo.setDocumentSection(documentSection);
        try {
            entityManager.merge(selectedAipo);
            entityManager.flush();
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Cannot link this document section");

        }
        return getReturnPage();
    }

    /**
     * Delete a selected Document
     */
    @Override
    public void deleteSection(ActorIntegrationProfileOption aipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSection");
        }
        selectedAipo = aipo;

        selectedAipo.setDocumentSection(null);
        try {
            entityManager.merge(selectedAipo);
            entityManager.flush();
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This section cannot be deleted");

        }
    }

    @Override
    public String cancelAddDocumentSection(ActorIntegrationProfileOption aipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelAddDocumentSection");
        }
        edit = true;
        selectedAipo = entityManager.find(ActorIntegrationProfileOption.class, aipo.getId());

        return getReturnPage();
    }

    private String getReturnPage() {
        String pageToReturn = null;
        if (callerPage.equals("integrationProfileOption")) {
            pageToReturn = "/tf/integrationProfileOption/editIntegrationProfileOption.seam";
        } else if (callerPage.equals("actor")) {
            pageToReturn = "/tf/actor/editActor.seam";
            setSelectedTab("integrationProfileOptions");
        } else if (callerPage.equals("consistencyCheck")) {
            pageToReturn = "/tf/utilities/tfConsistencyCheckList.seam";
        }

        callerPage = null;
        return pageToReturn;
    }

    @Override
    public ActorIntegrationProfileOption getSelectedAipo() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedAipo");
        }
        return this.selectedAipo;
    }

    @Override
    public void setSelectedAipo(ActorIntegrationProfileOption selectedAipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedAipo");
        }
        this.selectedAipo = selectedAipo;
    }

    @Override
    public String getSelectedTab() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTab");
        }
        return selectedTab;
    }

    @Override
    public void setSelectedTab(String selectedTab) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTab");
        }
        this.selectedTab = selectedTab;
    }

    @Override
    public int countAipoWithoutDocSectionLinkedToThisIntegrationProfileOption(IntegrationProfileOption selectedIPO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("countAipoWithoutDocSectionLinkedToThisIntegrationProfileOption");
        }
        selectedIntegrationProfileOption = entityManager.find(IntegrationProfileOption.class, selectedIPO.getId());
        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
        query.integrationProfileOption().id().eq(selectedIntegrationProfileOption.getId());
        query.documentSection().isEmpty();
        query.integrationProfileOption().keyword().neq("NONE");
        return query.getListDistinct().size();
    }

    public int getSelectedAipoForDetail() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedAipoForDetail");
        }
        return selectedAipoForDetail;
    }

    public void setSelectedAipoForDetail(int selectedAipoForDetail) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedAipoForDetail");
        }
        this.selectedAipoForDetail = selectedAipoForDetail;
    }

    public void redirectToAIPO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("redirectToAIPO");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        String keyword = fc.getExternalContext().getRequestParameterMap().get("keyword");
        Redirect redirect = Redirect.instance();
        Contexts.getSessionContext().set("backPage", null);
        Contexts.getSessionContext().set("backMessage", null);
        redirect.setViewId("/tf/integrationProfile/listIntegrationProfiles.seam");
        if (keyword != null) {

            ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
            query.id().eq(Integer.valueOf(keyword));
            ActorIntegrationProfileOption uniqueResult = query.getUniqueResult();
            if (uniqueResult != null) {
                IntegrationProfile integrationProfile = uniqueResult.getActorIntegrationProfile()
                        .getIntegrationProfile();

                redirect.setParameter("keyword", integrationProfile.getKeyword());
                redirect.setViewId("/profile.seam");
            } else {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Cannot find the requested AIPO, keyword=" + keyword, null);
                FacesMessages.instance().add(message);

            }
        }
        redirect.execute();
    }

    @Override
    public boolean validateKeyword(String keyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateKeyword");
        }
        IntegrationProfileOptionQuery q = new IntegrationProfileOptionQuery();
        List<String> listIntegrationProfileOption = q.keyword().getListDistinct();
        boolean valid = !listIntegrationProfileOption.contains(keyword);
        if (!valid) {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("keyword", StatusMessage.Severity.ERROR,
                    "This keyword already exist, please change !", "This keyword already exist, please change !");
        }
        return valid;
    }
}
