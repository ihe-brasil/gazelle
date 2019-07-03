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
import net.ihe.gazelle.tf.model.Actor;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

import static org.jboss.seam.ScopeType.SESSION;


@Name("actorSelector")
@Scope(SESSION)
@GenerateInterface("ActorSelectorLocal")
public class ActorSelector implements Serializable, ActorSelectorLocal {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ActorSelector.class);
    private static final long serialVersionUID = 123556212424560L;

    @In
    private EntityManager entityManager;

    @In(required = false)
    @Out(required = false)
    private Actor selectedActor;

    @Override
    public void cancelCreation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelCreation");
        }

    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionSelector', 'getPossibleSystemsInSession', null)}")
    public List<Actor> getPossibleActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleActors");
        }
        Session s = (Session) entityManager.getDelegate();
        Criteria c = s.createCriteria(Actor.class);

        c.addOrder(Order.asc("name"));
        return c.list();
    }

    @Override
    public Actor getSelectedActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActor");
        }
        return selectedActor;
    }

    @Override
    public void setSelectedActor(Actor choosenActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActor");
        }
        this.selectedActor = choosenActor;
    }

    @Override
    public void resetSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetSelectedSystemInSession");
        }

        setSelectedActor(null);
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}