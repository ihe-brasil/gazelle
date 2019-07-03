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
import net.ihe.gazelle.tm.systems.model.Demonstration;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.util.Pair;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Class Description : </b>SystemDemonstrationManager<br>
 * <br>
 * This class manage demonstration objects for a system. It corresponds to the Business Layer. A system may registered for multiple demonstrations.
 * All operations to implement are done in this class :
 * <li>Add a demonstration for a system</li> <li>Delete a demonstration for a system</li> <li>Show demonstrations for a system</li> <li>Edit
 * demonstration for a system</li> <li>etc...</li>
 *
 * @author JB Meyer
 * @version 1.0 - July 7, 2008
 * @class SystemDemonstrationManager.java
 * @package net.ihe.gazelle.tm.systems.action
 */

@Name("systemDemonstrationManager")
@Scope(ScopeType.PAGE)
@GenerateInterface("SystemDemonstrationManagerLocal")
public class SystemDemonstrationManager implements Serializable, SystemDemonstrationManagerLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1L;

    private static final Logger LOG = LoggerFactory.getLogger(SystemDemonstrationManager.class);

    // ~ Attributes //

    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;

    /**
     * List of all demonstration objects to be managed my this manager bean
     */
    private List<Pair<Boolean, Demonstration>> demonstrations;

    /**
     * demonstration object managed my this manager bean and injected in JSF
     */
    private Demonstration selectedDemonstration;

    /**
     * SelectedSystemInSession object managed my this manager bean and injected in JSF
     */
    private SystemInSession selectedSystemInSession;

    // ~ Methods //

    @Create
    @Override
    public void init() {
        final String systemInSessionId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        try {
            Integer id = Integer.parseInt(systemInSessionId);
            selectedSystemInSession = entityManager.find(SystemInSession.class, id);
            initDemoBoxes();
        } catch (NumberFormatException e) {
            LOG.error("failed to find system in session with id = " + systemInSessionId);
        }
    }

    @Override
    public void initDemoBoxes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initDemoBoxes");
        }
        List<Demonstration> availableDemonstrations = Demonstration
                .getActiveDemonstrationsForSession(selectedSystemInSession.getTestingSession());
        if (availableDemonstrations != null) {
            List<Demonstration> demoImplementedBySystem = selectedSystemInSession.getDemonstrations();
            demonstrations = new ArrayList<Pair<Boolean, Demonstration>>();

            if ((demoImplementedBySystem != null) && !demoImplementedBySystem.isEmpty()) {
                for (Demonstration demo : availableDemonstrations) {
                    demonstrations.add(new Pair<Boolean, Demonstration>(demoImplementedBySystem.contains(demo), demo));
                }
            } else {
                for (int index = 0; index < availableDemonstrations.size(); index++) {
                    if (availableDemonstrations.get(index) instanceof Demonstration) {
                        demonstrations.add(new Pair<Boolean, Demonstration>(false, availableDemonstrations.get(index)));
                    }
                }
            }

        } else {
            demonstrations = null;
        }
    }

    @Override
    public List<Pair<Boolean, Demonstration>> getDemonstrations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDemonstrations");
        }
        return demonstrations;
    }

    @Override
    public void setDemonstrations(List<Pair<Boolean, Demonstration>> demonstrations) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDemonstrations");
        }
        this.demonstrations = demonstrations;
    }

    /**
     * This method is called to update the selected/unselected demonstration for the system
     *
     * @param systemInSession : System in Session to be set/unset
     */
    @Override
    public void updateDemonstrations(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateDemonstrations");
        }
        List<Demonstration> demonstrationsToAdd = new ArrayList<Demonstration>();

        for (int iDemo = 0; iDemo < demonstrations.size(); iDemo++) {
            if (demonstrations.get(iDemo).getObject1()) {
                demonstrationsToAdd.add(demonstrations.get(iDemo).getObject2());
            }
        }
        systemInSession.setDemonstrations(demonstrationsToAdd);
        entityManager.merge(systemInSession);
        entityManager.flush();

        FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.systems.system.faces.SystemSuccessfullyUpdated",
                systemInSession.getSystem().getName());
    }

    @Override
    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        selectedSystemInSession = (SystemInSession) Component.getInstance("selectedSystemInSession");
        return selectedSystemInSession;
    }

    @Override
    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        this.selectedSystemInSession = selectedSystemInSession;
    }

    @Override
    public Demonstration getSelectedDemonstration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDemonstration");
        }
        return selectedDemonstration;
    }

    @Override
    public void setSelectedDemonstration(Demonstration selectedDemonstration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDemonstration");
        }
        this.selectedDemonstration = selectedDemonstration;
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
}
