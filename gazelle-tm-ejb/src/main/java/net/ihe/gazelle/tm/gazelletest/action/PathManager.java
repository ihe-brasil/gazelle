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

package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.definition.ContextualInformation;
import net.ihe.gazelle.tm.gazelletest.model.definition.Path;
import net.ihe.gazelle.tm.gazelletest.model.definition.PathQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

/**
 * @author Abderrazek Boufahja > INRIA Rennes IHE development Project
 */

@Name("pathManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("PathManagerLocal")
public class PathManager implements PathManagerLocal, Serializable {

    private static final long serialVersionUID = -8734567233751607120L;

    // ~ Statics variables and Class initializer ////////////////////////////////////////

    private static final Logger LOG = LoggerFactory.getLogger(PathManager.class);

    // ~ Attribute ///////////////////////////////////////////////////////////////////////

    private Path selectedPath;

    private List<ContextualInformation> listCIRelatedToSelectedPath;

    private Filter<Path> filter;

    // ~ getters and setters /////////////////////////////////////////////////////////////

    @Override
    public List<ContextualInformation> getListCIRelatedToSelectedPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListCIRelatedToSelectedPath");
        }
        return listCIRelatedToSelectedPath;
    }

    @Override
    public void setListCIRelatedToSelectedPath(List<ContextualInformation> listCIRelatedToSelectedPath) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListCIRelatedToSelectedPath");
        }
        this.listCIRelatedToSelectedPath = listCIRelatedToSelectedPath;
    }

    @Override
    public Path getSelectedPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedPath");
        }
        return selectedPath;
    }

    @Override
    public void setSelectedPath(Path selectedPath) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedPath");
        }
        this.selectedPath = selectedPath;
    }

    // methods /////////////////////////////////////////////////////////////////////////

    @Override
    public List<Path> getAllPath() {
        PathQuery q = new PathQuery();
        return q.getList();
    }

    @Override
    public Filter<Path> getFilter() {
        if (filter == null) {
            PathQuery q = new PathQuery();
            filter = new Filter<Path>(q.getHQLCriterionsForFilter());
        }
        return filter;
    }

    @Override
    public FilterDataModel<Path> getListAllPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListAllPath");
        }
        return new FilterDataModel<Path>(getFilter()) {
            @Override
            protected Object getId(Path path) {
                return path.getId();
            }
        };
    }

    @Override
    public void deleteSelectedPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedPath");
        }
        if (this.selectedPath != null) {
            Path.deletePath(this.selectedPath);
        }
    }

    @Override
    public void saveModifications() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveModifications");
        }
        if (this.selectedPath != null) {
            if (selectedPath.getDescription().length() > 499) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Path description is too long.");
            } else {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                this.selectedPath = entityManager.merge(this.selectedPath);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Path was saved.");
            }
        }
    }

    @Override
    public void initSelectedPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initSelectedPath");
        }
        this.selectedPath = new Path();
    }

    @Override
    public void calculateListCIRelatedToSelectedPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculateListCIRelatedToSelectedPath");
        }
        if (selectedPath == null) {
            this.listCIRelatedToSelectedPath = null;
            return;
        }
        this.listCIRelatedToSelectedPath = ContextualInformation.getContextualInformationFiltered(null, selectedPath,
                null);
        if (this.listCIRelatedToSelectedPath != null) {
            if (this.listCIRelatedToSelectedPath.size() == 0) {
                this.listCIRelatedToSelectedPath = null;
            }
        }
    }

    // destroy method ///////////////////////////////////////////////////////////////////

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
