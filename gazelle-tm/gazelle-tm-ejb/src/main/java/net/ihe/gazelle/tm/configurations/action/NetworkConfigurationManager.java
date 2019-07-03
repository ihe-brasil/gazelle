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

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.UserService;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jean-Baptiste Meyer / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, sep
 * @class ConfigurationManager.java
 * @package net.ihe.gazelle.tm.systems.model
 * @see > jmeyer@irisa.fr - http://www.ihe-europe.org
 */


@Name("networkConfigurationManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("NetworkConfigurationManagerLocal")
public class NetworkConfigurationManager implements NetworkConfigurationManagerLocal, Serializable {

    private static final long serialVersionUID = 187641097537985139L;

    private static final Logger LOG = LoggerFactory.getLogger(NetworkConfigurationManager.class);

    // ~ Attributes
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    private Host selectedNetworkConfiguration;
    private List<Host> networkConfigurationList;
    private TestingSession selectedTestingSession;
    private SystemInSession selectedSystemInSession;
    private FilterDataModel<Host> datamodel;
    private FilterDataModel<Host> hostWithoutConfDatamodel;

    private Institution choosenInstitutionForAdmin;

    private Filter<Host> hostFilter;
    private Filter<Host> hostWithoutConfFilter;

    private String ipToValidate;

    public TestingSession getSelectedTestingSession() {
        return selectedTestingSession;
    }

    public void setSelectedTestingSession(TestingSession selectedTestingSession) {
        this.selectedTestingSession = selectedTestingSession;
    }

    @Override
    public String getIpToValidate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIpToValidate");
        }
        this.ipToValidate = selectedNetworkConfiguration.getIp();
        return ipToValidate;
    }

    @Override
    public void setIpToValidate(String ipToValidate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIpToValidate");
        }
        if (ipToValidate != null) {
            selectedNetworkConfiguration.setIp(ipToValidate);
        }
        this.ipToValidate = ipToValidate;
    }

    @Override
    public Host getSelectedNetworkConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedNetworkConfiguration");
        }
        return selectedNetworkConfiguration;
    }

    @Override
    public void setSelectedNetworkConfiguration(Host selectedNetworkConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedNetworkConfiguration");
        }
        this.selectedNetworkConfiguration = selectedNetworkConfiguration;
    }

    @Override
    public List<Host> getNetworkConfigurationList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNetworkConfigurationList");
        }
        return networkConfigurationList;
    }

    @Override
    public void setNetworkConfigurationList(List<Host> networkConfigurationList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNetworkConfigurationList");
        }
        this.networkConfigurationList = networkConfigurationList;
    }

    @Override
    public Institution getChoosenInstitutionForAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChoosenInstitutionForAdmin");
        }
        return choosenInstitutionForAdmin;
    }

    @Override
    public void setChoosenInstitutionForAdmin(Institution choosenInstitutionForAdmin) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setChoosenInstitutionForAdmin");
        }
        this.choosenInstitutionForAdmin = choosenInstitutionForAdmin;
    }

    @Override
    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return selectedSystemInSession;
    }

    @Override
    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }

        this.selectedSystemInSession = selectedSystemInSession;
    }

    @Override
    public void getNetworkConfigurationListDependingSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNetworkConfigurationListDependingSystemInSession");
        }
        selectedSystemInSession = (SystemInSession) Component.getInstance("selectedSystemInSession");

        if (selectedSystemInSession != null) {
            networkConfigurationList = Host.getListOfHostForASystemInSessionAsAdmin(selectedSystemInSession);
        }
    }


    @Override
    public FilterDataModel<Host> getAllNetworkConfigurations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllNetworkConfigurations");
        }
        if (datamodel == null) {
            datamodel = new FilterDataModel<Host>(getHostFilter()) {
                @Override
                protected Object getId(Host host) {
                    return host.getId();
                }
            };
        }
        return datamodel;
    }

    private HQLCriterionsForFilter<Host> getHQLCriterions() {
        HostQuery query = new HostQuery();
        HQLCriterionsForFilter<Host> hqlCriterions = query.getHQLCriterionsForFilter();
        TMCriterions.addTestingSession(hqlCriterions, "testingSession", query.testingSession());
        hqlCriterions.addPath("institution", query.institution());
        hqlCriterions.addPath("systemInSession", query.configurations().systemInSession());
        hqlCriterions.addQueryModifier(new QueryModifier<Host>() {
            @Override
            public void modifyQuery(HQLQueryBuilder<Host> hqlQueryBuilder, Map<String, Object> map) {
                HostQuery q = new HostQuery();
                hqlQueryBuilder.addRestriction(q.configurations().systemInSession().acceptedToSession().eqRestriction(true));
            }
        });
        return hqlCriterions;
    }

    public Filter<Host> getHostFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        if (hostFilter == null) {
            hostFilter = new Filter<Host>(getHQLCriterions(), requestParameterMap);
        }
        return hostFilter;
    }

    public void initFilter() {
        datamodel = null;
        hostFilter = null;
        hostWithoutConfDatamodel = null;
        hostWithoutConfFilter = null;
    }

    public Filter<Host> getHostWithoutConfFilter() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        if (hostWithoutConfFilter == null) {
            hostWithoutConfFilter = new Filter<Host>(getHQLCriterions2(), requestParameterMap);
        }
        return hostWithoutConfFilter;
    }

    @Override
    public FilterDataModel<Host> getAllHostWithoutConf() {
        if (hostWithoutConfDatamodel == null) {
            hostWithoutConfDatamodel = new FilterDataModel<Host>(getHostWithoutConfFilter()) {
                @Override
                protected Object getId(Host host) {
                    return host.getId();
                }
            };
        }
        return hostWithoutConfDatamodel;
    }

    private HQLCriterionsForFilter<Host> getHQLCriterions2() {
        HostQuery query = new HostQuery();
        HQLCriterionsForFilter<Host> hqlCriterions = query.getHQLCriterionsForFilter();
        TMCriterions.addTestingSession(hqlCriterions, "testingSession", query.testingSession());
        hqlCriterions.addPath("institution", query.institution());
        hqlCriterions.addQueryModifier(new QueryModifier<Host>() {
            @Override
            public void modifyQuery(HQLQueryBuilder<Host> hqlQueryBuilder, Map<String, Object> map) {
                HostQuery q = new HostQuery();
                hqlQueryBuilder.addRestriction(q.configurations().isEmptyRestriction());
            }
        });
        return hqlCriterions;
    }

    @Override
    public void deleteNetworkConfiguration(Host inNetworkConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteNetworkConfiguration");
        }
        if (inNetworkConfiguration == null) {
            return;
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestingSession.getSelectedTestingSession();
        // inNetworkConfiguration
        Host h = entityManager.find(Host.class, inNetworkConfiguration.getId());

        entityManager.remove(h);

        FacesMessages.instance().add(StatusMessage.Severity.INFO, inNetworkConfiguration.getHostname() + " :  configuration deleted");
    }

    @Override
    public void deleteCurrentNetworkConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteCurrentNetworkConfiguration");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestingSession.getSelectedTestingSession();
        // inNetworkConfiguration
        Host h = entityManager.find(Host.class, selectedNetworkConfiguration.getId());

        entityManager.remove(h);

        FacesMessages.instance().add(StatusMessage.Severity.INFO, selectedNetworkConfiguration.getHostname() + " : configuration deleted");
    }

    @Override
    public void viewNetworkConfiguration(Host inNetworkConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewNetworkConfiguration");
        }
        // TODO Auto-generated method stub

    }

    @Override
    public String addEditNetworkConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addEditNetworkConfiguration");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedNetworkConfiguration == null) {
            return null;
            // creation of a host
        }

        if (!validateAlias(selectedNetworkConfiguration.getAlias())) {
            return null;
        }
        if (!validateHostname(selectedNetworkConfiguration)) {
            return null;
        }
        if (!validateComment(selectedNetworkConfiguration.getComment())) {
            return null;
        }

        if (StringUtils.trimToNull(selectedNetworkConfiguration.getIp()) == null) {
            selectedNetworkConfiguration.setIp(null);
            selectedNetworkConfiguration = entityManager.merge(selectedNetworkConfiguration);
            entityManager.flush();
        } else {
            if (!validateIP(selectedNetworkConfiguration.getIp())) {
                return null;
            } else {
                TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

                HostQuery hostQuery = new HostQuery();
                hostQuery.testingSession().eq(activatedTestingSession);
                hostQuery.ip().eq(selectedNetworkConfiguration.getIp());
                hostQuery.id().neq(selectedNetworkConfiguration.getId());
                Host hostWithSameIp = hostQuery.getUniqueResult();

                if (hostWithSameIp != null) {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                            "gazelle.configuration.network.error.hostnameAlreadyExisting");
                    return null;
                }
                selectedNetworkConfiguration = entityManager.merge(selectedNetworkConfiguration);
            }
        }
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Network configuration updated/created");
        if (Role.isLoggedUserAdmin()) {
            return "/configuration/NETWORK/list/listAllHostsForAdmin.xhtml";
        } else {
            return "/configuration/list/configurationMainWindow.seam";
        }
    }

    @Override
    public boolean validateHostname(String hostname, Integer hostID) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateHostname");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        if ((hostname == null) || (hostname.trim().length() == 0)) {

            return false;
        } else {
            Session s = (Session) entityManager.getDelegate();
            Criteria c = s.createCriteria(Host.class);
            Host h = null;
            try {
                if ((h = (Host) c.add(Restrictions.eq("hostname", hostname)).uniqueResult()) == null) {
                    return true;
                } else {
                    if (h.getId().equals(hostID)) {
                        return true;
                    } else {
                        FacesMessages.instance().addFromResourceBundle("hostHostname", FacesMessage.SEVERITY_WARN,
                                "gazelle.configuration.network.error.hostnameAlreadyExisting",
                                "Hostname already existing");
                        return false;
                    }

                }
            } catch (HibernateException e) {
                net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
                return false;
            }

        }
    }

    @Override
    public boolean validateHostname(Host host) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateHostname");
        }
        String hostname = host.getHostname();

        String hostnameReg = "^([a-zA-Z][a-zA-Z0-9]*(-*[a-zA-Z0-9]+))((\\.)*([a-zA-Z][a-zA-Z0-9]*(-*[a-zA-Z0-9]+)))*$";
        Pattern regex = Pattern.compile(hostnameReg, Pattern.DOTALL);
        Matcher regexMatcher = regex.matcher(hostname);
        StringBuilder errorMessage = new StringBuilder();

        if (hostname.isEmpty() || (regexMatcher.find() && regexMatcher.group().equals(hostname))) {
            if (!hostnameIsUniqueInDB(host)) {
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("hostnameInputText", StatusMessage.Severity.WARN,
                        "Hostname already exist in database for this session", "Hostname already exist in database for this session");
                return false;
            }
            if (hostname.length() < 25) {
                return hostname.isEmpty() || !hostname.contains("_");
            } else {
                errorMessage.append("Hostname contains more than 24 characters ! (RFC-952). ");
                return validateFQDN(host, errorMessage);
            }
        } else {
            errorMessage.append("Hostname is not conform to RFC-952. (Invalid character(s))");
            return validateFQDN(host, errorMessage);
        }
    }

    private boolean hostnameIsUniqueInDB(Host host) {
        String hostname = host.getHostname();

        HostQuery q = new HostQuery();
        q.testingSession().id().eq(TestingSession.getSelectedTestingSession().getId());
        q.hostname().eq(hostname);
        if (host.getId() != null) {
            q.id().neq(host.getId());
        }
        return q.hostname().getListDistinct().isEmpty();
    }

    private boolean validateFQDN(Host host, StringBuilder errorMessage) {
        String hostname = host.getHostname();
        String fqdnReg = "(?=^.{1,254}$)(^((?!-)[a-zA-Z0-9\\-]{1,63}\\.)+(?:[a-zA-Z]{2,63})$)";
        Pattern regex = Pattern.compile(fqdnReg, Pattern.DOTALL);
        Matcher regexMatcher = regex.matcher(hostname);
        if ((regexMatcher.find() && regexMatcher.group().equals(hostname))) {
            if (!hostnameIsUniqueInDB(host)) {
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("hostnameInputText", StatusMessage.Severity.WARN,
                        "Hostname already exist in database for this session", "Hostname already eexist in database for this session");
                return false;
            }
            return true;
        } else {
            errorMessage.append("According to RFC-1035 the length of a FQDN is limited to 255 characters, and each label " +
                    "(node delimited by a dot in the hostname) is limited to 63 characters.");
        }
        StatusMessages.instance().addToControlFromResourceBundleOrDefault("hostnameInputText", StatusMessage.Severity.WARN,
                errorMessage.toString(), errorMessage.toString());
        return false;
    }

    @Override
    public boolean validateAlias(String alias) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateAlias");
        }
        String hostnameReg = "^([a-zA-Z][a-zA-Z0-9]*(-*[a-zA-Z0-9]+))((\\.)*([a-zA-Z][a-zA-Z0-9]*(-*[a-zA-Z0-9]+)))*$";
        Pattern regex = Pattern.compile(hostnameReg, Pattern.DOTALL);
        Matcher regexMatcher = regex.matcher(alias);

        if (alias.isEmpty() || (regexMatcher.find() && regexMatcher.group().equals(alias))) {
            if (alias.length() < 25) {
                return (alias.isEmpty()) || !alias.contains("_");
            } else {
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("aliasInputText",
                        StatusMessage.Severity.WARN, "Alias contains more than 24 characteres ! (RFC-952)",
                        "Alias contains more than 24 characteres ! (RFC-952)");
                return false;
            }
        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("aliasInputText",
                    StatusMessage.Severity.WARN, "Alias is not conform to RFC-952", "Alias is not conform to RFC-952");
            return false;
        }
    }

    @Override
    public boolean validateComment(String comment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateComment");
        }
        if (comment.length() < 256) {
            return true;
        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("inputComment",
                    StatusMessage.Severity.WARN, "Comment contains more than 256 characteres !",
                    "Comment contains more than 256 characteres !");
            return false;
        }
    }

    @Override
    public boolean validateIP(String ip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateIP");
        }
        if ((ip == null) || ip.trim().equals("")) {
            return true;
        }

        Pattern ipPattern = Pattern.compile(NetworkConfigurationForTestingSession._stringIPRegex_);

        if (!ipPattern.matcher(ip).matches()) {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("hostIP", StatusMessage.Severity.WARN,
                    "gazelle.configuration.network.error.invalidIP", "Invalid IP");
            return false;
        }

        // ignore domaine name and internet testing -> check ip is not inside
        // testing session
        if ((selectedNetworkConfiguration != null) && (selectedNetworkConfiguration.getIgnoreDomainName() != null)
                && selectedNetworkConfiguration.getIgnoreDomainName()
                && (selectedNetworkConfiguration.getTestingSession() != null)
                && (selectedNetworkConfiguration.getTestingSession().getInternetTesting() != null)
                && !selectedNetworkConfiguration.getTestingSession().getInternetTesting()) {
            LinkedList<String> allIPs = NetworkConfigurationForTestingSession.getAllIPs(selectedNetworkConfiguration.getTestingSession());
            if (allIPs.contains(ip)) {
                StatusMessages.instance().addToControlFromResourceBundleOrDefault("hostIP",
                        StatusMessage.Severity.WARN, "gazelle.configuration.network.error.testingSessionIp",
                        "This is an IP reserved for the testing session.");
                return false;
            }
        }

        return true;
    }

    @Override
    public String addANetworkConfigButtonClickFirst() {
        selectedNetworkConfiguration = new Host();
        return "/configuration/NETWORK/create/addNetworkConfiguration.xhtml";
    }

    @Override
    public void addANetworkConfig() {
        if (selectedNetworkConfiguration.getId() == null) {
            choosenInstitutionForAdmin = (Institution) Component.getInstance("choosenInstitutionForAdmin");
            selectedSystemInSession = (SystemInSession) Component.getInstance("selectedSystemInSession");

            if (Role.isLoggedUserAdmin()) {
                if (choosenInstitutionForAdmin == null) {
                    Institution tmp = Institution.findInstitutionWithName(selectedSystemInSession.getSystem()
                            .getUniqueInstitution().getName());
                    if (tmp != null) {
                        choosenInstitutionForAdmin = tmp;
                    }
                }
                selectedNetworkConfiguration = Host.generateNewHostForSessionWithoutPersistance(choosenInstitutionForAdmin,
                        selectedSystemInSession);
            } else {
                User loginUser = User.loggedInUser();
                selectedNetworkConfiguration = Host.generateNewHostForSessionWithoutPersistance(loginUser.getInstitution(),
                        selectedSystemInSession);
            }
        }
    }

    @Override
    public String addANetworkConfigButtonClick() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addANetworkConfigButtonClick");
        }
        selectedNetworkConfiguration = new Host();
        addANetworkConfig();
        return "/configuration/NETWORK/create/addNetworkConfiguration.xhtml";
    }

    @Override
    public String editANetworkConfigButtonClick(Host inConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editANetworkConfigButtonClick");
        }
        selectedNetworkConfiguration = inConfiguration;
        return "/configuration/NETWORK/create/addNetworkConfiguration.xhtml";
    }

    @Override
    public String cancelAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelAction");
        }
        if (selectedNetworkConfiguration != null) {
            if (selectedNetworkConfiguration.getIp() != null) {
                TestingSession.getSelectedTestingSession();
            }
        }

        if (Role.isLoggedUserAdmin()) {
            return "/configuration/NETWORK/list/listAllHostsForAdmin.xhtml";
        } else {
            return "/configuration/list/configurationMainWindow.xhtml";
        }

    }

    @Override
    public String manageAllHosts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("manageAllHosts");
        }
        return "/configuration/NETWORK/list/listAllHostsForAdmin.xhtml";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean displayDeleteButton(Host h) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayDeleteButton");
        }
        ConfigurationQuery query = new ConfigurationQuery();
        query.host().id().eq(h.getId());
        List<Configuration> listDistinct = query.getListDistinct();
        return listDistinct.size() == 0;
    }

    @Override
    public void generateIPsForAllHosts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateIPsForAllHosts");
        }
        if (hostFilter != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            for (Host h : getAllNetworkConfigurations().getAllItems(FacesContext.getCurrentInstance())) {
                h.setIp(null);
                entityManager.merge(h);
            }
            entityManager.flush();

            TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
            LinkedList<String> availableIPs = NetworkConfigurationForTestingSession
                    .getAvailableIPs(activatedTestingSession);

            for (Host h : getAllNetworkConfigurations().getAllItems(FacesContext.getCurrentInstance())) {
                if (assignIP(entityManager, availableIPs, h)) {
                    break;
                }
            }
        }
    }

    @Override
    public void generateIPsForAllHostsWithoutConfs() {
        if (hostWithoutConfFilter != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            for (Host h : getAllHostWithoutConf().getAllItems(FacesContext.getCurrentInstance())) {
                h.setIp(null);
                entityManager.merge(h);
            }
            entityManager.flush();

            TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
            LinkedList<String> availableIPs = NetworkConfigurationForTestingSession
                    .getAvailableIPs(activatedTestingSession);

            for (Host h : getAllHostWithoutConf().getAllItems(FacesContext.getCurrentInstance())) {
                if (assignIP(entityManager, availableIPs, h)) {
                    break;
                }
            }
        }
    }

    private boolean assignIP(EntityManager entityManager, LinkedList<String> availableIPs, Host h) {
        if (availableIPs.isEmpty()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No more IP's available " +
                    "(or IPs and DNS are not configure in Network" +
                    " configuration), please contacts the administrator to get one");
            return true;
        } else {
            String ip = availableIPs.pop();
            h.setIp(ip);
            entityManager.merge(h);
            entityManager.flush();
        }
        return false;
    }

    @Override
    public void generateIP(Host h) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateIP");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        LinkedList<String> availableIPs = NetworkConfigurationForTestingSession.getAvailableIPs(activatedTestingSession);
        if (availableIPs.isEmpty()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No more IP's available " +
                    "(or IPs and DNS are not configure in Network" +
                    " configuration), please contacts the administrator to get one");
        } else {
            String ip = availableIPs.pop();
            h.setIp(ip);
            entityManager.merge(h);
            entityManager.flush();
        }
    }

    @Override
    public void generateIP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateIP");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        LinkedList<String> availableIPs = NetworkConfigurationForTestingSession.getAvailableIPs(activatedTestingSession);
        if (availableIPs.isEmpty()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No more IP's available " +
                    "(or IPs and DNS are not configure in Network" +
                    " configuration), please contacts the administrator to get one");
        } else {
            String ip = availableIPs.pop();
            selectedNetworkConfiguration.setIp(ip);
        }
    }

    @Override
    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

    @Override
    public boolean canEditHost() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canEditHost");
        }
        if (UserService.hasRole(Role.ADMINISTRATOR_ROLE_STRING)) {
            return true;
        }
        if (UserService.hasRole(Role.VENDOR_ROLE_STRING) || UserService.hasRole(Role.VENDOR_ADMIN_ROLE_STRING)) {
            if ((selectedNetworkConfiguration != null) && (selectedNetworkConfiguration.getTestingSession() != null)
                    && selectedNetworkConfiguration.getTestingSession().getInternetTesting()) {
                return true;
            }
            if ((selectedNetworkConfiguration != null) && (selectedNetworkConfiguration.getIgnoreDomainName() != null)
                    && selectedNetworkConfiguration.getIgnoreDomainName()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canEditIP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canEditIP");
        }
        return canEditHost();
    }

    @Override
    public boolean canGenerateIP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canGenerateIP");
        }
        if (UserService.hasRole(Role.ADMINISTRATOR_ROLE_STRING)) {
            if ((selectedNetworkConfiguration != null) && (selectedNetworkConfiguration.getTestingSession() != null)
                    && selectedNetworkConfiguration.getTestingSession().getInternetTesting()) {
                return false;
            }
            if ((selectedNetworkConfiguration != null) && (selectedNetworkConfiguration.getIgnoreDomainName() != null)
                    && selectedNetworkConfiguration.getIgnoreDomainName()) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canHostDNS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canHostDNS");
        }
        if ((selectedNetworkConfiguration != null) && (selectedNetworkConfiguration.getTestingSession() != null)
                && selectedNetworkConfiguration.getTestingSession().getInternetTesting()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canEditHostDNS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canEditHostDNS");
        }
        if (UserService.hasRole(Role.ADMINISTRATOR_ROLE_STRING)) {
            return true;
        }
        if (UserService.hasRole(Role.VENDOR_ROLE_STRING) || UserService.hasRole(Role.VENDOR_ADMIN_ROLE_STRING)) {
            return true;
        }
        return false;
    }

    @Override
    public void releaseIPsForAllHosts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("releaseIPsForAllHosts");
        }
        if (hostFilter != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            for (Host h : getAllNetworkConfigurations().getAllItems(FacesContext.getCurrentInstance())) {
                h.setIp(null);
                entityManager.merge(h);
            }
            entityManager.flush();
        }
    }

    @Override
    public void releaseIPsForAllHostsWithoutConfs() {
        if (hostWithoutConfFilter != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            for (Host h : getAllHostWithoutConf().getAllItems(FacesContext.getCurrentInstance())) {
                h.setIp(null);
                entityManager.merge(h);
            }
            entityManager.flush();
        }
    }

    @Override
    public String goToListAllHostsForAdminPage() {
        StringBuilder sb = new StringBuilder();
        sb.append("/configuration/NETWORK/list/listAllHostsForAdmin.seam");
        if (!getHostWithoutConfFilter().getUrlParameters().isEmpty()) {
            sb.append("?").append(getHostWithoutConfFilter().getUrlParameters());
        }
        return sb.toString();
    }

    @Override
    public String goToListAllHostsWithoutConfsForAdminPage() {
        StringBuilder sb = new StringBuilder();
        sb.append("/configuration/NETWORK/list/listAllHostsWithoutConfsForAdmin.seam");
        if (!getHostFilter().getUrlParameters().isEmpty()) {
            sb.append("?").append(getHostFilter().getUrlParameters());
        }
        return sb.toString();
    }
}
