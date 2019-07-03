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
package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.common.report.ReportExporterManager;
import net.ihe.gazelle.hql.HQLReloader;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.messaging.MessagePropertyChanged;
import net.ihe.gazelle.messaging.MessagingService;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import net.ihe.gazelle.tm.financial.FinancialSummary;
import net.ihe.gazelle.tm.financial.FinancialSummaryOneSystem;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemAIPOResultForATestingSession;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.utils.systems.IHEImplementationForSystem;
import net.ihe.gazelle.tm.utils.systems.IHEImplementationForSystemComparatorActor;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.util.Pair;
import net.sf.jasperreports.engine.JRException;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static org.jboss.seam.ScopeType.PAGE;

/**
 * <b>Class Description : </b>SystemManager<br>
 * <br>
 * This class manage the System object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add a system
 * (login)</li> <li>Delete a system</li> <li>Show a
 * system</li> <li>Show an Integration Statement</li> <li>Edit a system</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 */

@Name("iheImplementationForSystemManager")
@Scope(PAGE)
@GenerateInterface("IHEImplementationForSystemManagerLocal")
public class IHEImplementationForSystemManager implements Serializable, IHEImplementationForSystemManagerLocal {
    // ~ Statics variables and Class initializer
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -450911336363583760L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(IHEImplementationForSystemManager.class);
    /**
     * Keyword used for rendering
     */
    public static String noneKeywordString = "NONE";
    /**
     * Description used for rendering
     */
    public static String noneDescriptionString = "None";
    /**
     * Keyword used to render that all integration profile options are selected for this search criteria
     */
    public static String noneNameIntegrationProfileOptionString = "None integration profile option for this implementation";
    /**
     * Integration Profile Option defined as None
     */
    String valueOptionNONE = "'NONE'";

    // ~ Attributes
    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Default TestingType Id, this TestingType is automatically assigned to all new SystemActorProfiles
     */
    Integer defaultTestingTypeId = 1;
    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;
    /**
     * List of all the IHE implementations for a system (all Actor/IntegrationProfile/Integration Profile Option combinations)
     */
    private List<IHEImplementationForSystem> iheImplementations;
    private List<IHEImplementationForSystem> iheImplementationsToDisplay;
    private int iForIHEImplementationsIntegrationProfile = 0;
    private int iForIHEImplementationsActor = 0;
    private String sortOrderActor;
    /**
     * FinancialSummary contains informations for billing (nb systems/nb domains/ fees amounts)
     */
    @Out(scope = ScopeType.SESSION, required = false)
    private FinancialSummary financialSummary;

    /**
     * List of Financial summary for EACH system of a company (nb domains/ fees amounts), used to display financial details of systems
     */
    @Out(scope = ScopeType.SESSION, required = false)
    private List<FinancialSummaryOneSystem> financialSummaryForOneSystem;

    /**
     * Institution object : Choosen Institution, that admin selected in the institution list
     */
    private Institution choosenInstitutionForAdmin;

    /**
     * SelectedSystemInSession object managed my this manager bean and injected in JSF
     */
    private SystemInSession selectedSystemInSession;

    private IHEImplementationForSystem iheImplementationForSystemToDelete;

    // ~ Methods
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public IHEImplementationForSystemManager() {
    }

