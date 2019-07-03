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

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.systems.model.Demonstration;
import net.ihe.gazelle.tm.systems.model.DemonstrationQuery;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <b>Class Description : </b>DemonstrationManager<br>
 * <br>
 * This class manage demonstration objects for a system. It corresponds to the Business Layer. A system may registered for multiple demonstrations.
 * All operations to implement are done in this class :
 * <li>Add a demonstration for a system</li> <li>Delete a demonstration for a system</li> <li>Show demonstrations for a system</li> <li>Edit
 * demonstration for a system</li> <li>etc...</li>
 *
 * @author Xiaojun Ding (Mallinkrodt Institute Of Radiology - St Louis USA) , JB Meyer, Jean-Renan Chatel (INRIA, Rennes, FR), Anne-Gaelle Bergé
 * @version 1.0 - July 7, 2008
 * @class DemonstrationManager.java
 * @package net.ihe.gazelle.tm.systems.action
 */

@Name("demonstrationManager")
@Scope(ScopeType.PAGE)
public class DemonstrationManager implements Serializable, QueryModifier<Demonstration> {

    private static final long serialVersionUID = 11129891320883113L;

    private static final Logger LOG = LoggerFactory.getLogger(DemonstrationManager.class);
    // ~ Attributes //

    /**
     * selectedDemonstration object managed my this manager bean and injected in JSF
     */
    private Demonstration selectedDemonstration;
    private Filter<Demonstration> filter;
    private TestingSession selectedTestingSession;

    /**
     * SelectedSystemInSession object managed my this manager bean and injected in JSF
     */
    private SystemInSession selectedSystemInSession;

    private boolean displayList = true;

    private boolean editDemonstration = false;

    private boolean showDemonstration = false;

    private List<TestingSession> availableTestingSessions;
    private List<SystemInSession> systemsInSessionLinkedToDemonstration;

