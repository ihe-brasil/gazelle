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

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfiguration;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.model.reversed.AIPOQuery;
import org.ajax4jsf.model.DataVisitResult;
import org.ajax4jsf.model.DataVisitor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Jean-Baptiste Meyer / INRIA Rennes IHE development Project
 * @version 1.0 - 2009, sep
 * @class ConfigurationTypeMappingWithAIPOManager
 * @package net.ihe.gazelle.tm.systems.model
 * @see > jmeyer@irisa.fr - http://www.ihe-europe.org
 */

@AutoCreate

@Name("configurationTypeMappingWithAIPOManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("ConfigurationTypeMappingWithAIPOManagerLocal")
public class ConfigurationTypeMappingWithAIPOManager implements ConfigurationTypeMappingWithAIPOManagerLocal,
        Serializable {

    private static final long serialVersionUID = 17891410983414192L;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ConfigurationTypeMappingWithAIPOManager.class);

    /**
     * entityManager is the interface used to interact with the persistence context.
     */

    private EntityManager entityManager;

    private List<ConfigurationTypeMappedWithAIPO> listOfConfigurationType;

    private ConfigurationTypeMappedWithAIPO selectedConfigurationTypeMapped;

    private ConfigurationTypeWithPortsWSTypeAndSopClass selectedConfigurationType;

    private boolean divAddTypeDisplayed = false;
    private boolean divPortNonSecuredDisplayed = false;
    private boolean divPortSecuredDisplayed = false;
    private boolean divSopClassDisplayed = false;
    private boolean divTransportLayerDisplayed = false;
    private boolean divWSTypeDisplayed = false;
    private boolean divAddAWSTypeDisplayed = false;
    private boolean divTransactionDescriptionDisplayed = false;

    private Domain selectedDomain;
    private Actor selectedActor;
    private IntegrationProfile selectedIntegrationProfile;

    private WebServiceType webServiceTypeSelected;

    private FilterDataModel<ConfigurationTypeMappedWithAIPO> foundConfigurationTypeMappedWithAIPODataModels;

    @Override
    public FilterDataModel<ConfigurationTypeMappedWithAIPO> getFoundConfigurationTypeMappedWithAIPODataModels() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundConfigurationTypeMappedWithAIPODataModels");
        }
        if (foundConfigurationTypeMappedWithAIPODataModels == null) {
            foundConfigurationTypeMappedWithAIPODataModels = new FilterDataModel<ConfigurationTypeMappedWithAIPO>(
                    new Filter<ConfigurationTypeMappedWithAIPO>(getCriterions())) {
                @Override
                protected Object getId(ConfigurationTypeMappedWithAIPO t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return foundConfigurationTypeMappedWithAIPODataModels;
    }

    @Override
    public void setFoundConfigurationTypeMappedWithAIPODataModels(
            FilterDataModel<ConfigurationTypeMappedWithAIPO> foundConfigurationTypeMappedWithAIPODataModels) {
        this.foundConfigurationTypeMappedWithAIPODataModels = foundConfigurationTypeMappedWithAIPODataModels;
    }

    private HQLCriterionsForFilter<ConfigurationTypeMappedWithAIPO> getCriterions() {
        ConfigurationTypeMappedWithAIPOQuery query = new ConfigurationTypeMappedWithAIPOQuery();
        HQLCriterionsForFilter<ConfigurationTypeMappedWithAIPO> result = query.getHQLCriterionsForFilter();
        TMCriterions.addAIPOCriterions(result, query.actorIntegrationProfileOption());
        return result;
    }

    @Override
    public Domain getSelectedDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomain");
        }
        return selectedDomain;
    }

    @Override
    public void setSelectedDomain(Domain selectedDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomain");
        }
        this.selectedDomain = selectedDomain;
    }

    @Override
    public Actor getSelectedActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActor");
        }
        return selectedActor;
    }

    @Override
    public void setSelectedActor(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActor");
        }
        this.selectedActor = selectedActor;
    }

    @Override
    public IntegrationProfile getSelectedIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfile");
        }
        return selectedIntegrationProfile;
    }

    @Override
    public void setSelectedIntegrationProfile(IntegrationProfile selectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfile");
        }
        this.selectedIntegrationProfile = selectedIntegrationProfile;
    }

    @Override
    public boolean isDivAddTypeDisplayed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDivAddTypeDisplayed");
        }
        return divAddTypeDisplayed;
    }

    @Override
    public void setDivAddTypeDisplayed(boolean divAddTypeDisplayed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDivAddTypeDisplayed");
        }
        this.divAddTypeDisplayed = divAddTypeDisplayed;
    }

    @Override
    public List<ConfigurationTypeMappedWithAIPO> getListOfConfigurationType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfConfigurationType");
        }

        if (listOfConfigurationType != null) {

        }

        return listOfConfigurationType;
    }

    @Override
    public void setListOfConfigurationType(List<ConfigurationTypeMappedWithAIPO> listOfConfigurationType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListOfConfigurationType");
        }
        this.listOfConfigurationType = listOfConfigurationType;
    }

    @Override
    public ConfigurationTypeMappedWithAIPO getSelectedConfigurationTypeMapped() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedConfigurationTypeMapped");
        }
        return selectedConfigurationTypeMapped;
    }

    @Override
    public void setSelectedConfigurationTypeMapped(ConfigurationTypeMappedWithAIPO selectedConfigurationTypeMapped) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedConfigurationTypeMapped");
        }
        this.selectedConfigurationTypeMapped = selectedConfigurationTypeMapped;
    }

    @Override
    public ConfigurationTypeWithPortsWSTypeAndSopClass getSelectedConfigurationType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedConfigurationType");
        }
        return selectedConfigurationType;
    }

    @Override
    public void setSelectedConfigurationType(ConfigurationTypeWithPortsWSTypeAndSopClass selectedConfigurationType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedConfigurationType");
        }
        this.selectedConfigurationType = selectedConfigurationType;
    }

    @Override
    public void setSelectedConfigurationTypeMappedAndInitConfType(
            ConfigurationTypeMappedWithAIPO inConfigurationTypeMappedWithAIPO) {

        this.setSelectedConfigurationTypeMapped(inConfigurationTypeMappedWithAIPO);
        this.selectedConfigurationType = new ConfigurationTypeWithPortsWSTypeAndSopClass();
        this.divAddTypeDisplayed = true;
    }

    @Override
    public boolean isDivPortNonSecuredDisplayed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDivPortNonSecuredDisplayed");
        }
        return divPortNonSecuredDisplayed;
    }

    @Override
    public void setDivPortNonSecuredDisplayed(boolean divPortNonSecuredDisplayed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDivPortNonSecuredDisplayed");
        }
        this.divPortNonSecuredDisplayed = divPortNonSecuredDisplayed;
    }

    @Override
    public boolean isDivPortSecuredDisplayed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDivPortSecuredDisplayed");
        }
        return divPortSecuredDisplayed;
    }

    @Override
    public void setDivPortSecuredDisplayed(boolean divPortSecuredDisplayed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDivPortSecuredDisplayed");
        }
        this.divPortSecuredDisplayed = divPortSecuredDisplayed;
    }

    @Override
    public boolean isDivSopClassDisplayed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDivSopClassDisplayed");
        }
        return divSopClassDisplayed;
    }

    @Override
    public void setDivSopClassDisplayed(boolean divSopClassDisplayed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDivSopClassDisplayed");
        }
        this.divSopClassDisplayed = divSopClassDisplayed;
    }

    @Override
    public boolean isDivWSTypeDisplayed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDivWSTypeDisplayed");
        }
        return divWSTypeDisplayed;
    }

    @Override
    public void setDivWSTypeDisplayed(boolean divWSTypeDisplayed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDivWSTypeDisplayed");
        }
        this.divWSTypeDisplayed = divWSTypeDisplayed;
    }

    @Override
    public boolean isDivTransportLayerDisplayed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDivTransportLayerDisplayed");
        }
        return divTransportLayerDisplayed;
    }

    @Override
    public void setDivTransportLayerDisplayed(boolean divTransportLayerDisplayed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDivTransportLayerDisplayed");
        }
        this.divTransportLayerDisplayed = divTransportLayerDisplayed;
    }
    @Override
    public boolean isDivTransactionDescriptionDisplayed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDivTransactionDescriptionDisplayed");
        }
        return divTransactionDescriptionDisplayed;
    }
    @Override
    public void setDivTransactionDescriptionDisplayed(boolean divTransactionDescriptionDisplayed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDivTransactionDescriptionDisplayed");
        }
        this.divTransactionDescriptionDisplayed = divTransactionDescriptionDisplayed;
    }


    @Override
    public void addConfigurationTypeToSelectedConfTypeAIPO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addConfigurationTypeToSelectedConfTypeAIPO");
        }
        try {

            entityManager = EntityManagerService.provideEntityManager();

            if ((selectedConfigurationType == null) || (selectedConfigurationTypeMapped == null)) {
                return;
            }

            selectedConfigurationType = entityManager.merge(selectedConfigurationType);

            if (selectedConfigurationTypeMapped.getListOfConfigurationTypes() == null) {
                selectedConfigurationTypeMapped
                        .setListOfConfigurationTypes(new ArrayList<ConfigurationTypeWithPortsWSTypeAndSopClass>());
            }

            selectedConfigurationTypeMapped.getListOfConfigurationTypes().add(selectedConfigurationType);

            selectedConfigurationTypeMapped = entityManager.merge(selectedConfigurationTypeMapped);

            entityManager.flush();

            selectedConfigurationType = null;

            StatusMessages.instance().add("Configuration persisted");
        } catch (Exception e) {
            StatusMessages.instance().add("Problem adding config persisted : " + e.getMessage());
        }
    }

    @Override
    public void modifySingleSelectedConfigurationType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifySingleSelectedConfigurationType");
        }
        if ((this.selectedConfigurationType != null) && (this.selectedConfigurationTypeMapped != null)) {
            entityManager = EntityManagerService.provideEntityManager();
            int id = this.selectedConfigurationType.getId();
            ConfigurationTypeWithPortsWSTypeAndSopClass newCT = new ConfigurationTypeWithPortsWSTypeAndSopClass(
                    this.selectedConfigurationType);
            this.selectedConfigurationType = entityManager.find(ConfigurationTypeWithPortsWSTypeAndSopClass.class, id);

            this.selectedConfigurationTypeMapped.getListOfConfigurationTypes().remove(this.selectedConfigurationType);
            for (ConfigurationTypeWithPortsWSTypeAndSopClass sc : this.selectedConfigurationTypeMapped
                    .getListOfConfigurationTypes()) {
                if (sc.getId().equals(this.selectedConfigurationType.getId())) {
                    this.selectedConfigurationTypeMapped.getListOfConfigurationTypes().remove(sc);
                }
            }

            this.selectedConfigurationTypeMapped = entityManager.merge(this.selectedConfigurationTypeMapped);
            entityManager.flush();

            List<ConfigurationTypeMappedWithAIPO> listOfConf = ConfigurationTypeMappedWithAIPO
                    .getConfigurationTypeMappingWithAIPOFiltered(null, null, this.selectedConfigurationType);
            if (listOfConf.size() == 0) {
                entityManager.remove(this.selectedConfigurationType);
            }
            entityManager.flush();

            newCT = entityManager.merge(newCT);
            entityManager.flush();
            selectedConfigurationTypeMapped.getListOfConfigurationTypes().add(newCT);
            selectedConfigurationTypeMapped = entityManager.merge(selectedConfigurationTypeMapped);
            this.selectedConfigurationType = null;
            entityManager.flush();
            StatusMessages.instance().add("Configuration persisted");
        }
    }

    @Override
    public void modifySelectedConfigurationTypeInAllConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifySelectedConfigurationTypeInAllConfiguration");
        }
        if ((this.selectedConfigurationType != null) && (this.selectedConfigurationTypeMapped != null)) {
            entityManager = EntityManagerService.provideEntityManager();
            this.selectedConfigurationType = entityManager.merge(this.selectedConfigurationType);
            entityManager.flush();
            selectedConfigurationType = null;
            StatusMessages.instance().add("Configuration persisted");
        }
    }

    @Override
    public void listAllConfigurationsMappedWithAIPO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listAllConfigurationsMappedWithAIPO");
        }

        entityManager = EntityManagerService.provideEntityManager();

        listOfConfigurationType = ConfigurationTypeMappedWithAIPO.getAllConfigurationTypeMappedWithAIPO();

        List<ActorIntegrationProfileOption> listOfAIPO = ActorIntegrationProfileOption
                .listAllActorIntegrationProfileOption();

        HashMap<ActorIntegrationProfileOption, ConfigurationTypeMappedWithAIPO> mapOfAIPOAndConf = new HashMap<ActorIntegrationProfileOption,
                ConfigurationTypeMappedWithAIPO>();

        for (ConfigurationTypeMappedWithAIPO confTypeAIPO : listOfConfigurationType) {
            mapOfAIPOAndConf.put(confTypeAIPO.getActorIntegrationProfileOption(), confTypeAIPO);
        }

        for (ActorIntegrationProfileOption aipo : listOfAIPO) {
            if (!mapOfAIPOAndConf.containsKey(aipo)) {
                ConfigurationTypeMappedWithAIPO confTypeMappedWAIPO = new ConfigurationTypeMappedWithAIPO();
                confTypeMappedWAIPO.setActorIntegrationProfileOption(aipo);

                confTypeMappedWAIPO = entityManager.merge(confTypeMappedWAIPO);

                listOfConfigurationType.add(confTypeMappedWAIPO);
            }
        }

        entityManager.flush();

    }

    @Override
    public void changeTypeOfConfig() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeTypeOfConfig");
        }

        changeTypeOfConfig(selectedConfigurationType);
    }

    @Override
    public void selectConfTypeAndChangeTypeOfConfig(ConfigurationTypeMappedWithAIPO ctm,
                                                    ConfigurationTypeWithPortsWSTypeAndSopClass ctms) {
        this.selectConfMappedWithAIPOAndConfigurationType(ctm, ctms);
        this.changeTypeOfConfig();
    }

    @Override
    public void changeTypeOfConfig(ConfigurationTypeWithPortsWSTypeAndSopClass inConfigurationType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeTypeOfConfig");
        }

        if (inConfigurationType == null) {
            return;
        }
        if (inConfigurationType.getConfigurationType() == null) {
            return;
        }

        try {
            if (inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(DicomSCUConfiguration.class))
                    && inConfigurationType.getSopClass() != null && !inConfigurationType.getSopClass().getKeyword().equals("STORAGE COMMITMENT")) {
                divPortNonSecuredDisplayed = false;
                divPortSecuredDisplayed = false;
                divSopClassDisplayed = true;
                divTransportLayerDisplayed = false;
                divWSTypeDisplayed = false;
                divTransactionDescriptionDisplayed = false;
            } else if (inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(DicomSCPConfiguration.class))
                    || inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(DicomSCUConfiguration.class))) {
                divPortNonSecuredDisplayed = true;
                divPortSecuredDisplayed = true;
                divSopClassDisplayed = true;
                divTransportLayerDisplayed = false;
                divWSTypeDisplayed = false;
                divTransactionDescriptionDisplayed = false;
            } else if (inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(HL7V2InitiatorConfiguration.class))
                    || inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(HL7V3InitiatorConfiguration.class))) {
                divPortNonSecuredDisplayed = false;
                divPortSecuredDisplayed = false;
                divSopClassDisplayed = false;
                divTransportLayerDisplayed = false;
                divWSTypeDisplayed = false;
                divTransactionDescriptionDisplayed = false;
            } else if (inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(HL7V2ResponderConfiguration.class))) {
                divPortNonSecuredDisplayed = true;
                divPortSecuredDisplayed = true;
                divSopClassDisplayed = false;
                divTransportLayerDisplayed = false;
                divWSTypeDisplayed = false;
                divTransactionDescriptionDisplayed = false;
            } else if (inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(SyslogConfiguration.class))) {
                divPortNonSecuredDisplayed = true;
                divPortSecuredDisplayed = true;
                divSopClassDisplayed = false;
                divTransportLayerDisplayed = true;
                divWSTypeDisplayed = false;
                divTransactionDescriptionDisplayed = false;
            } else if (inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(WebServiceConfiguration.class))) {
                divPortNonSecuredDisplayed = true;
                divPortSecuredDisplayed = true;
                divSopClassDisplayed = false;
                divTransportLayerDisplayed = false;
                divWSTypeDisplayed = true;
                divTransactionDescriptionDisplayed = false;
            } else if (inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(HL7V3ResponderConfiguration.class))) {
                divPortNonSecuredDisplayed = true;
                divPortSecuredDisplayed = true;
                divSopClassDisplayed = false;
                divTransportLayerDisplayed = false;
                divWSTypeDisplayed = true;
                divTransactionDescriptionDisplayed = false;
            } else if (inConfigurationType.getConfigurationType().equals(
                    ConfigurationType.GetConfigurationTypeByClass(RawConfiguration.class))) {
                divPortNonSecuredDisplayed = true;
                divPortSecuredDisplayed = true;
                divSopClassDisplayed = false;
                divTransportLayerDisplayed = false;
                divWSTypeDisplayed = false;
                divTransactionDescriptionDisplayed = true;
            }else {
                divPortNonSecuredDisplayed = true;
                divPortSecuredDisplayed = true;
                divSopClassDisplayed = true;
                divTransportLayerDisplayed = true;
                divWSTypeDisplayed = true;
                divTransactionDescriptionDisplayed = false;
            }
        } catch (ClassNotFoundException e) {

            LOG.error("", e);
        }
    }

    @Override
    public String displayConfigurationType(ConfigurationTypeWithPortsWSTypeAndSopClass inConfigurationType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayConfigurationType");
        }
        if (inConfigurationType == null) {
            return "";
        }
        StringBuffer bufferToReturn = new StringBuffer();

        changeTypeOfConfig(inConfigurationType);

        if (divTransportLayerDisplayed && (inConfigurationType.getTransportLayer() != null) && (!inConfigurationType.getTransportLayer().getKeyword
                ().isEmpty())) {
            bufferToReturn.append("(" + inConfigurationType.getTransportLayer().getKeyword() + ")/");
        }
        if (divPortNonSecuredDisplayed && (inConfigurationType.getPortNonSecure() != null)) {
            bufferToReturn.append("Port(" + inConfigurationType.getPortNonSecure() + ")/");
        }

        if (divPortSecuredDisplayed && (inConfigurationType.getPortSecure() != null)) {
            bufferToReturn.append("Port sec(" + inConfigurationType.getPortSecure() + ")/");
        }

        if (divSopClassDisplayed && (inConfigurationType.getSopClass() != null)) {
            bufferToReturn.append("SopClass(" + inConfigurationType.getSopClass().getKeyword() + ")");
        }

        if (divTransactionDescriptionDisplayed && (inConfigurationType.getTransactionDescription() != null)) {
            bufferToReturn.append("Transaction Description(" + inConfigurationType.getTransactionDescription()+ ")");
        }

        // *
        if (divWSTypeDisplayed && (inConfigurationType.getWebServiceType() != null) && (inConfigurationType.getWebServiceType().getProfile() !=
                null)) {
            bufferToReturn.append("WSType(" + inConfigurationType.getWebServiceType().getProfile().getKeyword() + ")");
        }
        // */

        if (divWSTypeDisplayed && (inConfigurationType.getWsTRansactionUsage() != null)) {
            bufferToReturn.append("WSUsage("
                    + inConfigurationType.getWsTRansactionUsage().getTransaction().getKeyword() + ":"
                    + inConfigurationType.getWsTRansactionUsage().getUsage() + ")");
        }

        if ((inConfigurationType.getComment() != null) && (inConfigurationType.getComment().trim().length() > 0)) {
            bufferToReturn.append("(" + inConfigurationType.getComment() + ")");
        }

        return bufferToReturn.toString();

    }

    @Override
    public boolean validatePort(Integer port, String control) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validatePort");
        }
        if (port == null) {
            if (control != null) {
                StatusMessages.instance().addToControl(control, "The value shouldn't be empty");
            }
            return false;
        } else {
            if (port < 80) {
                if (control != null) {
                    StatusMessages.instance().addToControl(control, "The port value should be between 80 and 65636");
                }
                return false;

            } else if (port > 65635) {
                if (control != null) {
                    StatusMessages.instance().addToControl(control, "The port value should be between 80 and 65636");
                }
                return false;
            } else {
                return true;
            }

        }

    }

    @Override
    public void closeAddATypePanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeAddATypePanel");
        }

        divAddTypeDisplayed = false;
        divAddAWSTypeDisplayed = false;
    }

    @Override
    public void closeWSTypeEditionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("closeWSTypeEditionPanel");
        }
        divAddAWSTypeDisplayed = false;
    }

    @Override
    public void addConfigurationTypeToSelectedConfTypeAIPOToAllSelection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addConfigurationTypeToSelectedConfTypeAIPOToAllSelection");
        }
        entityManager = EntityManagerService.provideEntityManager();

        if (selectedConfigurationType == null) {
            return;
        }

        selectedConfigurationType = entityManager.merge(selectedConfigurationType);

        entityManager.flush();

        this.foundConfigurationTypeMappedWithAIPODataModels.walk(null, new DataVisitor() {

            @Override
            public DataVisitResult process(FacesContext arg0, Object arg1, Object arg2) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("process");
                }
                EntityManager em = EntityManagerService.provideEntityManager();
                ConfigurationTypeMappedWithAIPO confTypeMappedWithAIPO = em.find(ConfigurationTypeMappedWithAIPO.class, arg1);
                confTypeMappedWithAIPO.getListOfConfigurationTypes().add(selectedConfigurationType);
                entityManager.merge(confTypeMappedWithAIPO);
                return DataVisitResult.CONTINUE;
            }
        }, null, null);

        entityManager.flush();
        closeAddATypePanel();
    }

    @Override
    public void listConfigurationMappedWithAIPOFiltered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listConfigurationMappedWithAIPOFiltered");
        }
    }

    @Override
    public void addForThisSelection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addForThisSelection");
        }
        selectedConfigurationTypeMapped = null;
        this.selectedConfigurationType = new ConfigurationTypeWithPortsWSTypeAndSopClass();
    }

    @Override
    public void removeTypeFromList(ConfigurationTypeMappedWithAIPO inConfigurationTypeMapped,
                                   ConfigurationTypeWithPortsWSTypeAndSopClass inConfigurationType) {
        if ((inConfigurationTypeMapped == null) || (inConfigurationType == null)
                || (inConfigurationTypeMapped.getListOfConfigurationTypes().size() < 1)) {
            return;
        }

        entityManager = EntityManagerService.provideEntityManager();

        ConfigurationTypeMappedWithAIPO configToUse = entityManager.find(ConfigurationTypeMappedWithAIPO.class,
                inConfigurationTypeMapped.getId());

        ConfigurationTypeWithPortsWSTypeAndSopClass confWithPortsToUse = entityManager.find(
                ConfigurationTypeWithPortsWSTypeAndSopClass.class, inConfigurationType.getId());

        for (ConfigurationTypeWithPortsWSTypeAndSopClass currentConfTypeWithPorts : configToUse
                .getListOfConfigurationTypes()) {
            if (currentConfTypeWithPorts.getId().equals(confWithPortsToUse.getId())) {
                try {

                    configToUse.getListOfConfigurationTypes().remove(currentConfTypeWithPorts);
                    configToUse = entityManager.merge(configToUse);
                    List<ConfigurationTypeMappedWithAIPO> listOfConf = ConfigurationTypeMappedWithAIPO
                            .getConfigurationTypeMappingWithAIPOFiltered(null, null, currentConfTypeWithPorts);
                    if (listOfConf.size() == 0) {
                        entityManager.remove(currentConfTypeWithPorts);
                    }

                    entityManager.flush();

                    break;
                } catch (Exception e) {

                }
            }
        }

    }

    @Override
    public void removeTypeFromSelection(ConfigurationTypeWithPortsWSTypeAndSopClass inConfigurationType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeTypeFromSelection");
        }
        if (inConfigurationType == null) {
            return;
        }

        entityManager = EntityManagerService.provideEntityManager();

        if (inConfigurationType.getId() == null) {
            ConfigurationTypeWithPortsWSTypeAndSopClassQuery query = new ConfigurationTypeWithPortsWSTypeAndSopClassQuery();

            query.configurationType().eqIfValueNotNull(inConfigurationType.getConfigurationType());
            query.portSecure().eqIfValueNotNull(inConfigurationType.getPortSecure());
            query.portNonSecure().eqIfValueNotNull(inConfigurationType.getPortNonSecure());
            query.webServiceType().eqIfValueNotNull(inConfigurationType.getWebServiceType());
            query.sopClass().eqIfValueNotNull(inConfigurationType.getSopClass());
            query.transportLayer().eqIfValueNotNull(inConfigurationType.getTransportLayer());

            inConfigurationType = query.getUniqueResult();
        }
        if (inConfigurationType.getId() == null) {
            return;
        }
        for (ConfigurationTypeMappedWithAIPO confTypeMappedWithAIPO : listOfConfigurationType) {
            if (confTypeMappedWithAIPO.getListOfConfigurationTypes().contains(inConfigurationType)) {
                List<ConfigurationTypeWithPortsWSTypeAndSopClass> listSaved = new ArrayList<ConfigurationTypeWithPortsWSTypeAndSopClass>(
                        confTypeMappedWithAIPO.getListOfConfigurationTypes());
                for (ConfigurationTypeWithPortsWSTypeAndSopClass confTWPWS : confTypeMappedWithAIPO
                        .getListOfConfigurationTypes()) {
                    if (confTWPWS.equals(inConfigurationType)) {
                        listSaved.remove(inConfigurationType);
                    }
                }

                listSaved = entityManager.merge(listSaved);
            }
        }

        entityManager.flush();
    }

    @Override
    public boolean isDivAddAWSTypeDisplayed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDivAddAWSTypeDisplayed");
        }
        return divAddAWSTypeDisplayed;
    }

    @Override
    public void setDivAddAWSTypeDisplayed(boolean divAddAWSTypeDisplayed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDivAddAWSTypeDisplayed");
        }
        this.divAddAWSTypeDisplayed = divAddAWSTypeDisplayed;
    }

    @Override
    public WebServiceType getWebServiceTypeSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWebServiceTypeSelected");
        }
        return webServiceTypeSelected;
    }

    @Override
    public void setWebServiceTypeSelected(WebServiceType webServiceTypeSelected) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setWebServiceTypeSelected");
        }
        this.webServiceTypeSelected = webServiceTypeSelected;
    }

    @Override
    public void displayWebServiceTypePanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayWebServiceTypePanel");
        }
        divAddTypeDisplayed = false;
        divAddAWSTypeDisplayed = true;

        webServiceTypeSelected = new WebServiceType();
    }

    @Override
    public void updateWebServiceTypeSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateWebServiceTypeSelected");
        }
        if (webServiceTypeSelected == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "webServiceType is null cancelling action...");
            divAddAWSTypeDisplayed = false;
        }

        if (webServiceTypeSelected != null && webServiceTypeSelected.getProfile() != null) {
            entityManager = EntityManagerService.provideEntityManager();
            webServiceTypeSelected = entityManager.merge(webServiceTypeSelected);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO,
                    "webServiceType " + webServiceTypeSelected.getProfile().getKeyword() + " added");
            divAddAWSTypeDisplayed = false;
        }
    }

    @Override
    public void resetActorAndIP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetActorAndIP");
        }
        selectedActor = null;
        selectedIntegrationProfile = null;
    }

    @Override
    public List<ConfigurationTypeMappedWithAIPO> getListOfConfigurationTypeWhereSelectConfTypeUsed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfConfigurationTypeWhereSelectConfTypeUsed");
        }
        if ((selectedConfigurationType == null) || (selectedConfigurationType.getId() == null)) {
            return null;
        }

        return ConfigurationTypeMappedWithAIPO.getConfigurationTypeMappingWithAIPOFiltered(null, null,
                selectedConfigurationType);
    }

    @Override
    public void editConfigurationType(ConfigurationTypeWithPortsWSTypeAndSopClass inSelectedConfigurationType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editConfigurationType");
        }
        selectedConfigurationType = inSelectedConfigurationType;
        divAddTypeDisplayed = true;
    }

    @Override
    public List<IntegrationProfile> listIntegrationProfilesNotUsedForWebServiceType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listIntegrationProfilesNotUsedForWebServiceType");
        }
        List<IntegrationProfile> lip = IntegrationProfile.getAllIntegrationProfile();
        List<IntegrationProfile> lipused = new ArrayList<IntegrationProfile>();
        List<WebServiceType> lwst = WebServiceType.listAllWebServices();
        for (WebServiceType ct : lwst) {
            lipused.add(ct.getProfile());
        }
        lip.removeAll(lipused);
        return lip;
    }

    @Override
    public void selectConfMappedWithAIPOAndConfigurationType(ConfigurationTypeMappedWithAIPO ctm,
                                                             ConfigurationTypeWithPortsWSTypeAndSopClass ctms) {
        this.selectedConfigurationType = ctms;
        this.selectedConfigurationTypeMapped = ctm;
    }

    @Override
    public void removeTypeSelectedFromListSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeTypeSelectedFromListSelected");
        }
        this.removeTypeFromList(selectedConfigurationTypeMapped, selectedConfigurationType);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Selected configuration type was deleted.");
        this.foundConfigurationTypeMappedWithAIPODataModels.resetCache();
    }

    @Override
    public List<WSTransactionUsage> allowedWSTransactionUsages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("allowedWSTransactionUsages");
        }
        if (this.selectedConfigurationTypeMapped != null) {
            return WSTransactionUsage.getWSTransactionUsageFiltered(null, null, null,
                    this.selectedConfigurationTypeMapped.getActorIntegrationProfileOption()
                            .getActorIntegrationProfile());
        }
        return null;
    }

    @Override
    public void addMissing() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addMissing");
        }
        // get aipos ids without configuration
        AIPOQuery aipoQuery = new AIPOQuery();
        aipoQuery.configurationTypeMappedWithAIPO().isEmpty();
        List<Integer> aipoIds = aipoQuery.id().getListDistinct();

        // create configurations
        ActorIntegrationProfileOptionQuery actorIntegrationProfileOptionQuery = new ActorIntegrationProfileOptionQuery();
        actorIntegrationProfileOptionQuery.id().in(aipoIds);
        List<ActorIntegrationProfileOption> aipos = actorIntegrationProfileOptionQuery.getList();

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        for (ActorIntegrationProfileOption aipo : aipos) {
            ConfigurationTypeMappedWithAIPO confTypeMappedWAIPO = new ConfigurationTypeMappedWithAIPO();
            confTypeMappedWAIPO.setActorIntegrationProfileOption(aipo);

            confTypeMappedWAIPO = entityManager.merge(confTypeMappedWAIPO);
        }
        entityManager.flush();
    }

    @Override
    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

}