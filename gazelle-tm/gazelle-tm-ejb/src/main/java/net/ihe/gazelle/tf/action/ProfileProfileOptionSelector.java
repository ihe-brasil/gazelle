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
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;


@Name("profileProfileOptionSelector")
@GenerateInterface("ProfileProfileOptionSelectorLocal")
public class ProfileProfileOptionSelector implements Serializable, ProfileProfileOptionSelectorLocal {

    private static final long serialVersionUID = 123556212424560L;
    private static final Logger LOG = LoggerFactory.getLogger(ProfileProfileOptionSelector.class);
    @In
    private EntityManager entityManager;
    @In(required = false)
    @Out(required = false)
    private IntegrationProfile selectedIntegrationProfile;
    @In(required = false)
    @Out(required = false)
    private IntegrationProfileOption selectedIntegrationProfileOption;

    @Override
    public void cancelCreation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelCreation");
        }
    }

    @Override
    public List<IntegrationProfile> getPossibleIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfiles");
        }

        if (selectedIntegrationProfile != null) {

        } else {

        }
        if (selectedIntegrationProfileOption != null) {

        } else {

        }

        return (List<IntegrationProfile>) entityManager.createQuery("from IntegrationProfile row").getResultList();
    }

    @Override
    public List<IntegrationProfileOption> getPossibleIntegrationProfileOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfileOptions");
        }
        String query = "from IntegrationProfileOption row";

        if (selectedIntegrationProfile != null) {

        } else {

        }

        if (selectedIntegrationProfileOption != null) {

        } else {

        }

        return (List<IntegrationProfileOption>) entityManager.createQuery(query).getResultList();

    }

    @Override
    public IntegrationProfile getSelectedIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfile");
        }

        if (selectedIntegrationProfile != null) {

        }
        return selectedIntegrationProfile;
    }

    @Override
    public void setSelectedIntegrationProfile(IntegrationProfile integrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfile");
        }
        selectedIntegrationProfile = integrationProfile;
    }

    @Override
    public IntegrationProfileOption getSelectedIntegrationProfileOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfileOption");
        }
        return selectedIntegrationProfileOption;
    }

    @Override
    public void setSelectedIntegrationProfileOption(IntegrationProfileOption ipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfileOption");
        }
        selectedIntegrationProfileOption = ipo;
        if (ipo != null) {

        }
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}