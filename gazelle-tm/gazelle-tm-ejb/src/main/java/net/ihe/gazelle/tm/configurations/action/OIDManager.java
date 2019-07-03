package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.util.DocumentFileUpload;
import net.ihe.gazelle.csv.CSVExporter;
import net.ihe.gazelle.hql.HQLReloader;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.configurations.model.OIDRequirement;
import net.ihe.gazelle.tm.configurations.model.OIDRootDefinition;
import net.ihe.gazelle.tm.configurations.model.OIDRootDefinitionQuery;
import net.ihe.gazelle.tm.configurations.model.OIDSystemAssignment;
import net.ihe.gazelle.tm.systems.model.SystemActorProfiles;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author abderrazek boufahja
 */

@Scope(ScopeType.SESSION)
@Name("oidManager")
@GenerateInterface("OIDManagerLocal")
public class OIDManager implements Serializable, OIDManagerLocal {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(OIDManager.class);
    public static Integer TEXT_MAX_SIZE_FOR_DROPDOWNLIST = 30;
    public static Integer TEXT_MAX_SIZE_FOR_DROPDOWNLIST_SYSTEM = 30;

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    private OIDRequirement selectedOIDRequirement;
    private Filter<OIDRootDefinition> filter;

    private ActorIntegrationProfileOption selectedAIPO;

    private boolean AIPOInitialized;
    private OIDRootDefinition selectedOIDRootDefinition;

