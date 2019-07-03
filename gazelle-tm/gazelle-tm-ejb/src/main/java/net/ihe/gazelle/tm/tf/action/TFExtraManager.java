/*
 * Copyright 2010 IHE International (http://www.ihe.net)
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

package net.ihe.gazelle.tm.tf.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.action.IntegrationProfileManagerLocal;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.configurations.model.ConfigurationTypeMappedWithAIPO;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

/**
 * @author Jean-Rena Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2010, sep
 * @class TFExtraManager.java
 * @package net.ihe.gazelle.tm.systems.model
 * @see > jchatel@irisa.fr - http://www.ihe-europe.org
 */


@Name("tfExtraManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("TFExtraManagerLocal")
public class TFExtraManager implements TFExtraManagerLocal, Serializable {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -450988889991183760L;

    private static final Logger LOG = LoggerFactory.getLogger(TFExtraManager.class);

    /**
     * delaipo - deletes aipo link @ aipo aipo to delete @ obj - object being edited There is no delete effects display for this action.
     *
     * @return - returns to calling screen.
     */
    @Override
    public String deleteAIPO(Object obj, ActorIntegrationProfileOption aipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAIPO");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();

        // Before deleting AIPO, we need to delete ConfigurationMappedWithAipo objects
        List<ConfigurationTypeMappedWithAIPO> listOfCTMappedWithAIPOConfigurationTypeMappedWithAIPO = ConfigurationTypeMappedWithAIPO
                .getConfigurationTypeMappingWithAIPOFiltered(aipo, null, null);

        if (listOfCTMappedWithAIPOConfigurationTypeMappedWithAIPO != null) {

            for (ConfigurationTypeMappedWithAIPO ct : listOfCTMappedWithAIPOConfigurationTypeMappedWithAIPO) {
                entityManager.remove(ct);
                entityManager.flush();
            }
        }

        String results = null;

        return results;
    }

    @Override
    public String deleteActorIntegrationProfileProfileOptionForIP(ActorIntegrationProfileOption aipo,
                                                                  IntegrationProfile ip) {
        try {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            ActorIntegrationProfileOption p = entityManager.find(ActorIntegrationProfileOption.class, aipo.getId());
            List<ConfigurationTypeMappedWithAIPO> lctm = ConfigurationTypeMappedWithAIPO
                    .getConfigurationTypeMappingWithAIPOFiltered(p, null, null);
            EntityManager em = EntityManagerService.provideEntityManager();
            for (ConfigurationTypeMappedWithAIPO configurationTypeMappedWithAIPO : lctm) {

                boolean doit = true;
                if (configurationTypeMappedWithAIPO.getListOfConfigurationTypes() != null) {
                    if (configurationTypeMappedWithAIPO.getListOfConfigurationTypes().size() > 0) {
                        FacesMessages
                                .instance()
                                .add(StatusMessage.Severity.ERROR, "There are some configuration types related to this actor integration profile " +
                                        "option. Please delete them first.");
                        doit = false;
                    }
                }
                if (doit) {
                    em.remove(configurationTypeMappedWithAIPO);
                    em.flush();
                }
            }
            if(aipo.getIntegrationProfileOption().getKeyword().equals("NONE")){
                FacesMessages
                        .instance()
                        .add(StatusMessage.Severity.ERROR, "Can't remove an actor integration profile option with the \"NONE\" option");
                return null;
            }
            entityManager.remove(p);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Link removed");
            IntegrationProfile ip1 = entityManager.find(IntegrationProfile.class, ip.getId());
            IntegrationProfileManagerLocal im = (IntegrationProfileManagerLocal) Component
                    .getInstance("integrationProfileManager");
            return im.editIntegrationProfile(ip1);
        } catch (Exception e) {
            StatusMessages
                    .instance()
                    .add(StatusMessage.Severity.ERROR, "This object is already mapped with a ConfigurationMappedWithAIPO object. It cannot be " +
                            "deleted because it is used...");
            return null;
        }
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