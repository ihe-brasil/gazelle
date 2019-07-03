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

import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.persistence.EntityManager;
import java.io.Serializable;

@BypassInterceptors
@Name("TFConverter")
@Converter
public class TFConverter implements javax.faces.convert.Converter, Serializable {

    private static final long serialVersionUID = 0L;
    private static final Logger LOG = LoggerFactory.getLogger(TFConverter.class);

    /**
     * getAsString - multi-class converter for MasterModel
     *
     * @param facesContext
     * @param uiComponent
     * @param value        object to convert to string
     * @return string of format "ClassType:Id"
     */

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAsString");
        }
        if (value instanceof IntegrationProfile) {
            IntegrationProfile v = (IntegrationProfile) value;
            String r = CType.INTEGRATIONPROFILE.toString() + ":" + v.getId().toString();
            return r;
        }
        if (value instanceof IntegrationProfileOption) {
            IntegrationProfileOption v = (IntegrationProfileOption) value;
            String r = CType.INTEGRATIONPROFILEOPTION.toString() + ":" + v.getId().toString();
            return r;
        }
        if (value instanceof Actor) {
            Actor v = (Actor) value;
            String r = CType.ACTOR.toString() + ":" + v.getId().toString();
            return r;
        }

        return null;
    }

    /**
     * getAsObject - multi-class converter for master model
     *
     * @param Context
     * @param Component
     * @param value     string of format "ClassType:Id"
     * @return Object represented by value string
     * @throws ConverterException
     */

    @Override
    @Transactional
    public Object getAsObject(FacesContext Context, UIComponent Component, String value) throws ConverterException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAsObject");
        }
        CType t;
        int id;
        Object obj;
        if (value == null) {
            return null;
        }
        // Parse string for CType and Id values, return null if error
        int i = value.indexOf(':');
        if (i < 1) {
            return null;
        }
        try {
            t = CType.valueOf(value.substring(0, i));
            id = Integer.parseInt(value.substring(i + 1));
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            return null;
        }
        // Now we reload object represented by string
        EntityManager em = EntityManagerService.provideEntityManager();
        switch (t) {
            case INTEGRATIONPROFILE:
                obj = em.find(IntegrationProfile.class, id);
                break;
            case INTEGRATIONPROFILEOPTION:
                obj = em.find(IntegrationProfileOption.class, id);
                break;
            case ACTOR:
                obj = em.find(Actor.class, id);
                break;
            default:
                obj = null;

        }
        if (obj == null) {

        }
        return obj;
    }

    // ----------- enum shows types of classes converted
    private enum CType {
        INTEGRATIONPROFILE,
        INTEGRATIONPROFILEOPTION,
        ACTOR
    }

}
