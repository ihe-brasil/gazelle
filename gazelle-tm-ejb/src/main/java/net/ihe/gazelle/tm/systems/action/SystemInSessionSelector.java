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
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

import static org.jboss.seam.ScopeType.SESSION;


@Name("systemInSessionSelector")
@Scope(SESSION)
@GenerateInterface("SystemInSessionSelectorLocal")
public class SystemInSessionSelector implements Serializable, SystemInSessionSelectorLocal {

    private static final long serialVersionUID = 123556212424560L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SystemInSessionSelector.class);
    @In(required = false)
    @Out(required = false)
    private SystemInSession selectedSystemInSession;
    /**
     * ActivatedTestingSession object managed my this manager bean
     */
    @In(required = false)
    @Out(required = false)
    private TestingSession activatedTestingSession;
    @In(required = false)
    @Out(required = false)
    private Institution choosenInstitutionForAdmin;

    @Override
    public void cancelCreation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelCreation");
        }

    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionSelector', 'getPossibleSystemsInSession', null)}")
    public List<SystemInSession> getPossibleSystemsInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleSystemsInSession");
        }

        activatedTestingSession = TestingSession.getSelectedTestingSession();

        EntityManager em = EntityManagerService.provideEntityManager();
        User u = User.loggedInUser();
        if (Role.isLoggedUserAdmin()) {
            if ((choosenInstitutionForAdmin == null) || (choosenInstitutionForAdmin.getName() == null)) {
                return null;
            } else {

                Institution tmp = Institution.findInstitutionWithName(choosenInstitutionForAdmin.getName());
                if (tmp != null) {
                    choosenInstitutionForAdmin = tmp;
                    return SystemInSession.getAcceptedSystemsInSessionForCompanyForSession(em,
                            choosenInstitutionForAdmin, activatedTestingSession);
                } else {
                    return null;
                }

            }
        } else if (Role.isLoggedUserVendorAdmin() || Role.isLoggedUserVendorUser()) {

            return SystemInSession.getAcceptedSystemsInSessionForCompanyForSession(em, u.getInstitution(),
                    activatedTestingSession);
        } else {
            return null;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionSelector', 'getPossibleSystemsInSession', null)}")
    public List<SystemInSession> getPossibleSystemsInSession(Institution inInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleSystemsInSession");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        activatedTestingSession = TestingSession.getSelectedTestingSession();
        User u = User.loggedInUser();
        if (Role.isLoggedUserAdmin()) {
            if ((inInstitution == null) || (inInstitution.getName() == null)) {
                return null;
            } else {

                Institution tmp = Institution.findInstitutionWithName(inInstitution.getName());
                if (tmp != null) {
                    choosenInstitutionForAdmin = tmp;

                    return SystemInSession.getAcceptedSystemsInSessionForCompanyForSession(em, inInstitution,
                            activatedTestingSession);

                } else {
                    return null;
                }

            }
        } else if (Role.isLoggedUserVendorAdmin() || Role.isLoggedUserVendorUser()) {

            return SystemInSession.getAcceptedSystemsInSessionForCompanyForSession(em, u.getInstitution(),
                    activatedTestingSession);
        } else {
            return null;
        }
    }

    @Override
    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return selectedSystemInSession;
    }

    @Override
    public void setSelectedSystemInSession(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }

        if (systemInSession != null) {
            selectedSystemInSession = systemInSession;
        } else {
            selectedSystemInSession = null;
        }
    }

    @Override
    public void resetSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetSelectedSystemInSession");
        }

        setSelectedSystemInSession(null);

    }

    @Override
    public boolean displayConfig() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayConfig");
        }
        return (selectedSystemInSession != null);
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

        if (selectedSystemInSession != null) {

        }

    }

}