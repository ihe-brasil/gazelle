/*
 * Copyright 2009 IHE International (http://www.ihe.net)
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

package net.ihe.gazelle.objects.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.objects.model.ObjectInstance;
import net.ihe.gazelle.objects.model.ObjectInstanceAnnotation;
import net.ihe.gazelle.objects.model.ObjectInstanceAnnotationQuery;
import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * @author Abderrazek Boufahja > INRIA Rennes IHE development Project
 */
@Name("annotationManager")
@Scope(ScopeType.PAGE)
public class AnnotationManager implements Serializable {

    private static final long serialVersionUID = -8734567233751607120L;

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationManager.class);

    // ~ Statics variables and Class initializer ////////////////////////////////////////

    // ~ Attribute ///////////////////////////////////////////////////////////////////////

    private ObjectInstanceAnnotation selectedObjectInstanceAnnotation;
    private Filter<ObjectInstanceAnnotation> filter;

    // ~ getters and setters /////////////////////////////////////////////////////////////

    public ObjectInstanceAnnotation getSelectedObjectInstanceAnnotation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectInstanceAnnotation");
        }
        return selectedObjectInstanceAnnotation;
    }

    public void setSelectedObjectInstanceAnnotation(ObjectInstanceAnnotation selectedObjectInstanceAnnotation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectInstanceAnnotation");
        }
        this.selectedObjectInstanceAnnotation = selectedObjectInstanceAnnotation;
    }

    // methods /////////////////////////////////////////////////////////////////////////

    @Restrict("#{s:hasPermission('AnnotationManager', 'getListObjectInstanceAnnotation', null)}")
    public FilterDataModel<ObjectInstanceAnnotation> getListObjectInstanceAnnotation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListObjectInstanceAnnotation");
        }
        return new FilterDataModel<ObjectInstanceAnnotation>(getFilter()) {
            @Override
            protected Object getId(ObjectInstanceAnnotation objectInstanceAnnotation) {
                return objectInstanceAnnotation.getId();
            }
        };
    }

    public Filter<ObjectInstanceAnnotation> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            filter = new Filter(getHQLCriterions());
        }
        return filter;
    }

    private HQLCriterionsForFilter getHQLCriterions() {
        ObjectInstanceAnnotationQuery query = new ObjectInstanceAnnotationQuery();
        return query.getHQLCriterionsForFilter();
    }

    @Restrict("#{s:hasPermission('AnnotationManager', 'deleteSelectedAnnotation', null)}")
    public void deleteSelectedAnnotation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedAnnotation");
        }
        if (this.selectedObjectInstanceAnnotation != null) {
            ObjectInstanceAnnotation.deleteObjectInstanceAnnotation(this.selectedObjectInstanceAnnotation);
        }
    }

    @Restrict("#{s:hasPermission('AnnotationManager', 'getSamplePermanentlink', null)}")
    public String getSamplePermanentlink(ObjectInstance inObjectInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSamplePermanentlink");
        }
        String result = "";
        if (inObjectInstance != null) {
            if (inObjectInstance.getId() != null) {
                result = ApplicationPreferenceManager.instance().getApplicationUrl() + "objects/sample.seam?id="
                        + inObjectInstance.getId().toString();
            }
        }
        return result;
    }

    public void saveModifications() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveModifications");
        }
        if (this.selectedObjectInstanceAnnotation != null) {
            if (selectedObjectInstanceAnnotation.getAnnotation().getValue().length() > 499) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.CommentSampleLong']}");
            } else {
                selectedObjectInstanceAnnotation.getAnnotation().setValue(
                        StringEscapeUtils.escapeHtml(selectedObjectInstanceAnnotation.getAnnotation().getValue()));
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                selectedObjectInstanceAnnotation.setAnnotation(entityManager.merge(selectedObjectInstanceAnnotation
                        .getAnnotation()));
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.yourcommentwassaved']}");
            }
        }
    }
}
