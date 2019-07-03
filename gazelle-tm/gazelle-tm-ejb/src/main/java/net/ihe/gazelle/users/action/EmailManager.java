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
package net.ihe.gazelle.users.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Name("emailManager")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
@GenerateInterface("EmailManagerLocal")
public class EmailManager implements java.io.Serializable, EmailManagerLocal {

    // ~ Attributes
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -450973196361283760L;

    private static final Logger LOG = LoggerFactory.getLogger(EmailManager.class);

    private User recipient;

    private User concerned;

    private Institution institution;

    private String adminLabels;

    private List<User> institutionAdmins;

    private String applicationAdminEmail;

    private String applicationAdminName;

    // ~ Methods
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Send a confirmation email - Static method - this email is sent just after a new user registration into the ProductRegistry
     *
     * @param renderer
     * @param statusMessages
     * @param log
     * @throws Exception
     */
    @Override
    public void sendEmail(EmailTemplate emailTemplate) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail");
        }
        try {

            Renderer renderer = (Renderer) Component.getInstance("org.jboss.seam.faces.renderer");
            renderer.render(emailTemplate.getPageName());
        } catch (final Exception e) {
            LOG.error("Error sending mail " + emailTemplate.getPageName() + ", error =" + e.getMessage());
            throw e;
        }
    }

    @Override
    public User getRecipient() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRecipient");
        }
        return recipient;
    }

    @Override
    public void setRecipient(User recipient) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRecipient");
        }
        this.recipient = recipient;
    }

    @Override
    public User getConcerned() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConcerned");
        }
        return concerned;
    }

    @Override
    public void setConcerned(User concerned) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setConcerned");
        }
        this.concerned = concerned;
    }

    @Override
    public String getAdminLabels() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAdminLabels");
        }
        return adminLabels;
    }

    @Override
    public void setAdminLabels(String adminLabels) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAdminLabels");
        }
        this.adminLabels = adminLabels;
    }

    @Override
    public List<User> getInstitutionAdmins() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionAdmins");
        }
        return institutionAdmins;
    }

    @Override
    public void setInstitutionAdmins(List<User> institutionAdmins) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInstitutionAdmins");
        }
        this.institutionAdmins = institutionAdmins;
    }

    @Override
    public Institution getInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitution");
        }
        return institution;
    }

    @Override
    public void setInstitution(Institution institution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInstitution");
        }
        this.institution = institution;
    }

    public String getApplicationAdminEmail() {
        return ApplicationPreferenceManager.instance().getApplicationAdminEmail();
    }

    public void setApplicationAdminEmail(String applicationAdminEmail) {
        this.applicationAdminEmail = applicationAdminEmail;
    }

    public String getApplicationAdminName() {
        return ApplicationPreferenceManager.instance().getApplicationAdminName();
    }

    public void setApplicationAdminName(String applicationAdminName) {
        this.applicationAdminName = applicationAdminName;
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
        //
    }

}
