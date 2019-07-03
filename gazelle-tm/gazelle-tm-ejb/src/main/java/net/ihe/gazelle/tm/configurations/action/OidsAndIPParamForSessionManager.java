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
package net.ihe.gazelle.tm.configurations.action;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.LoggerFactory;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.report.ReportExporterManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.configurations.model.AbstractConfiguration;
import net.ihe.gazelle.tm.configurations.model.ConfigurationType;
import net.ihe.gazelle.tm.configurations.model.GenericOIDSConfigurationForSession;
import net.ihe.gazelle.tm.configurations.model.GenericOIDSInstitutionForSession;
import net.ihe.gazelle.tm.configurations.model.Host;
import net.ihe.gazelle.tm.configurations.model.NetworkConfigurationForTestingSession;
import net.ihe.gazelle.tm.configurations.model.OIDSConfigurationForSessionForHL7;
import net.ihe.gazelle.tm.configurations.model.OIDSConfigurationForSessionForHL7.OidsTypeForHL7;
import net.ihe.gazelle.tm.configurations.model.ProxyConfigurationForSession;
import net.ihe.gazelle.tm.configurations.model.WebServiceConfiguration;
import net.ihe.gazelle.tm.configurations.model.WebServiceType;
import net.ihe.gazelle.tm.configurations.model.HL7.AbstractHL7Configuration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.interfaces.ServerConfiguration;
import net.ihe.gazelle.tm.gazelletest.action.ProxyManager;
import net.ihe.gazelle.tm.gazelletest.bean.IpComparator;
import net.ihe.gazelle.tm.systems.model.OIDInstitution;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;

/**
 * @author Jean-Baptiste Meyer - / INRIA Rennes IHE development Project
 * @version 1.0 - 2009, Aug 15
 * @class OIDsAndIPParametersForTestingSessionManager.java
 * @see > jmeyer@irisa.fr - http://www.ihe-europe.org
 */

@Name("oidsAndIPParamForSessionManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("OidsAndIPParamForSessionManagerLocal")
public class OidsAndIPParamForSessionManager implements Serializable, OidsAndIPParamForSessionManagerLocal {
    // ~ Statics variables and Class initializer
    // ///////////////////////////////////////////////////////////////////////

    private static final long serialVersionUID = -1357012043456235656L;
    // ~ Attributes
    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(OidsAndIPParamForSessionManager.class);
    private static final String HOSTS_FILENAME = "hosts.txt";
    /**
     * entityManager is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;
    private OIDSConfigurationForSessionForHL7 oidsConfigurationForHL7;
    private List<GenericOIDSConfigurationForSession> oidsConfigurationsForWS;
    private GenericOIDSInstitutionForSession oidInstitutionForSession;

    private NetworkConfigurationForTestingSession networkConfigurationForTestingSession;

    private OIDSConfigurationForSessionForHL7.OidsTypeForHL7 oidChoosedForHL7;
    private GenericOIDSConfigurationForSession oidChoosedForWS;

    private List<AbstractConfiguration> listOfConfigWithOids;

    private ProxyConfigurationForSession proxyConfigurationForForAllServicesSession;

    // ~ Methods
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getExistingOIDForSystemAndConfigurationType(AbstractConfiguration configurationUsed,
                                                                     SystemInSession inSis) {
        try {
            Class<?> classUsed = Class.forName(configurationUsed.getClass().getCanonicalName());
            List<Class<?>> listOfClass = AbstractConfiguration.GetListOfClassWithOIDS();
            if (!listOfClass.contains(classUsed)) {
                return null;
            }

            List<AbstractConfiguration> confs = AbstractConfiguration.getConfigurationsFiltered(classUsed, inSis, null,
                    null, null, null, null, null, null, null);
            String oid = null;
            if ((confs != null) && (confs.size() > 0)) {
                for (AbstractConfiguration confToUse : confs) {
                    if (classUsed.getSuperclass().equals(AbstractHL7Configuration.class)) {

                        if (((AbstractHL7Configuration) confToUse).getAssigningAuthorityOID() != null) {
                            oid = ((AbstractHL7Configuration) confToUse).getAssigningAuthorityOID();
                            break;
                        }

                    } else if (classUsed.equals(WebServiceConfiguration.class)) {
                        LOG.debug("WS Type"
                                + ((WebServiceConfiguration) configurationUsed).getWebServiceType().getProfile()
                                .getKeyword());

                        if ((((WebServiceConfiguration) confToUse).getAssigningAuthorityOID() != null)
                                && (((WebServiceConfiguration) confToUse).getAssigningAuthorityOID().trim().length() > 0)
                                && ((WebServiceConfiguration) configurationUsed).getWebServiceType().equals(
                                ((WebServiceConfiguration) confToUse).getWebServiceType())) {
                            oid = ((WebServiceConfiguration) confToUse).getAssigningAuthorityOID();

                            break;
                        }
                    }

                }
            }
            if ((oid == null) || (oid.trim().length() == 0)) {
                return null;
            } else {
                return oid;
            }

        } catch (ClassNotFoundException e) {
            return null;
        }

    }

    @Override
    public void initializeOIDSConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeOIDSConfiguration");
        }

        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

        oidsConfigurationForHL7 = OIDSConfigurationForSessionForHL7
                .getConfigurationParametersForSession(activatedTestingSession);

        if (oidsConfigurationForHL7 == null) {
            oidsConfigurationForHL7 = new OIDSConfigurationForSessionForHL7(activatedTestingSession);
        }

        oidInstitutionForSession = GenericOIDSInstitutionForSession
                .getOIDInstitutionParametersForSession(activatedTestingSession);
        if (oidInstitutionForSession == null) {
            oidInstitutionForSession = new GenericOIDSInstitutionForSession();
            oidInstitutionForSession.setTestingSession(activatedTestingSession);
            oidInstitutionForSession.setCurrentOIDValue(0);
        }

        List<WebServiceType> wsTypeNeedingOid = WebServiceType.listWebServicesTypeFiltered(null, true);
        if (wsTypeNeedingOid.size() > 0) {
            oidsConfigurationsForWS = GenericOIDSConfigurationForSession
                    .getConfigurationParametersForSession(activatedTestingSession);
            List<GenericOIDSConfigurationForSession> listOfGenericOidsConfToAdd = new ArrayList<GenericOIDSConfigurationForSession>();
            for (WebServiceType currentWSType : wsTypeNeedingOid) {
                boolean confExistingBool = false;
                for (GenericOIDSConfigurationForSession genericOidConf : oidsConfigurationsForWS) {
                    if (genericOidConf.getProfileFor().equals(currentWSType.getProfile())) {
                        confExistingBool = true;
                        break;
                    }
                }

                if (!confExistingBool) {
                    GenericOIDSConfigurationForSession newOIDConf = new GenericOIDSConfigurationForSession();
                    newOIDConf.setTestingSession(activatedTestingSession);
                    newOIDConf.setProfileFor(currentWSType.getProfile());
                    newOIDConf.setCurrentOIDValue(0);

                    newOIDConf = entityManager.merge(newOIDConf);
                    listOfGenericOidsConfToAdd.add(newOIDConf);
                }

            }

            entityManager.flush();

            oidsConfigurationsForWS.addAll(listOfGenericOidsConfToAdd);
            Collections.sort(oidsConfigurationsForWS);
        }
    }

    @Override
    public GenericOIDSInstitutionForSession getOidInstitutionForSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOidInstitutionForSession");
        }
        return oidInstitutionForSession;
    }

    @Override
    public void setOidInstitutionForSession(GenericOIDSInstitutionForSession oidInstitutionForSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setOidInstitutionForSession");
        }
        this.oidInstitutionForSession = oidInstitutionForSession;
    }

    @Override
    public OIDSConfigurationForSessionForHL7.OidsTypeForHL7 getOidChoosedForHL7() {
        return oidChoosedForHL7;
    }

    @Override
    public void setOidChoosedForHL7(OIDSConfigurationForSessionForHL7.OidsTypeForHL7 oidChoosed) {
        this.oidChoosedForHL7 = oidChoosed;
    }

    @Override
    public GenericOIDSConfigurationForSession getOidChoosedForWS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOidChoosedForWS");
        }
        return oidChoosedForWS;
    }

    @Override
    public void setOidChoosedForWS(GenericOIDSConfigurationForSession oidChoosedForWS) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setOidChoosedForWS");
        }
        this.oidChoosedForWS = oidChoosedForWS;
    }

    @Override
    public List<AbstractConfiguration> getListOfConfigWithOids() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfConfigWithOids");
        }
        return listOfConfigWithOids;
    }

    @Override
    public void setListOfConfigWithOids(List<AbstractConfiguration> listOfConfigWithOids) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListOfConfigWithOids");
        }
        this.listOfConfigWithOids = listOfConfigWithOids;
    }

    @Override
    public void initializeNetworkConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeNetworkConfiguration");
        }

        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        networkConfigurationForTestingSession = NetworkConfigurationForTestingSession
                .getConfigurationParametersForSession(activatedTestingSession);

        if (networkConfigurationForTestingSession == null) {
            networkConfigurationForTestingSession = new NetworkConfigurationForTestingSession(activatedTestingSession);
        }
    }

    @Override
    public void initializeNetworkConfiguration(TestingSession ts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeNetworkConfiguration");
        }

        networkConfigurationForTestingSession = NetworkConfigurationForTestingSession
                .getConfigurationParametersForSession(ts);

        if (networkConfigurationForTestingSession == null) {
            networkConfigurationForTestingSession = new NetworkConfigurationForTestingSession(ts);
        }
    }

    @Override
    public OIDSConfigurationForSessionForHL7 getOidsConfigurationForHL7() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOidsConfigurationForHL7");
        }
        return oidsConfigurationForHL7;
    }

    @Override
    public void setOidsConfigurationForHL7(OIDSConfigurationForSessionForHL7 oidsConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setOidsConfigurationForHL7");
        }
        this.oidsConfigurationForHL7 = oidsConfiguration;
    }

    @Override
    public List<GenericOIDSConfigurationForSession> getOidsConfigurationsForWS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOidsConfigurationsForWS");
        }
        return oidsConfigurationsForWS;
    }

    @Override
    public void setOidsConfigurationsForWS(List<GenericOIDSConfigurationForSession> oidsConfigurationsForWS) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setOidsConfigurationsForWS");
        }
        this.oidsConfigurationsForWS = oidsConfigurationsForWS;
    }

    @Override
    public NetworkConfigurationForTestingSession getNetworkConfigurationForTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNetworkConfigurationForTestingSession");
        }
        return networkConfigurationForTestingSession;
    }

    @Override
    public void setNetworkConfigurationForTestingSession(
            NetworkConfigurationForTestingSession networkConfigurationForTestingSession) {
        this.networkConfigurationForTestingSession = networkConfigurationForTestingSession;
    }

    @Override
    public ProxyConfigurationForSession getProxyConfigurationForForAllServicesSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProxyConfigurationForForAllServicesSession");
        }
        // added NULL check to support IP address display in ITB Message Details
        if (proxyConfigurationForForAllServicesSession == null) {
            this.initProxiesConfigurationsForSession();
        }

        return proxyConfigurationForForAllServicesSession;
    }

    @Override
    public void setProxyConfigurationForForAllServicesSession(
            ProxyConfigurationForSession proxyConfigurationForForAllServicesSession) {
        this.proxyConfigurationForForAllServicesSession = proxyConfigurationForForAllServicesSession;
    }

    @Override
    public void addOrUpdateCurrentOIDSConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addOrUpdateCurrentOIDSConfiguration");
        }
        if (oidsConfigurationForHL7 == null) {
            return;
        }

        if (validateCurrentOIDSConfiguration()) {
            oidsConfigurationForHL7 = entityManager.merge(oidsConfigurationForHL7);
            StatusMessages.instance().add("HL7 OIDs updated");
        }

        for (GenericOIDSConfigurationForSession oidsConf : oidsConfigurationsForWS) {
            oidsConf.setOidForSession(oidsConfigurationForHL7.getOidForSession());

            if (validateGenericOIDConfiguration(oidsConf)) {
                entityManager.merge(oidsConf);
            } else {
                break;
            }

        }

        if (oidInstitutionForSession != null) {
            oidInstitutionForSession.setOidForSession(oidsConfigurationForHL7.getOidForSession());
            if (this.validateGenericOIDInstitution()) {
                oidInstitutionForSession = entityManager.merge(oidInstitutionForSession);
                StatusMessages.instance().add("Configuration of Insitution OID is updated");
            }
        }

        StatusMessages.instance().add("Webservices OIDs updated");
        entityManager.flush();
    }

    @Override
    public boolean validateCurrentOIDSConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateCurrentOIDSConfiguration");
        }
        if (oidsConfigurationForHL7 == null) {
            return false;
        }
        if ((oidsConfigurationForHL7.getOidForSession() == null)
                || (oidsConfigurationForHL7.getOidForSession().trim().length() == 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "OID for this session is null, please fill a correct value");
            return false;
        }
        if ((oidsConfigurationForHL7.getOidForHL7V2() == null)
                || (oidsConfigurationForHL7.getOidForHL7V2().trim().length() == 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "OID for HL7V2 is null, please fill a correct value");
            return false;
        }
        if ((oidsConfigurationForHL7.getOidForHL7V3() == null)
                || (oidsConfigurationForHL7.getOidForHL7V3().trim().length() == 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "OID for HL7V3 is null, please fill a correct value");
            return false;
        }

        return true;

    }

    @Override
    public boolean validateGenericOIDConfiguration(GenericOIDSConfigurationForSession genericConfig) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateGenericOIDConfiguration");
        }

        if (genericConfig == null) {
            return false;
        }
        if ((genericConfig.getOidForSession() == null) || (genericConfig.getOidForSession().trim().length() == 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "OID for this session is null, please fill a correct value");
            return false;
        }
        if ((genericConfig.getOidRootValue() == null) || (genericConfig.getOidRootValue().trim().length() == 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "OID for " + genericConfig.getProfileFor().getKeyword() + " is null, please fill a correct value");
            return false;
        }

        return true;
    }

    @Override
    public void addOrUpdateCurrentNetworkConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addOrUpdateCurrentNetworkConfiguration");
        }

        if (validateNetworkConfiguration()) {
            networkConfigurationForTestingSession = entityManager.merge(networkConfigurationForTestingSession);
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "#{messages['gazelle.systems.session.networkConfig.invalidRangeOrRangeEqualDNSIPAddress']}");
            return;
        }
    }

    private boolean validateNetworkConfiguration() {
        try {
            if (networkConfigurationForTestingSession == null) {
                return false;
            }

            InetAddress addressLow = InetAddress.getByName(networkConfigurationForTestingSession.getIpRangeLow());
            InetAddress addressHigh = InetAddress.getByName(networkConfigurationForTestingSession.getIpRangeHigh());
            InetAddress.getByName(networkConfigurationForTestingSession.getIpDns());
            if (addressLow.equals(addressHigh)) {
                return false;
            }

            return true;
        } catch (UnknownHostException e) {
            net.ihe.gazelle.common.log.ExceptionLogging.logException(e, LOG);
            return false;
        }
    }

    @Override
    public String getNumberOfIpsAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfIpsAvailable");
        }
        int allIps = NetworkConfigurationForTestingSession.getAllIPs(TestingSession.getSelectedTestingSession()).size();
        int availableIps = NetworkConfigurationForTestingSession.getAvailableIPs(
                TestingSession.getSelectedTestingSession()).size();
        return availableIps + " / " + allIps;
    }

    @Override
    public boolean validateOIDHL7V2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateOIDHL7V2");
        }
        boolean returnValue = validatorForOID(oidsConfigurationForHL7.getOidForHL7V2());
        if (!returnValue) {
            StatusMessages.instance().addToControl("hl7v2InputText", "Invalid oid for HL7V2");
        }
        return returnValue;
    }

    @Override
    public boolean validateOIDInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateOIDInstitution");
        }
        boolean returnValue = Pattern.matches("[0-9]*(\\.(0|([^0[1-9]][0-9]*)))*",
                oidInstitutionForSession.getOidForSession() + "." + oidInstitutionForSession.getOidRootValue() + ".0");
        if (!returnValue) {
            StatusMessages.instance().addToControl("oidforinsinp", "Invalid oid for Institutions");
        }
        return returnValue;
    }

    @Override
    public boolean validateOIDHL7V3() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateOIDHL7V3");
        }
        boolean returnValue = validatorForOID(oidsConfigurationForHL7.getOidForHL7V3());
        if (!returnValue) {

            StatusMessages.instance().addToControl("hl7v3InputText", "Invalid oid for HL7V3");
        }
        return returnValue;
    }

    @Override
    public boolean validateWSOids(String finalOID) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateWSOids");
        }
        boolean returnValue = validatorForOID(finalOID);
        if (!returnValue) {

            StatusMessages.instance().addToControl("hl7v3InputText", "Invalid oid for HL7V3");
        }
        return returnValue;
    }

    private boolean validatorForOID(String finalOID) {

        return Pattern.matches("[0-9]*(\\.(0|([^0[1-9]][0-9]*)))*", oidsConfigurationForHL7.getOidForSession() + "."
                + finalOID + ".0");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializeOIDSList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeOIDSList");
        }

        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

        if (oidChoosedForHL7 == null) {
            return;
        }
        if (oidChoosedForHL7 == OidsTypeForHL7.oidHL7V2) {

            listOfConfigWithOids = AbstractConfiguration.GetListOfConfiguration(activatedTestingSession,
                    HL7V2InitiatorConfiguration.class, HL7V2ResponderConfiguration.class);
        } else if (oidChoosedForHL7 == OidsTypeForHL7.oidHL7V3) {

            listOfConfigWithOids = AbstractConfiguration.GetListOfConfiguration(activatedTestingSession,
                    HL7V3InitiatorConfiguration.class, HL7V3ResponderConfiguration.class);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializeOIDSListForWS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeOIDSListForWS");
        }

        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

        if (oidChoosedForWS == null) {
            return;
        } else {
            listOfConfigWithOids = WebServiceConfiguration.GetListOfConfigurations(activatedTestingSession,
                    oidChoosedForWS);
        }
    }

    @Override
    public void resetAllOIDS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetAllOIDS");
        }
        oidsConfigurationForHL7.setCurrentOIDForHL7V2(0);
        oidsConfigurationForHL7.setCurrentOIDForHL7V3(0);

        oidsConfigurationForHL7 = entityManager.merge(oidsConfigurationForHL7);

        for (GenericOIDSConfigurationForSession gOid : oidsConfigurationsForWS) {
            gOid.setCurrentOIDValue(0);
            entityManager.merge(gOid);
        }

        oidInstitutionForSession.setCurrentOIDValue(0);
        oidInstitutionForSession = entityManager.merge(oidInstitutionForSession);

    }

    @Override
    public void assignAllOIDSInConfigurations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("assignAllOIDSInConfigurations");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        List<Institution> listOfInstitution = Institution.listAllInstitutions();
        EntityManager em = EntityManagerService.provideEntityManager();
        for (Institution currentInstitution : listOfInstitution) {
            this.rewriteInstitutionOID(currentInstitution, activatedTestingSession);
            List<SystemInSession> listOfSystemsInSession = SystemInSession.getSystemsInSessionForCompanyForSession(em,
                    currentInstitution, activatedTestingSession);

            for (SystemInSession systemInSession : listOfSystemsInSession) {
                this.rewriteAllConfigurationHL7V2ForSystemInSession(systemInSession);
                this.rewriteAllConfigurationHL7V3ForSystemInSession(systemInSession);

                if (oidsConfigurationsForWS != null) {
                    List<AbstractConfiguration> listOfWebService = AbstractConfiguration.GetListOfConfiguration(
                            systemInSession, WebServiceConfiguration.class);
                    Hashtable<IntegrationProfile, String> hashTable = new Hashtable<IntegrationProfile, String>();

                    for (AbstractConfiguration conf : listOfWebService) {
                        WebServiceConfiguration wsconf = (WebServiceConfiguration) conf;
                        this.rewriteOIDOfWebServiceConfiguration(wsconf, hashTable);
                    }
                }

            }
        }

        FacesMessages.instance().add(StatusMessage.Severity.INFO, "OIDs assigned");
    }

    private void rewriteOIDOfWebServiceConfiguration(WebServiceConfiguration wsconf,
                                                     Hashtable<IntegrationProfile, String> hashTable) {
        for (GenericOIDSConfigurationForSession genericConf : oidsConfigurationsForWS) {
            if (wsconf.getWebServiceType().getProfile().equals(genericConf.getProfileFor())) {
                if (hashTable.containsKey(wsconf.getWebServiceType().getProfile())) {
                    wsconf.setAssigningAuthorityOID(hashTable.get(wsconf.getWebServiceType().getProfile()));
                    entityManager.merge(wsconf);
                } else {
                    String oid = genericConf.toString();
                    hashTable.put(wsconf.getWebServiceType().getProfile(), oid);
                    genericConf.incrementOID();

                    wsconf.setAssigningAuthorityOID(oid);
                    entityManager.merge(wsconf);
                }
                break;
            }
        }
    }

    private void rewriteAllConfigurationHL7V2ForSystemInSession(SystemInSession systemInSession) {
        if (oidsConfigurationForHL7 != null) {
            String oidForHL7V2 = oidsConfigurationForHL7.toString(OidsTypeForHL7.oidHL7V2);
            List<AbstractConfiguration> listOfConfigForHL7V2 = AbstractConfiguration.GetListOfConfiguration(
                    systemInSession, HL7V2InitiatorConfiguration.class, HL7V2ResponderConfiguration.class);
            if (listOfConfigForHL7V2.size() > 0) {
                for (AbstractConfiguration conf : listOfConfigForHL7V2) {
                    ((AbstractHL7Configuration) conf).setAssigningAuthorityOID(oidForHL7V2);
                    entityManager.merge(conf);
                }
                oidsConfigurationForHL7.incrementOID(OidsTypeForHL7.oidHL7V2);
            }
        }
    }

    private void rewriteInstitutionOID(Institution currentInstitution, TestingSession activatedTestingSession) {
        List<OIDInstitution> loi = OIDInstitution.getOIDInstitutionFiltered(currentInstitution,
                activatedTestingSession, null);
        OIDInstitution currentOIDIns = null;
        if (loi != null) {
            if (loi.size() > 0) {
                currentOIDIns = loi.get(0);
            }
        }
        if (currentOIDIns == null) {
            currentOIDIns = new OIDInstitution();
            currentOIDIns.setInstitution(currentInstitution);
            currentOIDIns.setTestingSession(activatedTestingSession);
        }
        String oid = oidInstitutionForSession.generateOIDForInstitution();
        currentOIDIns.setOid(oid);
        entityManager.merge(currentOIDIns);
        oidInstitutionForSession = oidInstitutionForSession.incrementOID();
    }

    private void rewriteAllConfigurationHL7V3ForSystemInSession(SystemInSession systemInSession) {
        if (oidsConfigurationForHL7 != null) {
            String oidForHL7V3 = oidsConfigurationForHL7.toString(OidsTypeForHL7.oidHL7V3);
            List<AbstractConfiguration> listOfConfigForHL7V3 = AbstractConfiguration.GetListOfConfiguration(
                    systemInSession, HL7V3InitiatorConfiguration.class, HL7V3ResponderConfiguration.class);
            if (listOfConfigForHL7V3.size() > 0) {
                for (AbstractConfiguration conf : listOfConfigForHL7V3) {
                    ((AbstractHL7Configuration) conf).setAssigningAuthorityOID(oidForHL7V3);
                    entityManager.merge(conf);
                }

                oidsConfigurationForHL7.incrementOID(OidsTypeForHL7.oidHL7V3);
            }
        }
    }

    /**
     * update configurations and institution which have not oids
     */
    @Override
    public void updateOIDS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateOIDS");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        List<Institution> listOfInstitution = Institution.listAllInstitutions();
        EntityManager em = EntityManagerService.provideEntityManager();

        for (Institution currentInstitution : listOfInstitution) {
            this.updateInstitutionOID(currentInstitution, activatedTestingSession);
            List<SystemInSession> listOfSystemsInSession = SystemInSession.getSystemsInSessionForCompanyForSession(em,
                    currentInstitution, activatedTestingSession);

            for (SystemInSession systemInSession : listOfSystemsInSession) {
                this.updateAllConfigurationHL7V2ForSystemInSession(systemInSession);
                this.updateAllConfigurationHL7V3ForSystemInSession(systemInSession);

                if (oidsConfigurationsForWS != null) {
                    List<AbstractConfiguration> listOfWebService = AbstractConfiguration.GetListOfConfiguration(
                            systemInSession, WebServiceConfiguration.class);
                    Hashtable<IntegrationProfile, String> hashTable = new Hashtable<IntegrationProfile, String>();

                    for (AbstractConfiguration conf : listOfWebService) {
                        WebServiceConfiguration wsconf = (WebServiceConfiguration) conf;
                        this.updateOIDOfWebServiceConfiguration(wsconf, hashTable);
                    }
                }

            }
        }
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Configurations and institutions which have no OIDs were updated");
    }

    private void updateOIDOfWebServiceConfiguration(WebServiceConfiguration wsconf,
                                                    Hashtable<IntegrationProfile, String> hashTable) {
        if (wsconf.getAssigningAuthorityOID() == null) {
            wsconf.setAssigningAuthorityOID("");
        }
        if (!wsconf.getAssigningAuthorityOID().equals("")) {
            hashTable.put(wsconf.getWebServiceType().getProfile(), wsconf.getAssigningAuthorityOID());
            return;
        }
        for (GenericOIDSConfigurationForSession genericConf : oidsConfigurationsForWS) {
            if (wsconf.getWebServiceType().getProfile().equals(genericConf.getProfileFor())) {
                if (hashTable.containsKey(wsconf.getWebServiceType().getProfile())) {
                    wsconf.setAssigningAuthorityOID(hashTable.get(wsconf.getWebServiceType().getProfile()));
                    entityManager.merge(wsconf);
                    entityManager.flush();
                } else {
                    String oid = genericConf.toString();
                    hashTable.put(wsconf.getWebServiceType().getProfile(), oid);
                    genericConf.incrementOID();

                    wsconf.setAssigningAuthorityOID(oid);
                    entityManager.merge(wsconf);
                    entityManager.flush();
                }
                break;
            }
        }
    }

    private void updateAllConfigurationHL7V2ForSystemInSession(SystemInSession systemInSession) {
        if (oidsConfigurationForHL7 != null) {
            String oidForHL7V2 = oidsConfigurationForHL7.toString(OidsTypeForHL7.oidHL7V2);
            List<AbstractConfiguration> listOfConfigForHL7V2 = AbstractConfiguration.GetListOfConfiguration(
                    systemInSession, HL7V2InitiatorConfiguration.class, HL7V2ResponderConfiguration.class);

            boolean mustincrement = true;

            if (listOfConfigForHL7V2.size() == 0) {
                mustincrement = false;
            }

            for (AbstractConfiguration conf : listOfConfigForHL7V2) {
                if (((AbstractHL7Configuration) conf).getAssigningAuthorityOID() != null) {
                    if (!((AbstractHL7Configuration) conf).getAssigningAuthorityOID().equals("")) {
                        oidForHL7V2 = ((AbstractHL7Configuration) conf).getAssigningAuthorityOID();
                        mustincrement = false;
                        break;
                    }
                }
            }

            for (AbstractConfiguration conf : listOfConfigForHL7V2) {
                if ((((AbstractHL7Configuration) conf).getAssigningAuthorityOID() == null)
                        || (((AbstractHL7Configuration) conf).getAssigningAuthorityOID().equals(""))) {
                    ((AbstractHL7Configuration) conf).setAssigningAuthorityOID(oidForHL7V2);
                    entityManager.merge(conf);
                    entityManager.flush();
                }
            }

            if (mustincrement == true) {
                oidsConfigurationForHL7.incrementOID(OidsTypeForHL7.oidHL7V2);
                entityManager.flush();
            }
        }
    }

    private void updateInstitutionOID(Institution currentInstitution, TestingSession activatedTestingSession) {
        List<OIDInstitution> loi = OIDInstitution.getOIDInstitutionFiltered(currentInstitution,
                activatedTestingSession, null);
        OIDInstitution currentOIDIns = null;
        if (loi != null) {
            if (loi.size() > 0) {
                return;
            }
        }
        if (currentOIDIns == null) {
            currentOIDIns = new OIDInstitution();
            currentOIDIns.setInstitution(currentInstitution);
            currentOIDIns.setTestingSession(activatedTestingSession);
        }
        String oid = oidInstitutionForSession.generateOIDForInstitution();
        currentOIDIns.setOid(oid);
        entityManager.merge(currentOIDIns);
        oidInstitutionForSession = oidInstitutionForSession.incrementOID();
        entityManager.flush();
    }

    private void updateAllConfigurationHL7V3ForSystemInSession(SystemInSession systemInSession) {
        if (oidsConfigurationForHL7 != null) {
            String oidForHL7V3 = oidsConfigurationForHL7.toString(OidsTypeForHL7.oidHL7V3);
            List<AbstractConfiguration> listOfConfigForHL7V3 = AbstractConfiguration.GetListOfConfiguration(
                    systemInSession, HL7V3InitiatorConfiguration.class, HL7V3ResponderConfiguration.class);

            boolean mustincrement = true;

            if (listOfConfigForHL7V3.size() == 0) {
                mustincrement = false;
            }

            for (AbstractConfiguration conf : listOfConfigForHL7V3) {
                if (((AbstractHL7Configuration) conf).getAssigningAuthorityOID() != null) {
                    if (!((AbstractHL7Configuration) conf).getAssigningAuthorityOID().equals("")) {
                        oidForHL7V3 = ((AbstractHL7Configuration) conf).getAssigningAuthorityOID();
                        mustincrement = false;
                        break;
                    }
                }
            }

            for (AbstractConfiguration conf : listOfConfigForHL7V3) {
                if ((((AbstractHL7Configuration) conf).getAssigningAuthorityOID() == null)
                        || (((AbstractHL7Configuration) conf).getAssigningAuthorityOID().equals(""))) {
                    ((AbstractHL7Configuration) conf).setAssigningAuthorityOID(oidForHL7V3);
                    entityManager.merge(conf);
                    entityManager.flush();
                }
            }

            if (mustincrement) {

                oidsConfigurationForHL7.incrementOID(OidsTypeForHL7.oidHL7V3);
                entityManager.flush();
            }
        }
    }

    @Override
    public void deleteAllOIDSInConfigurations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllOIDSInConfigurations");
        }

        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

        List<AbstractConfiguration> list = AbstractConfiguration
                .GetListOfConfigurationWithOIDS(activatedTestingSession);

        if (list != null) {
            for (AbstractConfiguration conf : list) {
                if (conf instanceof AbstractHL7Configuration) {
                    ((AbstractHL7Configuration) conf).setAssigningAuthorityOID("");
                }
                if (conf instanceof WebServiceConfiguration) {
                    ((WebServiceConfiguration) conf).setAssigningAuthorityOID("");
                }

                entityManager.merge(conf);
            }
        }

        this.deleteAllOIDInstitutionForCurrentTestingSession();

        resetAllOIDS();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "OIDs deleted");
    }

    private void deleteAllOIDInstitutionForCurrentTestingSession() {
        List<OIDInstitution> loi = OIDInstitution.getAllOIDInstitutionForCurrentTestingSession();
        for (OIDInstitution oidInstitution : loi) {
            entityManager.remove(oidInstitution);
        }
        entityManager.flush();
    }

    @Override
    public void initProxiesConfigurationsForSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initProxiesConfigurationsForSession");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

        ConfigurationType.GetConfigurationTypeCompatibleWithAProxy();

        ProxyConfigurationForSession proxyConfigForSession = ProxyConfigurationForSession
                .getProxyConfigurationForSession(activatedTestingSession);
        if (proxyConfigForSession != null) {
            proxyConfigurationForForAllServicesSession = proxyConfigForSession;
            if (activatedTestingSession.getIsProxyUseEnabled()) {
                validateProxyPort();
            }
        } else {
            ProxyConfigurationForSession proxyConf = new ProxyConfigurationForSession();
            proxyConf.setTestingSession(activatedTestingSession);
            proxyConfigurationForForAllServicesSession = proxyConf;
        }
    }

    private void validateProxyPort() {
        // Test if values are not null
        if (proxyConfigurationForForAllServicesSession.getPortRangeLow() == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Your low port value is not informed. Default value has been assigned !");
            proxyConfigurationForForAllServicesSession.setPortRangeLow(1024);
        } else if (proxyConfigurationForForAllServicesSession.getPortRangeHigh() == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Your high port value is not informed. Default value has been assigned !");
            proxyConfigurationForForAllServicesSession.setPortRangeHigh(1024);
        }

        // Get min_proxy_port and max_proxy_port from Proxy
        try {
            int assignedPort = 0, portMin = 0, portMax = 0;
            String min = ProxyManager.getMinProxyPort();
            String max = ProxyManager.getMaxProxyPort();
            if (min != null && max != null) {
                portMin = Integer.valueOf(min);

                portMax = Integer.valueOf(max);

                assignedPort = proxyConfigurationForForAllServicesSession.getPortRangeLow();
                if (assignedPort < portMin || assignedPort > portMax) {
                    proxyConfigurationForForAllServicesSession.setPortRangeLow(portMin);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "Your min proxy port(" + assignedPort + ") is not in the range(" + portMin + "-" + portMax
                                    + ") define in Proxy configuration");
                }
                assignedPort = proxyConfigurationForForAllServicesSession.getPortRangeHigh();
                if (assignedPort < portMin || assignedPort > portMax) {
                    proxyConfigurationForForAllServicesSession.setPortRangeHigh(portMax);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "Your max proxy port(" + assignedPort + ") is not in the range(" + portMin + "-" + portMax
                                    + ") define in Proxy configuration");
                }
            }
            entityManager.flush();
        } catch (RemoteException e) {
            LOG.error("" + e.getMessage());
        }
    }

    @Override
    public void saveProxyConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveProxyConfiguration");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        try {
            entityManager.flush();
            proxyConfigurationForForAllServicesSession.setTestingSession(activatedTestingSession);
            if (proxyConfigurationForForAllServicesSession.getIpForClass() != null) {
                proxyConfigurationForForAllServicesSession = entityManager
                        .merge(proxyConfigurationForForAllServicesSession);
                generateFreeAvailablePortsForProxy();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Proxy configurations updated !");
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No IP is defined !");
            }
        } catch (Exception e) {
            LOG.error("saveProxyConfiguration() : " + e);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem :" + e.getMessage());
        }
    }

    private void generateFreeAvailablePortsForProxy() {
        validateProxyPort();

        if (proxyConfigurationForForAllServicesSession.getPortRangeLow() > proxyConfigurationForForAllServicesSession
                .getPortRangeHigh()) {
            int tmp = proxyConfigurationForForAllServicesSession.getPortRangeLow();
            proxyConfigurationForForAllServicesSession.setPortRangeLow(tmp);
            proxyConfigurationForForAllServicesSession.setPortRangeHigh(tmp);
        }
        entityManager.flush();

        attributeConfigPortProxyForActiveSession();
    }

    @Override
    public void attributeConfigPortProxyForActiveSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("attributeConfigPortProxyForActiveSession");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();

        List<AbstractConfiguration> configurations = AbstractConfiguration
                .GetListOfConfigurationWithPortProxy(activatedTestingSession);

        Set<Integer> usedPorts = AbstractConfiguration.getUsedProxyPorts(activatedTestingSession);
        int i = 0;
        for (AbstractConfiguration conf : configurations) {
            conf.getProxyPortIfNeeded(usedPorts);
            i = i + conf.getProxyPortsErrors();
        }
        if (i > 0) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error assigning ports : " + i + " ports were missing, increase range");
        }
    }

    /**
     * Admin may generate the hosts file clicking on a button. This button uses this method.
     */
    @Override
    public void generateHostsFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateHostsFile");
        }
        StringBuilder hostsContent = new StringBuilder();
        Integer sessionId;

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        if ((params.get("sessionId") == null) || (params.get("sessionId").isEmpty())) {
            sessionId = TestingSession.getSelectedTestingSession().getId();
        } else {
            sessionId = Integer.parseInt(params.get("sessionId"));
        }
        TestingSession ts = TestingSession.GetSessionById(sessionId);
        if (ts == null) {
            LOG.error("The provided sessionId is invalid.");
        } else {

            List<Host> listOfHostsForSession = Host.getListOfHostForASession(ts);

            initializeNetworkConfiguration(ts);
            LOG.debug("networkConfigurationForTestingSession.getHostHeader()  = "
                    + networkConfigurationForTestingSession.getHostHeader());
            if ((networkConfigurationForTestingSession.getHostHeader() != null)
                    && (!networkConfigurationForTestingSession.getHostHeader().isEmpty())) {
                hostsContent.append(networkConfigurationForTestingSession.getHostHeader()).append("\n");
            }

            TreeMap<String, Host> ips = new TreeMap<String, Host>(new IpComparator());
            for (Host h : listOfHostsForSession) {
                if ((h.getIp() != null) && (!h.getIp().isEmpty())) {
                    ips.put(h.getIp(), h);
                }
            }

            for (Map.Entry<String, Host> ip : ips.entrySet()) {
                String cleanIp = StringUtils.rightPad(ip.getKey(), 15, ' ');
                Host host = ip.getValue();
                hostsContent.append(cleanIp).append(" ").append(host.getHostname());
                if ((host.getAlias() != null) && (!host.getAlias().isEmpty())) {
                    hostsContent.append(" ").append(host.getAlias());
                }
                hostsContent.append("\n");
            }
            ReportExporterManager.exportToFile("text/plain", hostsContent.toString().getBytes(StandardCharsets.UTF_8), HOSTS_FILENAME, false);
        }
    }

    /**
     * Show a hosts file (txt) clicking on a link It calls a static method which performing job for all kind of docs (TXT, HL7, DICOM...) This
     * method is in
     * net.ihe.gazelle.common.util.DocumentFileUpload
     */
    @Override
    public void showHostsFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showHostsFile");
        }

        String fileName = ApplicationPreferenceManager.instance().getGazelleDataPath() + java.io.File.separatorChar
                + HOSTS_FILENAME;

        net.ihe.gazelle.common.util.DocumentFileUpload.showFile(fileName);

    }

    /**
     * Save the testingSession's information : Domain name
     */
    @Override
    @Deprecated
    public void saveDomainName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveDomainName");
        }

        entityManager.merge(networkConfigurationForTestingSession);
        entityManager.flush();
    }

    /**
     * Save the testingSession's information : Host header
     */
    @Override
    @Deprecated
    public void saveHostHeader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveHostHeader");
        }

        entityManager.merge(networkConfigurationForTestingSession);
        entityManager.flush();
    }

    /**
     * Save the testingSession's information : DNS header
     */
    @Override
    @Deprecated
    public void saveDnsHeader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveDnsHeader");
        }

        entityManager.merge(networkConfigurationForTestingSession);
        entityManager.flush();
    }

    /**
     * Save the testingSession's information
     */
    @Override
    public void saveNetworkConfigurationForTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveNetworkConfigurationForTestingSession");
        }

        if (networkConfigurationForTestingSession != null) {
            try {
                networkConfigurationForTestingSession = entityManager.merge(networkConfigurationForTestingSession);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO,
                        "Network Configuration for "
                                + networkConfigurationForTestingSession.getTestingSession().getDescription()
                                + " has been successfully saved");
            } catch (Exception e) {
                FacesMessages
                        .instance()
                        .add(StatusMessage.Severity.ERROR, "Failed to save network Configuration : You must configure IPs and DNS before add a Host " +
                                "file header");
                LOG.error("Enable to save the given networkConfigurationForTestingSession for the following reason: "
                        + e.getMessage(), e);
            }

        } else {
            LOG.error("Cannot save the networkConfigurationForTestingSession because it is null");
        }
    }

    /**
     * Admin may generate the DNS file clicking on a button. This button uses this method.
     */
    @Override
    public void generateDNSFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateDNSFile");
        }

        StringBuilder dnsContent = new StringBuilder();
        Integer sessionId;

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if ((params.get("sessionId") == null) || (params.get("sessionId").isEmpty())) {
            sessionId = TestingSession.getSelectedTestingSession().getId();
        } else {
            sessionId = Integer.parseInt(params.get("sessionId"));
        }
        TestingSession ts = TestingSession.GetSessionById(sessionId);
        if (ts == null) {
            LOG.error("The provided sessionId is invalid.");
            return;
        } else {
            List<Host> listOfHostsForSession = Host.getListOfHostForASession(ts);

            initializeNetworkConfiguration(ts);
            if ((networkConfigurationForTestingSession.getDnsHeader() != null)
                    && (!networkConfigurationForTestingSession.getDnsHeader().isEmpty())) {
                dnsContent.append(networkConfigurationForTestingSession.getDnsHeader()).append("\n");
            }

            for (Host h : listOfHostsForSession) {
                if ((h.getIp() != null) && (!h.getIp().isEmpty())) {
                    dnsContent.append(h.getHostname()).append("		IN	A ").append(h.getIp()).append("\n");
                    if (h.getAlias() != null && !h.getAlias().isEmpty()) {
                        dnsContent.append(h.getAlias()).append("		IN	A ").append(h.getIp()).append("\n");
                    }
                }

            }
        }

        String dnsFileName = "zone.";
        if (networkConfigurationForTestingSession.getDomainName() != null) {
            dnsFileName = dnsFileName + networkConfigurationForTestingSession.getDomainName();
        } else {
            dnsFileName = dnsFileName + "enter-domain-name";
        }
        ReportExporterManager.exportToFile(null, dnsContent.toString().getBytes(StandardCharsets.UTF_8), dnsFileName, true);

    }

    /**
     * Admin may need to generate the reverse file for the DNS
     */
    @Override
    public void generateReverseFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateReverseFile");
        }
        StringBuilder reverseFileContent = new StringBuilder();
        TestingSession ts;

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if ((params.get("sessionId") == null) || (params.get("sessionId").isEmpty())) {
            ts = TestingSession.getSelectedTestingSession();
        } else {
            ts = TestingSession.GetSessionById(Integer.parseInt(params.get("sessionId")));
        }

        if (ts == null) {
            LOG.error("The provided sessionId is invalid.");
            return;
        } else {
            List<Host> listOfHostsForSession = Host.getListOfHostForASession(ts);

            initializeNetworkConfiguration(ts);
            if ((networkConfigurationForTestingSession.getReverseHeader() != null)
                    && (!networkConfigurationForTestingSession.getReverseHeader().isEmpty())) {
                reverseFileContent.append(networkConfigurationForTestingSession.getReverseHeader()).append("\n");
            }

            for (Host h : listOfHostsForSession) {
                Scanner sc = null;
                try {
                    if ((h.getIp() != null) && (!h.getIp().isEmpty())) {
                        sc = new Scanner(h.getIp()).useDelimiter("\\.");
                        sc.nextLong();
                        sc.nextLong();
                        long i3 = sc.nextLong();
                        long i4 = sc.nextLong();

                        // 179.200 PTR abbott1.
                        String twoLastReversed = i4 + "." + i3;
                        twoLastReversed = StringUtils.rightPad(twoLastReversed, 7, ' ');
                        reverseFileContent.append(twoLastReversed).append(" PTR ").append(h.getHostname()).append(".\n");
                        if (h.getAlias() != null && !h.getAlias().isEmpty()) {
                            reverseFileContent.append(twoLastReversed).append(" PTR ").append(h.getAlias()).append(".\n");
                        }

                    }
                } catch (RuntimeException e) {
                    LOG.error(e.getMessage());
                } finally {
                    if (sc != null) {
                        sc.close();
                    }
                }
            }
        }

        String reverseFileName = "zone.";
        if (networkConfigurationForTestingSession.getDomainName() != null) {
            reverseFileName = reverseFileName.concat(networkConfigurationForTestingSession.getDomainName()).concat(
                    ".rev");
        } else {
            reverseFileName = reverseFileName.concat("enter-domain-name.rev");
        }
        ReportExporterManager.exportToFile(null, reverseFileContent.toString().getBytes(StandardCharsets.UTF_8), reverseFileName, true);

    }

    /**
     * Show a DNS file (txt) clicking on a link It calls a static method which performing job for all kind of docs (TXT, HL7, DICOM...) This method
     * is in net.ihe.gazelle.common.util.DocumentFileUpload
     */
    @Override
    public void showDNSFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showDNSFile");
        }

    }

    @Override
    public boolean validateGenericOIDInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateGenericOIDInstitution");
        }

        if (this.oidInstitutionForSession == null) {
            return false;
        }
        if ((oidInstitutionForSession.getOidForSession() == null)
                || (oidInstitutionForSession.getOidForSession().trim().length() == 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "OID for this session is null for institution, please fill a correct value");
            return false;
        }
        if ((oidInstitutionForSession.getOidRootValue() == null)
                || (oidInstitutionForSession.getOidRootValue().trim().length() == 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "OID for Institution is null, please fill a correct value");
            return false;
        }

        return true;
    }

    @Override
    public void enableProxyUse() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("enableProxyUse");
        }
        changeProxyUseForATestingSession(true);
    }

    @Override
    public void disableProxyUse() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("disableProxyUse");
        }
        changeProxyUseForATestingSession(false);
    }

    @Override
    public TestingSession getSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestingSession");
        }
        return TestingSession.getSelectedTestingSession();
    }

    private void changeProxyUseForATestingSession(boolean isProxyUseEnabled) {
        TestingSession inTestinSession = TestingSession.getSelectedTestingSession();
        inTestinSession.setIsProxyUseEnabled(isProxyUseEnabled);
        TestingSession.mergeTestingSession(inTestinSession, entityManager);
    }

    @Override
    public void startAllChannels() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("startAllChannels");
        }
        // test is new values has been saved
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        ProxyConfigurationForSession proxyConfigForSession = ProxyConfigurationForSession
                .getProxyConfigurationForSession(activatedTestingSession);
        if ((!proxyConfigurationForForAllServicesSession.getPortRangeHigh().equals(proxyConfigForSession.getPortRangeHigh()))
                || (!proxyConfigurationForForAllServicesSession.getPortRangeLow().equals(proxyConfigForSession
                .getPortRangeLow()))
                || (!proxyConfigurationForForAllServicesSession.getIpForClass().equals(proxyConfigForSession.getIpForClass()))) {
            saveProxyConfiguration();
        }

        try {
            ProxyManager.startAllChannels();

            // Get the number of open channel(s)
            List<AbstractConfiguration> confList = AbstractConfiguration
                    .listAllConfigurationsForATestingSession(TestingSession.getSelectedTestingSession());
            int openChannels = 0;
            for (AbstractConfiguration ac : confList) {
                try {
                    ServerConfiguration serverConfiguration = (ServerConfiguration) ac;
                    if (serverConfiguration.getPort() != null && serverConfiguration.getPort() != -1) {
                        openChannels += 1;
                    }
                } catch (ClassCastException e) {
                    LOG.error("" + e);
                }
            }
            if (openChannels > 1) {
                FacesMessages.instance().add(StatusMessage.Severity.INFO, openChannels + " channels started !");
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Channel started !");
            }
        } catch (Exception e) {
            LOG.error("Failed to start channels", e);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to start channels");
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
