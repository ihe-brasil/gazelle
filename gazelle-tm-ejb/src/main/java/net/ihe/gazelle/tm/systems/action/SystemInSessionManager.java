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
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.async.QuartzDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Class Description : </b>SystemInSessionManager<br>
 * <br>
 *
 * @author Jean-Baptiste Meyer / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 28
 * @class SystemInSessionManager.java
 * @package net.ihe.gazelle.pr.systems.action
 * @see > jmeyer@irisa.fr - http://www.ihe-europe.org
 */

@Name("systemInSessionManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("SystemInSessionManagerLocal")
public class SystemInSessionManager implements Serializable, SystemInSessionManagerLocal {
    // ~ Statics variables and Class initializer
    // ///////////////////////////////////////////////////////////////////////

    private static final String TESTING_SESSION = "testingSession";

    private static final String QUERY2 = "query = ";

    private static final String CHOOSEN_INSTITUTION_FOR_ADMIN = "choosenInstitutionForAdmin";

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionManager.class);

    // ~ Attributes
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;

    /**
     * List of all accepted systems in session, objects to be managed by this manager bean
     */
    private List<SystemInSession> acceptedSystemsInSession;

    /**
     * List of all NOT accepted systems in session, objects to be managed by this manager bean
     */
    private List<SystemInSession> notAcceptedSystemsInSession;

    // ~ Constructors
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public SystemInSessionManager() throws Exception {
        super();

    }

    // ~ Getters and Setters
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<SystemInSession> getAcceptedSystemsInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAcceptedSystemsInSession");
        }
        return acceptedSystemsInSession;
    }

    @Override
    public void setAcceptedSystemsInSession(List<SystemInSession> acceptedSystemsInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAcceptedSystemsInSession");
        }
        this.acceptedSystemsInSession = acceptedSystemsInSession;
    }

    @Override
    public List<SystemInSession> getNotAcceptedSystemsInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNotAcceptedSystemsInSession");
        }
        return notAcceptedSystemsInSession;
    }

    @Override
    public void setNotAcceptedSystemsInSession(List<SystemInSession> notAcceptedSystemsInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNotAcceptedSystemsInSession");
        }
        this.notAcceptedSystemsInSession = notAcceptedSystemsInSession;
    }

    // ~ Methods
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Change and persist boolean value for : ACCEPTED TO SESSION
     */
    @Override
    public void saveAcceptedToSession(SystemInSession sis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveAcceptedToSession");
        }
        if (sis == null) {
            LOG.warn("saveAcceptedToSession : SystemInSession is null ");
            return;
        }

        entityManager.merge(sis);
        entityManager.flush();
        List<Integer> testingSessionid = new ArrayList<Integer>();
        testingSessionid.add(sis.getTestingSession().getId());
        QuartzDispatcher.instance().scheduleAsynchronousEvent("updateConnectathonResultsList", testingSessionid);
    }

    /**
     * Get the list of accepted system for a given session
     *
     * @param TestingSession : concernated testing session
     */
    @Override
    public void getListOfAcceptedSystemsToSession(TestingSession ts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAcceptedSystemsToSession");
        }
        Query query = null;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserMonitor()) {
            if ((((Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN)) != null) && (((Institution) Component.getInstance
                    (CHOOSEN_INSTITUTION_FOR_ADMIN)).getName() != null))

            {

                query = entityManager.createQuery("SELECT distinct sis FROM SystemInSession sis, InstitutionSystem iSyst WHERE " + " sis" +
                        ".acceptedToSession IS TRUE AND "
                        + " sis.testingSession = :testingSession AND " + " sis.system = iSyst.system AND " + " iSyst.institution = :institution ");
                query.setParameter(TESTING_SESSION, ts);
                query.setParameter("institution", Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN));

                acceptedSystemsInSession = query.getResultList();

            } else if ((((Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN)) == null) || (((Institution) Component.getInstance
                    (CHOOSEN_INSTITUTION_FOR_ADMIN)).getName() == null)) {
                query = entityManager.createQuery("SELECT distinct sis FROM SystemInSession sis WHERE sis.acceptedToSession IS TRUE AND sis" +
                        ".testingSession = :testingSession");
                query.setParameter(TESTING_SESSION, ts);

                acceptedSystemsInSession = query.getResultList();

            }
        }

    }

    /**
     * Get the list of accepted system for the activated session
     */
    @Override
    public void getListOfAcceptedSystemsForActivatedSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAcceptedSystemsForActivatedSession");
        }
        getListOfAcceptedSystemsToSession(TestingSession.getSelectedTestingSession());
    }

    /**
     * Get the list of NOT accepted system for a given session
     *
     * @param TestingSession : concernated testing session
     */
    @Override
    public void getListOfNotAcceptedSystemsToSession(TestingSession ts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfNotAcceptedSystemsToSession");
        }
        Query query = null;

        if (Role.isLoggedUserAdmin() || Role.isLoggedUserMonitor()) {
            if ((((Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN)) != null) && (((Institution) Component.getInstance
                    (CHOOSEN_INSTITUTION_FOR_ADMIN)).getName() != null)) {

                query = entityManager.createQuery("SELECT distinct sis FROM SystemInSession sis, InstitutionSystem iSyst WHERE " + " sis" +
                        ".acceptedToSession IS FALSE AND "
                        + " sis.testingSession = :testingSession AND " + " sis.system = iSyst.system AND " + " iSyst.institution = :institution ");
                query.setParameter(TESTING_SESSION, ts);
                query.setParameter("institution", Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN));

                notAcceptedSystemsInSession = query.getResultList();
            } else if ((((Institution) Component.getInstance(CHOOSEN_INSTITUTION_FOR_ADMIN)) == null) || (((Institution) Component.getInstance
                    (CHOOSEN_INSTITUTION_FOR_ADMIN)).getName() == null)) {
                query = entityManager.createQuery("SELECT distinct sis FROM SystemInSession sis WHERE sis.acceptedToSession IS FALSE AND sis" +
                        ".testingSession = :testingSession");
                query.setParameter(TESTING_SESSION, ts);

                notAcceptedSystemsInSession = query.getResultList();

            } else {

            }
        }

    }

    /**
     * Get the list of NOT accepted system for the activated session
     */
    @Override
    public void getListOfNotAcceptedSystemsForActivatedSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfNotAcceptedSystemsForActivatedSession");
        }
        getListOfNotAcceptedSystemsToSession(TestingSession.getSelectedTestingSession());
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
