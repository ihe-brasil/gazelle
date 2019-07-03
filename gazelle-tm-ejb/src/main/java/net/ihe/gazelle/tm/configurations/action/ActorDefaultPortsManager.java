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
package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.configurations.model.ActorDefaultPorts;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

@Name("actorDefaultPortsManager")
@Scope(ScopeType.APPLICATION)
@GenerateInterface("ActorDefaultPortsManagerLocal")
public class ActorDefaultPortsManager implements Serializable, ActorDefaultPortsManagerLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -450984231541183760L;

    private static final Logger LOG = LoggerFactory.getLogger(ActorDefaultPortsManager.class);

    private static Hashtable<String, List<ActorDefaultPorts>> hl7DefaultPorts = null;
    private static Hashtable<String, List<ActorDefaultPorts>> hl7V3DefaultPorts = null;
    private static Hashtable<String, List<ActorDefaultPorts>> dicomSCUDefaultPorts = null;
    private static Hashtable<String, List<ActorDefaultPorts>> dicomSCPDefaultPorts = null;
    private static Hashtable<String, List<ActorDefaultPorts>> webServicesDefaultPorts = null;

    public static Hashtable<String, List<ActorDefaultPorts>> getDefautPorts(String type) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDefautPorts");
        }

        EntityManager em = EntityManagerService.provideEntityManager();
        Hashtable<String, List<ActorDefaultPorts>> hashDefautPorts = new Hashtable<String, List<ActorDefaultPorts>>();

        Session s = (Session) em.getDelegate();
        Criteria c = s.createCriteria(ActorDefaultPorts.class);
        c.add(Restrictions.eq("typeOfConfig", type));
        List<ActorDefaultPorts> list = c.list();

        for (int i = 0; i < list.size(); i++) {
            String actorKeyword = list.get(i).getActor().getKeyword();
            if (hashDefautPorts.get(actorKeyword) == null) {
                LinkedList<ActorDefaultPorts> listActors = new LinkedList<ActorDefaultPorts>();
                listActors.add(list.get(i));
                hashDefautPorts.put(actorKeyword, listActors);
            } else {
                hashDefautPorts.get(actorKeyword).add(list.get(i));
            }
        }

        return hashDefautPorts;
    }

    public static Hashtable<String, List<ActorDefaultPorts>> getHL7DefaultPorts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7DefaultPorts");
        }
        if (hl7DefaultPorts == null) {
            hl7DefaultPorts = getDefautPorts(ActorDefaultPorts.TYPE_CONFIG_HL7);
        }
        return (Hashtable<String, List<ActorDefaultPorts>>) hl7DefaultPorts.clone();
    }

    public static List<ActorDefaultPorts> getHL7DefaultPortsFor(String actorKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7DefaultPortsFor");
        }

        if (hl7DefaultPorts == null) {
            getHL7DefaultPorts();
        }
        return hl7DefaultPorts.get(actorKeyword);
    }

    public static Hashtable<String, List<ActorDefaultPorts>> getHL7V3DefaultPorts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7V3DefaultPorts");
        }
        if (hl7DefaultPorts == null) {
            hl7DefaultPorts = getDefautPorts(ActorDefaultPorts.TYPE_CONFIG_HL7_V3);
        }
        return (Hashtable<String, List<ActorDefaultPorts>>) hl7DefaultPorts.clone();
    }

    public static List<ActorDefaultPorts> getHL7V3DefaultPortsFor(String actorKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7V3DefaultPortsFor");
        }

        if (hl7DefaultPorts == null) {
            getHL7DefaultPorts();
        }
        return hl7DefaultPorts.get(actorKeyword);
    }

    public static Hashtable<String, List<ActorDefaultPorts>> getDicomSCUDefaultPorts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDicomSCUDefaultPorts");
        }
        if (dicomSCUDefaultPorts == null) {
            dicomSCUDefaultPorts = getDefautPorts(ActorDefaultPorts.TYPE_CONFIG_DICOM_SCU);
        }
        return (Hashtable<String, List<ActorDefaultPorts>>) dicomSCUDefaultPorts.clone();

    }

    public static List<ActorDefaultPorts> getDicomSCUDefaultPortsFor(String actorKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDicomSCUDefaultPortsFor");
        }
        if (dicomSCUDefaultPorts == null) {
            getDicomSCUDefaultPorts();
        }
        return dicomSCUDefaultPorts.get(actorKeyword);
    }

    public static Hashtable<String, List<ActorDefaultPorts>> getDicomSCPDefaultPorts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDicomSCPDefaultPorts");
        }
        if (dicomSCPDefaultPorts == null) {
            dicomSCPDefaultPorts = getDefautPorts(ActorDefaultPorts.TYPE_CONFIG_DICOM_SCP);
        }
        return (Hashtable<String, List<ActorDefaultPorts>>) dicomSCPDefaultPorts.clone();
    }

    public static List<ActorDefaultPorts> getDicomSCPDefaultPortsFor(String actorKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDicomSCPDefaultPortsFor");
        }
        if (dicomSCPDefaultPorts == null) {
            getDicomSCPDefaultPorts();
        }
        return dicomSCPDefaultPorts.get(actorKeyword);
    }

    public static Hashtable<String, List<ActorDefaultPorts>> getWebServicesDefaultPorts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWebServicesDefaultPorts");
        }
        if (webServicesDefaultPorts == null) {
            webServicesDefaultPorts = getDefautPorts(ActorDefaultPorts.TYPE_CONFIG_WEBSERVICE);
        }
        return (Hashtable<String, List<ActorDefaultPorts>>) webServicesDefaultPorts.clone();
    }

    public static List<ActorDefaultPorts> getWebServicesDefaultFor(String actorKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWebServicesDefaultFor");
        }
        if (webServicesDefaultPorts == null) {
            getWebServicesDefaultPorts();
        }
        return webServicesDefaultPorts.get(actorKeyword);
    }

    // @Destroy
    @Override
    @Remove
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}