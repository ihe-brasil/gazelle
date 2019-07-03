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
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.utils.systems.IHEImplementationForSystem;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * <b>Class Description : </b>TestingTypeManager<br>
 * <br>
 * This class manage the TestingType object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add a
 * testingType</li> <li>Edit a testingType</li> <li>
 * Delete a testingType</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, July 10
 * @class TestingTypeManager.java
 * @package net.ihe.gazelle.tm.systems.action
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */

@Scope(ScopeType.PAGE)
@Name("testingTypeManager")
@GenerateInterface("TestingTypeManagerLocal")
public class TestingTypeManager implements Serializable, TestingTypeManagerLocal {
    private static final long serialVersionUID = -450911336361283760L;
    private static final Logger LOG = LoggerFactory.getLogger(TestingTypeManager.class);

    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;

    /**
     * SelectedSystem object managed my this manager bean and injected in JSF
     */
    private SystemInSession selectedSystemInSession;

    /**
     * TestingType objects - List of possible values managed my this manager bean and injected in JSF
     */
    private List<TestingType> possibleTestingTypes;

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
     * Set and persist the selected testingType after a user action (click on TestingType listBox) This operation is allowed for some granted
     * testingTypes (check the security.drl)
     */
    @Override
    public void setTestingTypeFromListbox(final IHEImplementationForSystem iheImplementation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingTypeFromListbox");
        }
        setTestingType(iheImplementation, false);
    }

    @Override
    public void setTestingTypeFromListbox(final IHEImplementationForSystem iheImplementation, SimulatorInSession simulatorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingTypeFromListbox");
        }
        setTestingType(iheImplementation, false, simulatorInSession);
    }

    @Override
    public void setWantedTestingTypeFromListbox(final IHEImplementationForSystem iheImplementation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setWantedTestingTypeFromListbox");
        }
        setTestingType(iheImplementation, true);
    }

    @Override
    public void setTestingTypeFromListboxSAP(SystemActorProfiles systemActorProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingTypeFromListboxSAP");
        }
        changeTestingType(false, systemActorProfile.getSystem(), systemActorProfile.getActorIntegrationProfileOption()
                .getActorIntegrationProfile().getIntegrationProfile(), systemActorProfile
                .getActorIntegrationProfileOption().getActorIntegrationProfile().getActor(), systemActorProfile
                .getActorIntegrationProfileOption().getIntegrationProfileOption(), systemActorProfile.getTestingType());
    }

    @Override
    public void setTestingType(final IHEImplementationForSystem iheImplementation, boolean wanted, SimulatorInSession simulatorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingType");
        }
        System system = null;
        if (selectedSystemInSession != null) {
            system = selectedSystemInSession.getSystem();
        }
        if (system == null) {
            system = simulatorInSession.getSystem();
        }
        if (iheImplementation != null) {
            IntegrationProfile integrationProfile = iheImplementation.getIntegrationProfile();
            Actor actor = iheImplementation.getActor();
            IntegrationProfileOption integrationProfileOption = iheImplementation.getIntegrationProfileOption();

            TestingType testingTypeToPersist;
            if (wanted) {
                testingTypeToPersist = iheImplementation.getWantedTestingType();
            } else {
                testingTypeToPersist = iheImplementation.getTestingType();
            }

            if (testingTypeToPersist != null) {
                testingTypeToPersist = entityManager.find(TestingType.class, testingTypeToPersist.getId());
            }
            changeTestingType(wanted, system, integrationProfile, actor, integrationProfileOption, testingTypeToPersist);
        }
    }

    @Override
    public void setTestingType(final IHEImplementationForSystem iheImplementation, boolean wanted) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingType");
        }
        if (iheImplementation != null) {
            System system = selectedSystemInSession.getSystem();
            IntegrationProfile integrationProfile = iheImplementation.getIntegrationProfile();
            Actor actor = iheImplementation.getActor();
            IntegrationProfileOption integrationProfileOption = iheImplementation.getIntegrationProfileOption();

            TestingType testingTypeToPersist;
            if (wanted) {
                testingTypeToPersist = iheImplementation.getWantedTestingType();
            } else {
                testingTypeToPersist = iheImplementation.getTestingType();
            }
            if (testingTypeToPersist != null) {
                testingTypeToPersist = entityManager.find(TestingType.class, testingTypeToPersist.getId());
            }
            changeTestingType(wanted, system, integrationProfile, actor, integrationProfileOption, testingTypeToPersist);
        }
    }

    @Override
    public void changeTestingType(boolean wanted, System system, IntegrationProfile integrationProfile, Actor actor,
                                  IntegrationProfileOption integrationProfileOption, TestingType testingTypeToPersist) {
        SystemActorProfilesQuery query = new SystemActorProfilesQuery();
        query.system().id().eq(system.getId());
        query.actorIntegrationProfileOption().actorIntegrationProfile().integrationProfile().id().eq(integrationProfile.getId());
        query.actorIntegrationProfileOption().actorIntegrationProfile().actor().id().eq(actor.getId());
        query.actorIntegrationProfileOption().integrationProfileOption().id().eq(integrationProfileOption.getId());

        List<SystemActorProfiles> listSystemActorProfiles = query.getList();
        if (listSystemActorProfiles.size() >= 1) {
            for (SystemActorProfiles systemActorProfiles : listSystemActorProfiles) {
                if (wanted) {
                    systemActorProfiles.setWantedTestingType(testingTypeToPersist);
                } else {
                    systemActorProfiles.setTestingType(testingTypeToPersist);
                    if (testingTypeToPersist == null) {
                        systemActorProfiles.setTestingTypeReviewed(false);
                    } else {
                        systemActorProfiles.setTestingTypeReviewed(true);
                    }
                }
                entityManager.merge(systemActorProfiles);
            }
        } else {
            StatusMessages.instance().add("Internal error : Cannot persist system type");
            LOG.error("No SystemActorProfiles to upadte...");
        }
    }

    /**
     * Find all testingTypes existing in the database
     *
     * @return List<TestingType> : list of the possible TestingType
     */
    @Override
    public List<TestingType> getPossibleTestingTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestingTypes");
        }
        possibleTestingTypes = entityManager.createQuery("from TestingType row").getResultList();
        return possibleTestingTypes;
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
    public boolean canSetTestingType(IHEImplementationForSystem iheImplementationForSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canSetTestingType");
        }
        if (iheImplementationForSystem == null) {
            return false;
        }
        if (!Role.isLoggedUserAdmin()) {
            return false;
        }
        ActorIntegrationProfileOption aipo = getAIPO(iheImplementationForSystem);
        if ((aipo != null) && aipo.getMaybeSupportive()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean couldSetWantedTestingType(IHEImplementationForSystem iheImplementationForSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("couldSetWantedTestingType");
        }
        if (iheImplementationForSystem == null) {
            return false;
        }
        ActorIntegrationProfileOption aipo = getAIPO(iheImplementationForSystem);
        if ((aipo != null) && aipo.getMaybeSupportive()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canSetWantedTestingType(IHEImplementationForSystem iheImplementationForSystem,
                                           net.ihe.gazelle.tm.systems.model.System system) {
        if ((iheImplementationForSystem == null) || (system == null)) {
            return false;
        }
        if (Role.isLoggedUserAdmin()) {
            return true;
        }
        Institution institution = User.loggedInUser().getInstitution();
        Set<InstitutionSystem> institutionSystems = system.getInstitutionSystems();
        boolean found = false;
        if (institutionSystems != null) {
            for (InstitutionSystem institutionSystem : institutionSystems) {
                if (institutionSystem.getInstitution().getId().equals(institution.getId())) {
                    found = true;
                }
            }
        }
        if (!found) {
            return false;
        }
        ActorIntegrationProfileOption uniqueResult = getAIPO(iheImplementationForSystem);
        if ((uniqueResult != null) && uniqueResult.getMaybeSupportive()) {
            SystemActorProfiles sap = getSystemActorProfiles(uniqueResult, system);
            if (sap != null) {
                if ((sap.getTestingTypeReviewed() == null) || !sap.getTestingTypeReviewed()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public SystemActorProfiles getSystemActorProfiles(IHEImplementationForSystem iheImplementationForSystem,
                                                      net.ihe.gazelle.tm.systems.model.System system) {
        return getSystemActorProfiles(getAIPO(iheImplementationForSystem), system);
    }

    @Override
    public SystemActorProfiles getSystemActorProfiles(ActorIntegrationProfileOption aipo,
                                                      net.ihe.gazelle.tm.systems.model.System system) {
        if (aipo == null) {
            return null;
        }
        SystemActorProfilesQuery sapQuery = new SystemActorProfilesQuery();
        sapQuery.actorIntegrationProfileOption().eq(aipo);
        sapQuery.system().eq(system);
        SystemActorProfiles sap = sapQuery.getUniqueResult();
        return sap;
    }

    @Override
    public ActorIntegrationProfileOption getAIPO(IHEImplementationForSystem iheImplementationForSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAIPO");
        }
        if (iheImplementationForSystem == null) {
            return null;
        }
        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
        query.actorIntegrationProfile().actor().eq(iheImplementationForSystem.getActor());
        query.actorIntegrationProfile().integrationProfile().eq(iheImplementationForSystem.getIntegrationProfile());
        query.integrationProfileOption().eq(iheImplementationForSystem.getIntegrationProfileOption());
        ActorIntegrationProfileOption uniqueResult = query.getUniqueResult();
        return uniqueResult;
    }

    @Override
    public void setTestingTypeReviewed(IHEImplementationForSystem iheImplementationForSystem,
                                       net.ihe.gazelle.tm.systems.model.System system) {
        SystemActorProfiles sap = getSystemActorProfiles(iheImplementationForSystem, system);
        if (sap != null) {
            sap.setTestingTypeReviewed(iheImplementationForSystem.isTestingTypeReviewed());
            setTestingTypeReviewedSAP(sap);
        }
    }

    @Override
    public void setTestingTypeReviewedSAP(SystemActorProfiles systemActorProfiles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingTypeReviewedSAP");
        }
        EntityManagerService.provideEntityManager().merge(systemActorProfiles);
        EntityManagerService.provideEntityManager().flush();
    }

    @Override
    public void initWantedTestingType(SystemInSession sis, IHEImplementationForSystem iheImplementationForSystem) {
        selectedSystemInSession = sis;
        initWantedTestingType(iheImplementationForSystem);
    }

    @Override
    public void initWantedTestingType(IHEImplementationForSystem iheImplementationForSystem) {
        System system = selectedSystemInSession.getSystem();
        IntegrationProfile integrationProfile = iheImplementationForSystem.getIntegrationProfile();
        Actor actor = iheImplementationForSystem.getActor();
        IntegrationProfileOption integrationProfileOption = iheImplementationForSystem.getIntegrationProfileOption();

        SystemActorProfilesQuery query = new SystemActorProfilesQuery();
        query.system().id().eq(system.getId());
        query.actorIntegrationProfileOption().actorIntegrationProfile().integrationProfile().id()
                .eq(integrationProfile.getId());
        query.actorIntegrationProfileOption().actorIntegrationProfile().actor().id().eq(actor.getId());

        query.actorIntegrationProfileOption().integrationProfileOption().id().eq(integrationProfileOption.getId());

        List<SystemActorProfiles> listSystemActorProfiles = query.getList();
        for (SystemActorProfiles systemActorProfiles : listSystemActorProfiles) {
            TestingType res = systemActorProfiles.getWantedTestingType();
            if (res != null) {
            } else {
                TestingTypeQuery q = new TestingTypeQuery();
                q.name().eq("Thorough");
                TestingType testingType = q.getUniqueResult();
                if (testingType != null) {
                    systemActorProfiles.setWantedTestingType(testingType);
                    systemActorProfiles = entityManager.merge(systemActorProfiles);
                }
            }
        }
        if (iheImplementationForSystem.getWantedTestingType() == null) {
            TestingTypeQuery q = new TestingTypeQuery();
            q.name().eq("Thorough");
            TestingType testingType = q.getUniqueResult();
            if (testingType != null) {
                iheImplementationForSystem.setWantedTestingType(testingType);
            }
        }
        entityManager.flush();
    }
}