    @Override
    public OIDRequirement getSelectedOIDRequirement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedOIDRequirement");
        }
        return selectedOIDRequirement;
    }

    @Override
    public void setSelectedOIDRequirement(OIDRequirement selectedOIDRequirement) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedOIDRequirement");
        }
        this.selectedOIDRequirement = selectedOIDRequirement;
    }

    @Override
    public ActorIntegrationProfileOption getSelectedAIPO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedAIPO");
        }
        return selectedAIPO;
    }

    @Override
    public void setSelectedAIPO(ActorIntegrationProfileOption selectedAIPO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedAIPO");
        }
        this.selectedAIPO = selectedAIPO;
    }

    @Override
    public boolean isAIPOInitialized() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAIPOInitialized");
        }
        return AIPOInitialized;
    }

    @Override
    public void setAIPOInitialized(boolean AIPOInitialized) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAIPOInitialized");
        }
        this.AIPOInitialized = AIPOInitialized;
    }

    @Override
    public String editSelectedOIDRequirement(OIDRequirement oidRequirement) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editSelectedOIDRequirement");
        }
        this.selectedOIDRequirement = HQLReloader.reloadDetached(oidRequirement);
        this.selectedOIDRequirement.getActorIntegrationProfileOptionList().size();
        return "/configuration/oid/editOIDRequirement.seam";
    }

    @Override
    public void saveSelectedOIDRequirement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveSelectedOIDRequirement");
        }
        if (this.selectedOIDRequirement != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            this.selectedOIDRequirement = em.merge(this.selectedOIDRequirement);
            em.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Modification of the selected OIDRequirement was saved");
        }
    }

    @Override
    public void deleteSelectedAIPOFromSelectedOIDRequirement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedAIPOFromSelectedOIDRequirement");
        }
        if (this.selectedOIDRequirement != null) {
            if (this.selectedAIPO != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                this.selectedOIDRequirement.getActorIntegrationProfileOptionList().remove(this.selectedAIPO);
                this.selectedOIDRequirement = em.merge(this.selectedOIDRequirement);
                em.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "The selected AIPO was deleted from the current OID Requirement.");
                resetAllAIPO();
            }
        }
    }

    @Override
    public void initializeSelectedAIPO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeSelectedAIPO");
        }
        if (this.selectedAIPO == null || isAIPOInitialized()) {
            this.selectedAIPO = new ActorIntegrationProfileOption();
            setAIPOInitialized(true);
        }
        if (this.selectedAIPO.getActorIntegrationProfile() == null) {
            this.selectedAIPO.setActorIntegrationProfile(new ActorIntegrationProfile());
            this.selectedAIPO.getActorIntegrationProfile().setActor(null);
            this.selectedAIPO.getActorIntegrationProfile().setIntegrationProfile(null);
        }
    }

    @Override
    public List<IntegrationProfile> getPossibleIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfiles");
        }
        return IntegrationProfile.getAllIntegrationProfile();
    }

    @Override
    public List<Actor> getPossibleActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleActors");
        }
        if (this.selectedAIPO.getActorIntegrationProfile().getIntegrationProfile() == null) {
            return null;
        }
        return Actor.getActorFiltered(null, this.selectedAIPO.getActorIntegrationProfile().getIntegrationProfile(),
                null);
    }

    @Override
    public List<IntegrationProfileOption> getPossibleOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleOptions");
        }
        if (this.selectedAIPO != null) {
            if (this.selectedAIPO.getActorIntegrationProfile() != null) {
                if ((this.selectedAIPO.getActorIntegrationProfile().getActor() != null)
                        && (this.selectedAIPO.getActorIntegrationProfile().getIntegrationProfile() != null)) {
                    return IntegrationProfileOption.getListOfIntegrationProfileOptions(this.selectedAIPO
                            .getActorIntegrationProfile().getActor(), this.selectedAIPO.getActorIntegrationProfile()
                            .getIntegrationProfile());
                }
            }
        }
        return null;
    }

    @Override
    public void resetAIPO(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetAIPO");
        }
        if (i == 1) {
            this.selectedAIPO.getActorIntegrationProfile().setActor(null);
            this.selectedAIPO.setIntegrationProfileOption(null);
        }
        if (i == 2) {
            this.selectedAIPO.setIntegrationProfileOption(null);
        }

    }

    @Override
    public void resetAllAIPO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetAllAIPO");
        }
        resetAIPO(1);
        resetAIPO(2);
        this.selectedAIPO = null;
        setAIPOInitialized(false);
    }

    @Override
    public void addNewAIPOToOIDRequirement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewAIPOToOIDRequirement");
        }
        if (this.selectedAIPO != null) {
            if ((this.selectedAIPO.getActorIntegrationProfile() != null)
                    && (this.selectedAIPO.getIntegrationProfileOption() != null)) {
                if ((this.selectedAIPO.getActorIntegrationProfile().getActor() != null)
                        && (this.selectedAIPO.getActorIntegrationProfile().getIntegrationProfile() != null)) {
                    EntityManager em = EntityManagerService.provideEntityManager();
                    ActorIntegrationProfileOption aipo = ActorIntegrationProfileOption
                            .getActorIntegrationProfileOption(
                                    this.selectedAIPO.getActorIntegrationProfile().getActor(), this.selectedAIPO
                                            .getActorIntegrationProfile().getIntegrationProfile(), this.selectedAIPO
                                            .getIntegrationProfileOption());
                    if (aipo == null) {
                        FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                                "A problem Occure on adding selected ActorIntegrationProfileOption.");
                        return;
                    }
                    if (this.selectedOIDRequirement != null) {
                        if (this.selectedOIDRequirement.getId() != null) {
                            if (this.selectedOIDRequirement.getActorIntegrationProfileOptionList() == null) {
                                this.selectedOIDRequirement
                                        .setActorIntegrationProfileOptionList(new ArrayList<ActorIntegrationProfileOption>());
                            }
                            this.selectedOIDRequirement.getActorIntegrationProfileOptionList().add(aipo);
                            this.selectedOIDRequirement = em.merge(this.selectedOIDRequirement);
                            em.flush();
                            FacesMessages.instance().add(StatusMessage.Severity.INFO, "The ActorIntegrationProfileOption selected was added.");
                        }
                    }
                }
            }
        }
        resetAllAIPO();
    }

    @Override
    public String addNewOIDRequirement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewOIDRequirement");
        }
        this.selectedOIDRequirement = new OIDRequirement();
        return "/configuration/oid/editOIDRequirement.seam";
    }

    @Override
    public void deleteSelectedOIDRequirement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedOIDRequirement");
        }
        if (this.selectedOIDRequirement != null) {
            if (this.selectedOIDRequirement.getId() != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                this.selectedOIDRequirement = em.find(OIDRequirement.class, this.selectedOIDRequirement.getId());
                em.remove(this.selectedOIDRequirement);
                this.selectedOIDRequirement = null;
                em.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "The selected OID Requirement was deleted.");
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canDeleteSelectedOIDRequirement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canDeleteSelectedOIDRequirement");
        }
        if (this.selectedOIDRequirement != null) {
            if (this.selectedOIDRequirement.getId() != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                this.selectedOIDRequirement = em.find(OIDRequirement.class, this.selectedOIDRequirement.getId());
                if (this.selectedOIDRequirement != null) {
                    List<OIDSystemAssignment> losa = OIDSystemAssignment.getOIDSystemAssignmentFiltered(null,
                            this.selectedOIDRequirement.getLabel(), null, null);
                    if (losa != null) {
                        if (losa.size() > 0) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public OIDRootDefinition getSelectedOIDRootDefinition() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedOIDRootDefinition");
        }
        return selectedOIDRootDefinition;
    }

    @Override
    public void setSelectedOIDRootDefinition(OIDRootDefinition selectedOIDRootDefinition) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedOIDRootDefinition");
        }
        this.selectedOIDRootDefinition = selectedOIDRootDefinition;
    }

    @Override
    public void saveOIDRootDefinition() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveOIDRootDefinition");
        }
        if (this.selectedOIDRootDefinition != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            this.selectedOIDRootDefinition = em.merge(this.selectedOIDRootDefinition);
            em.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "The selected OID Root Definition was saved.");
        }
    }

    @Override
    public void deleteOIDRootDefinition() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteOIDRootDefinition");
        }
        if (this.selectedOIDRootDefinition != null) {
            if (this.selectedOIDRootDefinition.getId() != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                this.selectedOIDRootDefinition = em.find(OIDRootDefinition.class,
                        this.selectedOIDRootDefinition.getId());
                em.remove(this.selectedOIDRootDefinition);
                em.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "The selected OID Root Definition was deleted.");
            }
        }
    }

    @Override
    public boolean canDeleteSelectedOIDRootDefinition() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canDeleteSelectedOIDRootDefinition");
        }
        if (this.selectedOIDRootDefinition != null) {
            if (this.selectedOIDRootDefinition.getId() != null) {
                List<OIDRequirement> lorq = this.selectedOIDRootDefinition.getListOIDRequirements();
                if (lorq != null) {
                    if (lorq.size() > 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void initializeOIDRootDefinition() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeOIDRootDefinition");
        }
        this.selectedOIDRootDefinition = new OIDRootDefinition();
        this.selectedOIDRootDefinition.setLastValue(0);
    }

    @Override
    public void deleteAllOIDSystemAssignment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllOIDSystemAssignment");
        }
        this.deleteAllOIDSystemAssignment1();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "All OID System Assignment are deleted.");
    }

    private void deleteAllOIDSystemAssignment1() {
        List<OIDSystemAssignment> losa = OIDSystemAssignment.getOIDSystemAssignmentFiltered(null, null,
                TestingSession.getSelectedTestingSession(), null);
        EntityManager em = EntityManagerService.provideEntityManager();
        if (losa != null) {
            for (OIDSystemAssignment oidSystemAssignment : losa) {
                em.remove(oidSystemAssignment);
                em.flush();
            }
        }
        em.flush();
    }

    @Override
    public void deleteAndGenerateAllOIDSystemAssignment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAndGenerateAllOIDSystemAssignment");
        }
        this.deleteAllOIDSystemAssignment1();
        this.updateSequenceOfOIDRootDefinition();
        this.updateOIDSystemAssignment1();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "All OID System Assignment are deleted and regenerated.");
    }

    @Override
    public void updateOIDSystemAssignment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateOIDSystemAssignment");
        }
        this.updateOIDSystemAssignment1();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "OID System Assignment are updated.");
    }

    private void updateSequenceOfOIDRootDefinition() {
        EntityManager em = EntityManagerService.provideEntityManager();
        List<OIDRootDefinition> lord = OIDRootDefinition.getAllOIDRootDefinition();
        for (OIDRootDefinition oidRootDefinition : lord) {
            List<OIDRequirement> lor = OIDRequirement.getOIDRequirementFiltered(oidRootDefinition, null);
            int last = 1;
            for (OIDRequirement oidRequirement : lor) {
                List<OIDSystemAssignment> losa = OIDSystemAssignment.getOIDSystemAssignmentFiltered(null,
                        oidRequirement.getLabel(), null, null);
                if (losa != null) {
                    for (OIDSystemAssignment oidSystemAssignment : losa) {

                        String ss = oidSystemAssignment.getLastIndex();
                        if ((ss != null) && (!ss.equals(""))) {
                            try {
                                last = Math.max(last, Integer.valueOf(ss));
                            } catch (Exception e) {
                                LOG.error("prob on the oid of "
                                        + oidSystemAssignment.getSystemInSession().getSystem().getKeyword()
                                        + " : the oid is not a real oid.");
                            }
                        }
                    }
                }
            }
            oidRootDefinition.setLastValue(last);
            em.merge(oidRootDefinition);
            em.flush();
        }
    }

    private void updateOIDSystemAssignment1() {
        EntityManager em = EntityManagerService.provideEntityManager();
        List<OIDRequirement> lor = OIDRequirement.getAllOIDRequirement();
        for (OIDRequirement oidRequirement : lor) {
            List<SystemInSession> lsis = SystemInSession.getSystemInSessionFiltered(em, null,
                    TestingSession.getSelectedTestingSession(), null, null, null, null, null, null, null, null, null,
                    null);
            for (SystemInSession systemInSession : lsis) {
                List<SystemActorProfiles> lsap = SystemActorProfiles.getSystemActorProfilesFiltered(em, null,
                        systemInSession.getSystem(), null, null, null, null, null, null);
                for (SystemActorProfiles systemActorProfiles : lsap) {
                    if (oidRequirement.getActorIntegrationProfileOptionList().contains(
                            systemActorProfiles.getActorIntegrationProfileOption())) {
                        boolean oidexist = false;
                        List<OIDSystemAssignment> losa = OIDSystemAssignment.getOIDSystemAssignmentFiltered(
                                systemInSession, oidRequirement.getLabel(), null, null);
                        if (losa != null) {
                            if (losa.size() > 0) {
                                oidexist = true;
                            }
                        }
                        if (!oidexist) {
                            OIDSystemAssignment osa = new OIDSystemAssignment();
                            osa.setOidRequirement(oidRequirement);
                            osa.setSystemInSession(systemInSession);
                            oidRequirement.getOidRootDefinition().setLastValue(
                                    oidRequirement.getOidRootDefinition().getLastValue() + 1);
                            String oid = oidRequirement.getOidRootDefinition().generateNewOID();
                            osa.setOid(oid);

                            em.merge(osa);

                            OIDRootDefinition ord = oidRequirement.getOidRootDefinition();
                            em.merge(ord);

                            em.flush();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void getSpecifiedOIDsBySytemKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSpecifiedOIDsBySytemKeyword");
        }

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String systemKeyword = params.get("systemKeyword");
        String testingSessionIdString = params.get("testingSessionId");
        String oidRequirementLabel = params.get("requirement");
        if (systemKeyword == null) {
            if (oidRequirementLabel == null) {
                String res = "You have to specify the systemKeyword or the requirement. \n "
                        + "Example : "
                        + ApplicationPreferenceManager.instance().getApplicationUrl()
                        + "oidSystems.seam?systemKeyword=EHR_Rogan_XDS&testingSessionId=15&requirement=homeCommunity ID";
                this.showFile(res, "error.txt");
                return;
            }
        }
        if (testingSessionIdString == null) {
            String res = "You have to specify the testingSessionId. \n " + "Example : "
                    + ApplicationPreferenceManager.instance().getApplicationUrl()
                    + "oidSystems.seam?systemKeyword=EHR_Rogan_XDS&testingSessionId=15";
            this.showFile(res, "error.txt");
            return;
        }
        TestingSession ts = TestingSession.GetSessionById(Integer.valueOf(testingSessionIdString));
        if (ts == null) {
            String res = "The specified testingSessionId is not well";
            this.showFile(res, "error.txt");
            return;
        }
        net.ihe.gazelle.tm.systems.model.System sys = net.ihe.gazelle.tm.systems.model.System
                .getSystemByAllKeyword(systemKeyword);
        List<OIDRequirement> loreq = OIDRequirement.getOIDRequirementFiltered(null, oidRequirementLabel);
        if (loreq != null) {
            if (loreq.size() == 0) {
                loreq = null;
            }
        }
        if (loreq == null) {
            if (oidRequirementLabel != null) {
                String res = "The specified requirement is not well\b "
                        + "You have to use word like homeCommunityID OID, source OID, etc.";
                this.showFile(res, "error.txt");
                return;
            }
            if (sys == null) {
                String res = "The specified systemKeyword does not exist";
                this.showFile(res, "error.txt");
                return;
            }
        }

        SystemInSession sis = SystemInSession.getSystemInSessionForSession(sys, ts);
        List<OIDSystemAssignment> losa = OIDSystemAssignment.getOIDSystemAssignmentFiltered(sis, oidRequirementLabel,
                null, null);
        if (losa == null) {
            losa = new ArrayList<OIDSystemAssignment>();
        }
        if (losa.size() == 0) {
            String res = "There are no OID for the specified parameters.";
            this.showFile(res, systemKeyword + "_OIDs.txt");
            return;
        } else {
            List exportables = losa;
            String csv = CSVExporter.exportCSV(exportables);
            this.showFile(csv, systemKeyword + "_OIDs.csv");
            return;
        }

    }

    private void showFile(String contentString, String filename) {
        try {
            DocumentFileUpload.showFile(contentString.getBytes(StandardCharsets.UTF_8.name()), filename, true);
        } catch (UnsupportedEncodingException e1) {
            LOG.error(e1.getMessage(), e1);
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }
    }

    public Filter<OIDRootDefinition> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            filter = new Filter<OIDRootDefinition>(getHQLCriterions());
        }
        return filter;
    }

    private HQLCriterionsForFilter<OIDRootDefinition> getHQLCriterions() {
        OIDRootDefinitionQuery query = new OIDRootDefinitionQuery();
        return query.getHQLCriterionsForFilter();
    }

    public FilterDataModel<OIDRootDefinition> getOIDRoots() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOIDRoots");
        }
        return new FilterDataModel<OIDRootDefinition>(getFilter()) {
            @Override
            protected Object getId(OIDRootDefinition oidRoot) {
                return oidRoot.getId();
            }
        };
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
