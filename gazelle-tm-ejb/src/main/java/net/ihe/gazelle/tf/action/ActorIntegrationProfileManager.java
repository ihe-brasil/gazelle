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

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tf.model.ActorIntegrationProfile;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;


@Name("actorIntegrationProfileManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("ActorIntegrationProfileManagerLocal")
public class ActorIntegrationProfileManager implements Serializable, ActorIntegrationProfileManagerLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1357012043456235100L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ActorIntegrationProfileManager.class);
    @In
    private EntityManager entityManager;
    @DataModel
    private List<ActorIntegrationProfile> actorIntegrationProfiles;

    @Override
    @Factory("actorIntegrationProfiles")
    public void findActorIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findActorIntegrationProfiles");
        }

        Query query = entityManager.createQuery("SELECT aip FROM ActorIntegrationProfile aip");
        this.actorIntegrationProfiles = query.getResultList();

    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