    // Getters and Setters
    public Demonstration getSelectedDemonstration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDemonstration");
        }
        return selectedDemonstration;
    }

    public void setSelectedDemonstration(Demonstration selectedDemonstration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDemonstration");
        }
        this.selectedDemonstration = selectedDemonstration;
    }

    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return selectedSystemInSession;
    }

    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        this.selectedSystemInSession = selectedSystemInSession;
    }

    public boolean isDisplayList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplayList");
        }
        return displayList;
    }

    public void setDisplayList(boolean displayList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayList");
        }
        this.displayList = displayList;
    }

    public boolean isEditDemonstration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isEditDemonstration");
        }
        return editDemonstration;
    }

    public void setEditDemonstration(boolean editDemonstration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditDemonstration");
        }
        this.editDemonstration = editDemonstration;
    }

    public boolean isShowDemonstration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowDemonstration");
        }
        return showDemonstration;
    }

    public void setShowDemonstration(boolean showDemonstration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowDemonstration");
        }
        this.showDemonstration = showDemonstration;
    }

    public List<TestingSession> getAvailableTestingSessions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAvailableTestingSessions");
        }
        return availableTestingSessions;
    }

    public void setAvailableTestingSessions(List<TestingSession> availableTestingSessions) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAvailableTestingSessions");
        }
        this.availableTestingSessions = availableTestingSessions;
    }

    public List<SystemInSession> getSystemsInSessionLinkedToDemonstration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsInSessionLinkedToDemonstration");
        }
        return systemsInSessionLinkedToDemonstration;
    }

    public void setSystemsInSessionLinkedToDemonstration(List<SystemInSession> systemsInSessionLinkedToDemonstration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemsInSessionLinkedToDemonstration");
        }
        this.systemsInSessionLinkedToDemonstration = systemsInSessionLinkedToDemonstration;
    }

    /**
     * Find all demonstrations existing in the database
     */

    public FilterDataModel<Demonstration> findAllDemonstrations(boolean allSessions) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findAllDemonstrations");
        }
        if (!allSessions) {
            selectedTestingSession = TestingSession.getSelectedTestingSession();
        } else {
            selectedTestingSession = null;
        }
        return new FilterDataModel<Demonstration>(getFilter()) {
            @Override
            protected Object getId(Demonstration demonstration) {
                return demonstration.getId();
            }
        };
    }

    public Filter<Demonstration> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            filter = new Filter<Demonstration>(getHQLCriterionForFilter());
        }
        return filter;
    }

    private HQLCriterionsForFilter<Demonstration> getHQLCriterionForFilter() {
        DemonstrationQuery query = new DemonstrationQuery();
        HQLCriterionsForFilter<Demonstration> criterion = query.getHQLCriterionsForFilter();
        criterion.addQueryModifier(this);
        return criterion;
    }

    public void editSelectedDemonstration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editSelectedDemonstration");
        }
        displayList = false;
        editDemonstration = true;
        showDemonstration = false;
        if ((selectedDemonstration != null) && (selectedDemonstration.getId() != null)) {
            selectedDemonstration = Demonstration.getDemonstrationById(selectedDemonstration.getId());
        }
        availableTestingSessions = TestingSession.GetAllSessions();
    }

    public void displayAllDemonstrations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayAllDemonstrations");
        }
        displayList = true;
        editDemonstration = false;
        showDemonstration = false;
        selectedDemonstration = null;
    }

    public void showSelectedDemonstration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showSelectedDemonstration");
        }
        displayList = false;
        editDemonstration = false;
        showDemonstration = true;
        selectedDemonstration = Demonstration.getDemonstrationById(selectedDemonstration.getId());
        systemsInSessionLinkedToDemonstration = selectedDemonstration.getSystemsInSession();
    }

    /**
     * Add a new demonstration action (from the demo management list or from the administration menu bar) This operation is allowed for some
     * granted users (check the security.drl) (This does not add a
     * button, but rather adds a demonstration !)
     *
     * @return String : JSF page to render
     */
    public void addNewDemonstration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewDemonstration");
        }

        selectedDemonstration = new Demonstration();

        editSelectedDemonstration();
    }

    public void addDemonstration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addDemonstration");
        }
        try {
            if (selectedDemonstration == null) {
                return;
            }
            // We check that the name is entered
            if ((selectedDemonstration.getName() == null) || (selectedDemonstration.getName().equals(""))) {
                LOG.warn("Please enter a demonstration name...");
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("name", StatusMessage.Severity.WARN,
                        "gazelle.validator.systems.demonstration.notNull", "Please enter a demonstration name...");
                return;
            }
            if (!isValidDemonstrationName(selectedDemonstration)) {
                LOG.warn("Please enter ANOTHER demonstration name, this one already exists...");
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("name", StatusMessage.Severity.WARN,
                        "gazelle.validator.systems.demonstration.alreadyExists",
                        "Please enter another demonstration name, this one already exists...");
                return;
            }

            // We chech that the date of beginning if before the date of end
            if (selectedDemonstration.getBeginningDemonstration() != null) {
                if (selectedDemonstration.getEndingDemonstration() != null) {
                    if (selectedDemonstration.getBeginningDemonstration().after(
                            selectedDemonstration.getEndingDemonstration())) {
                        LOG.warn("Beginning Date Should Be Before Ending Date !!!!");
                        FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "gazelle.systems.demonstration.faces.BeginningDateShouldBeBeforeEndingDate");
                        return;
                    }
                } else {
                    LOG.warn("No date of ending for this demonstration...  this is not a validation error but it could be");
                }
            } else if (selectedDemonstration.getEndingDemonstration() != null) {
                LOG.warn("No date of beginning, but a date of ending for this demonstration...  this is a validation error");
                FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "gazelle.systems.demonstration.faces.PleaseFillBeginningDateOrRemoveEndingDate");
                return;
            }

            EntityManager entityManager = EntityManagerService.provideEntityManager();

            if (selectedDemonstration.getId() == null) {
                // We persist the demonstration if it's a creation
                selectedDemonstration = entityManager.merge(selectedDemonstration);
                FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                        "gazelle.systems.demonstration.faces.DemonstrationSuccessfullyCreated",
                        selectedDemonstration.getName());
            } else {
                // We merge the updated object in database (edit mode)
                selectedDemonstration = entityManager.merge(selectedDemonstration);
                FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                        "gazelle.systems.demonstration.faces.DemonstrationSuccessfullyUpdated",
                        selectedDemonstration.getName());
            }

            entityManager.flush();

            displayAllDemonstrations();

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error : " + e.getMessage());
            return;
        }
    }

    /**
     * Prepare objects before editing demonstration's informations This operation is allowed for some granted users (check the security.drl)
     */
    public void editSelectedDemonstration(Demonstration inDemonstration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editSelectedDemonstration");
        }
        if (inDemonstration != null) {
            selectedDemonstration = inDemonstration;
            editSelectedDemonstration();
        } else {
            return;
        }

    }

    /**
     * Prepare objects before showing demonstration's informations
     */
    public void showSelectedDemonstration(Demonstration inDemonstration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showSelectedDemonstration");
        }
        if (inDemonstration != null) {
            selectedDemonstration = inDemonstration;
            showSelectedDemonstration();
        } else {
            return;
        }

    }

    /**
     * This method is called to delete the selected demonstration
     */
    public void deleteSelectedDemonstration(Demonstration inDemonstration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedDemonstration");
        }

        selectedDemonstration = inDemonstration;
    }

    public void confirmDemonstrationDeletion() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("confirmDemonstrationDeletion");
        }
        if (selectedDemonstration != null) {

            Demonstration.deleteDemonstrationWithFind(selectedDemonstration,
                    EntityManagerService.provideEntityManager());
            selectedDemonstration = null;
        } else {
            LOG.error("method deleteSelectedDemonstration() : selectedDemonstration passed as parameter is null ! ");
        }
    }

    /**
     * We validate the demonstration name/
     *
     * @param Demonstration : demonstration to validate
     * @return Boolean : True if the demonstration name is correst (not already existing)
     */
    public Boolean isValidDemonstrationName(Demonstration demo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isValidDemonstrationName");
        }
        Demonstration d = Demonstration.getDemonstrationByName(demo.getName());

        if (d == null) {

            return true;
        } else {
            if (demo.getId() != null) {
                if (demo.getId().equals(d.getId())) {

                    return true;
                } else {

                    return false;
                }
            } else {

                return false;
            }

        }
    }

    public void showSelectedDemonstrationForUser(Demonstration inDemonstration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showSelectedDemonstrationForUser");
        }
        selectedDemonstration = inDemonstration;
    }

    /**
     * Destroy the Manager bean when the session is over.
     */
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public void modifyQuery(HQLQueryBuilder<Demonstration> hqlQueryBuilder, Map<String, Object> map) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        if (selectedTestingSession != null) {
            DemonstrationQuery query = new DemonstrationQuery();
            hqlQueryBuilder.addRestriction(query.testingSessions().id().eqRestriction(selectedTestingSession.getId()));
        }
    }
}