    /**
     * Reset all the IHE implementations associated to a system
     *
     * @return null : Page to render, but it's now an Ajax action, so we do not render a page, we just refresh panels
     */
    @Override
    public void resetSelectedIHEImplementationForSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetSelectedIHEImplementationForSystem");
        }
        int iIHEImplementationsSize = 0;

        iIHEImplementationsSize = iheImplementations.size();

        for (int i = 0; i < iIHEImplementationsSize; i++) {
            deleteIHEImplementationForSystem(iheImplementations.get(i), true);
        }
        if (iheImplementations != null) {
            iheImplementations.clear();
        }
        iheImplementations = null;
    }

    /**
     * Get the list of all ActorIntegrationProfileOption for a selected system in session
     */

    @Override
    public void getAllIHEImplementationsForSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllIHEImplementationsForSystemInSession");
        }
        init();
        getAllIHEImplementationsForSystemInSession(selectedSystemInSession);
    }

    @Override
    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return selectedSystemInSession;
    }

    @Override
    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        this.selectedSystemInSession = selectedSystemInSession;
    }

    @Create
    @Override
    public void init() {
        final String systemInSessionId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (systemInSessionId != null) {
            try {
                Integer id = Integer.parseInt(systemInSessionId);
                selectedSystemInSession = entityManager.find(SystemInSession.class, id);
            } catch (NumberFormatException e) {
                LOG.error("failed to find system in session with id = " + systemInSessionId);
            }
        }
    }

    /**
     * Get the list of all ActorIntegrationProfileOption for a selected system in session
     */
    @Override
    @SuppressWarnings("unchecked")
    public void getAllIHEImplementationsForSystemInSession(SystemInSession sis) {
        if (sis != null) {
            getAllIHEImplementationsForSystem(sis.getSystem());
        } else {
            LOG.debug("System in session is null");
        }
    }

    /**
     * Get the list of all ActorIntegrationProfileOption for a selected system
     */

    @Override
    @SuppressWarnings("unchecked")
    public void getAllIHEImplementationsForSystem(System s) {
        iheImplementations = IHEImplementationForSystemDAO.getAllIHEImplementationsForSystem(s);
        iheImplementationsToDisplay = IHEImplementationForSystemDAO.getIHEImplementationsToDisplay(iheImplementations);
    }

    @Override
    public List<IHEImplementationForSystem> displayAllIHEImplementationsForSystem(System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayAllIHEImplementationsForSystem");
        }
        if (system != null) {
            getAllIHEImplementationsForSystem(system);
        }
        return iheImplementations;
    }

    /**
     * Add a combination Actor/Profile/OPtion to a system, if this combination does not exist. Also calculate the news fees amount.
     *
     * @param d   : Domain
     * @param ip  : Integration Profile
     * @param a   : Actor
     * @param ipo : Integration Profile Option
     *
     * @return JSF page to render
     */
    @Override
    public void addIHEImplementationForSystem(ActorIntegrationProfileOption inActorIntegrationProfileOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addIHEImplementationForSystem");
        }
        try {

            TestingType defaultTestingType;
            inActorIntegrationProfileOption = ActorIntegrationProfileOption.getActorIntegrationProfileOption(
                    inActorIntegrationProfileOption.getActorIntegrationProfile().getActor(),
                    inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile(),
                    inActorIntegrationProfileOption.getIntegrationProfileOption());

            inActorIntegrationProfileOption = entityManager.find(ActorIntegrationProfileOption.class,
                    inActorIntegrationProfileOption.getId());

            if (inActorIntegrationProfileOption == null) {
                LOG.warn("addIHEImplementationForSystem : ActorIntegrationProfileOption is null...");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tf.faces.AIPONull");

            } else if (inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile() == null) {
                LOG.warn("addIHEImplementationForSystem : IntegrationProfile a is null...");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tf.faces.IPNull");

            } else if (inActorIntegrationProfileOption.getActorIntegrationProfile().getActor() == null) {
                LOG.warn("addIHEImplementationForSystem : Actor a is null...");
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tf.faces.ActorNull");

            } else if (inActorIntegrationProfileOption.getIntegrationProfileOption() == null) {
                inActorIntegrationProfileOption.setIntegrationProfileOption(new IntegrationProfileOption(
                        noneKeywordString, noneDescriptionString));

            }

            IHEImplementationForSystem iheImplementationForSystem = null;
            if (inActorIntegrationProfileOption != null) {
                iheImplementationForSystem = new IHEImplementationForSystem(
                        inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile(),
                        inActorIntegrationProfileOption.getActorIntegrationProfile().getActor(),
                        inActorIntegrationProfileOption.getIntegrationProfileOption());
            }

            // We create the list containing all IHE Implementations for the
            // Integration Statement, if it does not exist
            if (iheImplementations == null) {
                iheImplementations = new Vector<IHEImplementationForSystem>();
            }

            // We check that the asked IHEImplementationForSystem does not exist
            // in the IHE Implementations list
            if (isExistingIHEImplementationForSystem(iheImplementationForSystem)) {
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("facesM",
                        StatusMessage.Severity.WARN, "gazelle.systems.system.faces.ThisIHEImplementationAlreadyExists",
                        "gazelle.systems.system.faces.ThisIHEImplementationAlreadyExists");
            } else {
                // We keep the selected system in the session

                iheImplementations.add(iheImplementationForSystem);

                // We retrieve the default TestingType from the entity, this
                // TestingType will be associated to this new SystemActorProfile
                defaultTestingType = entityManager.find(TestingType.class, defaultTestingTypeId);

                // Add SystemActorProfile entity into Database
                SystemActorProfiles systemActorProfile;

                if (inActorIntegrationProfileOption.getIntegrationProfileOption().getKeyword().equals(noneKeywordString)) {
                    IntegrationProfileOption ipo1 = entityManager.find(IntegrationProfileOption.class, 1);

                    // We finally create sap with null option using with the
                    // option id = 1 (corresponds to the null option)
                    systemActorProfile = new SystemActorProfiles(selectedSystemInSession.getSystem(),
                            inActorIntegrationProfileOption.getActorIntegrationProfile().getActor(),
                            inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile(), ipo1,
                            defaultTestingType);
                } else {
                    systemActorProfile = new SystemActorProfiles(selectedSystemInSession.getSystem(),
                            inActorIntegrationProfileOption.getActorIntegrationProfile().getActor(),
                            inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile(),
                            inActorIntegrationProfileOption.getIntegrationProfileOption(), defaultTestingType);
                }

                entityManager.merge(systemActorProfile);
                entityManager.flush();

                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.systems.system.faces.IHEImplementationAdded",
                        inActorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile().getName(),
                        inActorIntegrationProfileOption.getActorIntegrationProfile().getActor().getName(),
                        inActorIntegrationProfileOption.getIntegrationProfileOption().getName(),
                        selectedSystemInSession.getSystem().getName());

                MessagingService.publishMessage(new MessagePropertyChanged<System, ActorIntegrationProfileOption>(
                        selectedSystemInSession.getSystem(), "addAIPO", null, inActorIntegrationProfileOption));
                entityManager.merge(selectedSystemInSession.getSystem());

                // Add the Null Option for this system/Actor/Profile if it does
                // not exist
                IntegrationProfileOption ipo1 = entityManager.find(IntegrationProfileOption.class, 1);

                List<SystemActorProfiles> sapList = SystemActorProfiles
                        .getSystemActorProfilesFiltered(entityManager, null, selectedSystemInSession.getSystem(), null,
                                null, null, iheImplementationForSystem.getIntegrationProfile(), ipo1,
                                iheImplementationForSystem.getActor());

                Integer resultOfOption = sapList.size();

                // If it does not exist, we create it
                if (resultOfOption == 0) {

                    // We finally create sap with null option using with the
                    // option id = 1 (corresponds to the null option)
                    SystemActorProfiles noneOptionSystemActorProfile = new SystemActorProfiles(
                            selectedSystemInSession.getSystem(), inActorIntegrationProfileOption
                            .getActorIntegrationProfile().getActor(), inActorIntegrationProfileOption
                            .getActorIntegrationProfile().getIntegrationProfile(), ipo1, defaultTestingType);

                    entityManager.merge(noneOptionSystemActorProfile);
                    entityManager.flush();
                }
            }

            reloadSystemInSession();

            return;
        } catch (ConstraintViolationException exc) {

            ExceptionLogging.logException(exc, LOG);
            LOG.error("Constraint Exception on : " + exc.getConstraintName());
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "gazelle.systems.system.faces.ThisIHEImplementationAlreadyExists");

        } catch (Exception exc2) {
            ExceptionLogging.logException(exc2, LOG);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.systems.system.faces.Error" + exc2.getMessage());

        }
    }

    /**
     * This method is used to add a Profile/Actor/Option during the TF dependencies checking. After checking TF dependencies, a modal panel is
     * rendered with missing dependencies. A 'Add it' link is
     * displayed close to each missing dependency. This following method is called when user clicks on that link.
     *
     * @param List < Pair < String, ActorIntegrationProfileOption>> aipoList ) : list of dependencies
     */
    @Override
    public void addIHEImplementationForSystem(List<Pair<String, ActorIntegrationProfileOption>> aipoList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addIHEImplementationForSystem");
        }
        try {

            for (Pair<String, ActorIntegrationProfileOption> aipo : aipoList) {
                addIHEImplementationForSystem(aipo.getObject2());
            }

        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
        }

    }

    @Override
    public boolean canDeleteIHEImplementationForSystem(IHEImplementationForSystem iheImplementationForSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canDeleteIHEImplementationForSystem");
        }
        // valid integration profile option
        if ((iheImplementationForSystem.getIntegrationProfileOption() != null)
                && (iheImplementationForSystem.getIntegrationProfileOption().getKeyword() != null)) {
            // option is none
            if ("none".equals(iheImplementationForSystem.getIntegrationProfileOption().getKeyword().toLowerCase())) {
                // look for other option of same actor integration profile
                for (IHEImplementationForSystem iheimpl : iheImplementationsToDisplay) {
                    // not this
                    if (!iheimpl.toString().equals(iheImplementationForSystem.toString())) {
                        // same actor, same integration profile
                        if (iheimpl.getActor().getId().equals(iheImplementationForSystem.getActor().getId())
                                && iheimpl.getIntegrationProfile().getId()
                                .equals(iheImplementationForSystem.getIntegrationProfile().getId())) {
                            // can not delete!
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Delete one IHE implementation associated to the current system (Delete one combination Actor/IP /IP Option)
     *
     * @param iheImplementationForSystem : IHE implementation to delete from the system
     * @param isReset                    : Boolean, indicating if we are working with the mode : "Reset ALL IHE implementations", this mode calls
     *                                   this metho as well
     *
     * @return JSF page to render
     */
    @Override
    public void deleteIHEImplementationForSystem(IHEImplementationForSystem iheImplementationForSystem, Boolean isReset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteIHEImplementationForSystem");
        }

        try {
            synchronized (this) {

                SystemActorProfiles currentSystemActorProfile;

                // Remove from displayed list (list of
                // IHEImplementationForSystem) if it's not a global reset action
                // (with the reset button)
                // If it's a global reset action, the list is cleared in the
                // reset method
                if (!isReset) {
                    iheImplementations.remove(iheImplementationForSystem);
                }

                // The IHE implementation to remove from the system has no
                // integration profile option
                if (iheImplementationForSystem.getIntegrationProfileOption() == null) {
                    currentSystemActorProfile = IHEImplementationForSystemDAO.getSystemActorProfiles(entityManager, iheImplementationForSystem, selectedSystemInSession);

                } else // The IHE implementation to remove from the system has
                // an integration profile option
                {
                    currentSystemActorProfile = IHEImplementationForSystemDAO.getSystemActorProfilesQuery(entityManager, iheImplementationForSystem, selectedSystemInSession);
                }

                System system = currentSystemActorProfile.getSystem();

                SystemAIPOResultForATestingSession.deleteSystemAIPOResult(currentSystemActorProfile);
                entityManager.remove(currentSystemActorProfile);

                MessagingService.publishMessage(new MessagePropertyChanged<System, ActorIntegrationProfileOption>(
                        selectedSystemInSession.getSystem(), "removeAIPO", currentSystemActorProfile
                        .getActorIntegrationProfileOption(), null));
                entityManager.merge(system);

                entityManager.flush();

                FacesMessages.instance().add(StatusMessage.Severity.INFO, "System AIPO: " + currentSystemActorProfile.toString() + " has been " +
                        "removed !");

                selectedSystemInSession = HQLReloader.reloadDetached(selectedSystemInSession);
                HQLReloader.reloadDetached(system);
                reloadSystemInSession();
            }
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            StatusMessages.instance().add("Err : " + e.getMessage());

        }

    }



    private void reloadSystemInSession() {
        if ((selectedSystemInSession != null) && (selectedSystemInSession.getId() != null)
                && (selectedSystemInSession.getSystem() != null)
                && (selectedSystemInSession.getSystem().getId() != null)) {

            EntityManagerService.provideEntityManager().merge(selectedSystemInSession);
            EntityManagerService.provideEntityManager().merge(selectedSystemInSession.getSystem());
            EntityManagerService.provideEntityManager().flush();

            SystemInSessionQuery systemInSessionQuery = new SystemInSessionQuery();
            systemInSessionQuery.id().eq(selectedSystemInSession.getId());
            selectedSystemInSession = systemInSessionQuery.getUniqueResult();
        }
    }

    /**
     * Check if an AIPO is already associated to the selected system in session
     *
     * @param iheImplementationForSystem : object containing AIPO information
     *
     * @return Boolean : true if the AIPO already exists for that system in session
     */
    @Override
    public Boolean isExistingIHEImplementationForSystem(IHEImplementationForSystem iheImplementationForSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isExistingIHEImplementationForSystem");
        }
        Boolean isExisting = false;
        Integer i;

        // We cannot compare IHE implementation using : isExisting =
        // iheImplementations.contains(iheImplementationForSystem); because
        // objects are different.

        for (i = 0; i < iheImplementations.size(); i++) {
            if (iheImplementations.get(i).getActor().getId().equals(iheImplementationForSystem.getActor().getId())) {
                if (iheImplementations.get(i).getIntegrationProfile().getId()
                        .equals(iheImplementationForSystem.getIntegrationProfile().getId())) {
                    if (iheImplementationForSystem.getIntegrationProfileOption().getKeyword().equals(noneKeywordString)) // if there is no
                    // integration profile option for this IHE implementation
                    {

                        if (iheImplementations.get(i).getIntegrationProfileOption().getKeyword()
                                .equals(noneKeywordString)) {

                            isExisting = true;
                        }
                    } else { // if there is an integration profile option for
                        // this IHE implementation

                        if (iheImplementations.get(i).getIntegrationProfileOption().getId()
                                .equals(iheImplementationForSystem.getIntegrationProfileOption().getId())) {

                            isExisting = true;
                        }
                    }
                }
            }
        }

        return isExisting;
    }

    /**
     * This method is called to generate an Integration Statement. An Integration Statement possesses all IHE features for a system.
     *
     * @param UserPreferences : Integration Statement to generate for this system
     *
     * @return String : JSF page to render
     */
    @Override
    public String generatePDF(System syst) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generatePDF");
        }
        if ((syst.getIntegrationStatementDate() == null)
                || (syst.getIntegrationStatementDate().toString().length() == 0)) {
            syst.setIntegrationStatementDate(new Date());
            entityManager.merge(syst);
        }
        return "/systems/system/integrationStatement.seam";
    }

    public String generatePDF(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generatePDF");
        }
        if ((systemInSession.getSystem().getIntegrationStatementDate() == null)
                || (systemInSession.getSystem().getIntegrationStatementDate().toString().length() == 0)) {
            systemInSession.getSystem().setIntegrationStatementDate(new Date());
            entityManager.merge(systemInSession.getSystem());
        }
        return "/systems/system/integrationStatement.seam?id=" + systemInSession.getId();
    }

    /**
     * Destroy the Manager bean when the session is over.
     */
    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }


    @Override
    public List<IHEImplementationForSystem> getIheImplementations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIheImplementations");
        }
        return iheImplementations;
    }

    @Override
    public void setIheImplementations(List<IHEImplementationForSystem> iheImplementations) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIheImplementations");
        }
        this.iheImplementations = iheImplementations;
    }

    @Override
    public List<IHEImplementationForSystem> getIheImplementationsToDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIheImplementationsToDisplay");
        }
        return iheImplementationsToDisplay;
    }

    @Override
    public void setIheImplementationsToDisplay(List<IHEImplementationForSystem> iheImplementationsToDisplay) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIheImplementationsToDisplay");
        }
        this.iheImplementationsToDisplay = iheImplementationsToDisplay;
    }

    @Override
    public Object sortBy(String field) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sortBy");
        }

        Object ret;

        if (field.equals("integrationProfile.keyword")) {
            ret = iheImplementations.get(iForIHEImplementationsIntegrationProfile++).getIntegrationProfile()
                    .getKeyword();
            if (iForIHEImplementationsIntegrationProfile == iheImplementations.size()) {
                iForIHEImplementationsIntegrationProfile = 0;
            }
            return ret;
        } else {
            ret = iheImplementations.get(iForIHEImplementationsActor++).getActor().getKeyword();
            if (iForIHEImplementationsActor == iheImplementations.size()) {
                iForIHEImplementationsActor = 0;
            }
            return ret;
        }

    }

    @Override
    public String getSortOrderActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSortOrderActor");
        }
        if (sortOrderActor.equals("UNSORTED")) {
            IHEImplementationForSystemComparatorActor cact = new IHEImplementationForSystemComparatorActor();
            java.util.Collections.sort(iheImplementations, cact);
            sortOrderActor = "ASCENDING";
        } else if (sortOrderActor.equals("ASCENDING")) {
            IHEImplementationForSystemComparatorActor cact = new IHEImplementationForSystemComparatorActor();
            java.util.Collections.sort(iheImplementations, cact);
            sortOrderActor = "DESCENDING";
        } else {
            IHEImplementationForSystemComparatorActor cact = new IHEImplementationForSystemComparatorActor();
            java.util.Collections.sort(iheImplementations, cact);
            sortOrderActor = "UNSORTED";
        }

        return sortOrderActor;
    }

    @Override
    public void setSortOrderActor(String sortOrderActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSortOrderActor");
        }
        this.sortOrderActor = sortOrderActor;
    }

    /**
     * WARNING: This method initializes variables before calling a static method. This small calling method also WARNING: exists in others beans
     * (Institution, System...). If you update
     * getFinancialSummaryDTO in a bean, don't WARNING: forget also to update other beans (use the Eclipse finder with a 'Search' :
     * getFinancialSummaryDTO ) This method is called to calculate the new
     * due amounts (including TVA, total fees, discount amount...) and create/update invoice objects. This method is called when : 1. When a user
     * or admin add/remove a system 2. When a user or admin
     * add/remove an actor/profile 3. When a user or admin edit company demographics (cause billing country impacts TVA amount) 4. When an admin
     * save invoice informations (because discount as to be
     * taken in consideration).. It initializes variables before calling getFinancialSummaryStatic method which will calculate amounts
     */
    @Override
    public void calculateFinancialSummaryDTO() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculateFinancialSummaryDTO");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        BigDecimal feeFirstSystem = activatedTestingSession.getFeeFirstSystem();
        BigDecimal feeAdditionalSystem = activatedTestingSession.getFeeAdditionalSystem();
        financialSummary = net.ihe.gazelle.tm.financial.action.FinancialManager
                .getFinancialSummaryWithCalculatedAmountsStatic(feeFirstSystem, feeAdditionalSystem, TestingSession
                                .getSelectedTestingSession().getFeeParticipant(), TestingSession.getSelectedTestingSession(),
                        choosenInstitutionForAdmin, entityManager);
        if (financialSummary != null) {
            financialSummaryForOneSystem = financialSummary.getFinancialSummaryOneSystems();
        } else {
            financialSummaryForOneSystem = null;
        }
    }

    @Override
    public void listTransactionsToSupportForGivenSystemPDF() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTransactionsToSupportForGivenSystemPDF");
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("sessionId", TestingSession.getSelectedTestingSession().getId());
        parameters.put("systemId", selectedSystemInSession.getSystem().getId());
        try {
            ReportExporterManager.exportToPDF("listOfTransactionsToSupportPerSystem",
                    "listOfTransactionsToSupportPerSystem.pdf", parameters);
        } catch (JRException e) {
            ExceptionLogging.logException(e, LOG);
            StatusMessages.instance().add(e.getMessage());
        }
    }

    @Override
    public void setImplementationToDelete(IHEImplementationForSystem iheImplementationForSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setImplementationToDelete");
        }
        iheImplementationForSystemToDelete = iheImplementationForSystem;
    }

    public IHEImplementationForSystem getIheImplementationForSystemToDelete() {
        return iheImplementationForSystemToDelete;
    }

    @Override
    public void deleteIHEImplementationForSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteIHEImplementationForSystem");
        }
        deleteIHEImplementationForSystem(iheImplementationForSystemToDelete, false);
    }

}
