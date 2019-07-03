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

package net.ihe.gazelle.objects.admin;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.tree.GazelleTreeNodeImpl;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.objects.model.*;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.datamodel.ObjectTypeDataModel;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionRegistrationStatus;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author Abderrazek Boufahja > INRIA Rennes IHE development Project
 */

@Name("objectTypeManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface(value = "ObjectTypeManagerLocal")
public class ObjectTypeManager extends ObjectFileTypeManager implements ObjectTypeManagerLocal, Serializable {

    private static final long serialVersionUID = 9096149345318123456L;

    // ~ Logger ///////////////////////////////////////////////////////////////////////////

    private static final Logger LOG = LoggerFactory.getLogger(ObjectTypeManager.class);

    // ~ Attribute ///////////////////////////////////////////////////////////////////////

    private ObjectType selectedObjectType;

    private IntegrationProfile selectedIntegrationProfileForCreator;

    private Actor selectedActorForCreator;

    private IntegrationProfileOption selectedIntegrationProfileOptionForCreator;

    private String creatorDescription;

    private IntegrationProfile selectedIntegrationProfileForReader;

    private Actor selectedActorForReader;

    private IntegrationProfileOption selectedIntegrationProfileOptionForReader;

    private String readerDescription;

    private ObjectFileType selectedFileType;

    private String selectedFileDescription;

    private String createdAttributeType;

    private String createdAttributeDescription;

    private ObjectCreator selectedCreator;

    private ObjectReader selectedReader;

    private ObjectFile selectedObjectFile;

    private ObjectAttribute selectedObjectAttribute;

    private int selectedFileMinimum;

    private int selectedFileMaximum;

    private Domain selectedDomainForCreators;

    private Domain selectedDomainForReaders;

    private ObjectTypeStatus selectedObjectTypeStatus;

    private ObjectTypeDataModel foundObjectTypes;

    // ~ getter && setter ////////////////////////////////////////////////////////////////

    @Override
    public ObjectTypeDataModel getFoundObjectTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundObjectTypes");
        }
        return foundObjectTypes;
    }

    @Override
    public void setFoundObjectTypes(ObjectTypeDataModel foundObjectTypes) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFoundObjectTypes");
        }
        this.foundObjectTypes = foundObjectTypes;
    }

    @Override
    public ObjectTypeStatus getSelectedObjectTypeStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectTypeStatus");
        }
        return selectedObjectTypeStatus;
    }

    @Override
    public void setSelectedObjectTypeStatus(ObjectTypeStatus selectedObjectTypeStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectTypeStatus");
        }
        this.selectedObjectTypeStatus = selectedObjectTypeStatus;
    }

    @Override
    public Domain getSelectedDomainForCreators() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomainForCreators");
        }
        return selectedDomainForCreators;
    }

    @Override
    public void setSelectedDomainForCreators(Domain selectedDomainForCreators) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomainForCreators");
        }
        this.selectedDomainForCreators = selectedDomainForCreators;
    }

    @Override
    public Domain getSelectedDomainForReaders() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomainForReaders");
        }
        return selectedDomainForReaders;
    }

    @Override
    public void setSelectedDomainForReaders(Domain selectedDomainForReaders) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomainForReaders");
        }
        this.selectedDomainForReaders = selectedDomainForReaders;
    }

    @Override
    public int getSelectedFileMinimum() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedFileMinimum");
        }
        return selectedFileMinimum;
    }

    @Override
    public void setSelectedFileMinimum(int selectedFileMinimum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedFileMinimum");
        }
        this.selectedFileMinimum = selectedFileMinimum;
    }

    @Override
    public int getSelectedFileMaximum() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedFileMaximum");
        }
        return selectedFileMaximum;
    }

    @Override
    public void setSelectedFileMaximum(int selectedFileMaximum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedFileMaximum");
        }
        this.selectedFileMaximum = selectedFileMaximum;
    }

    @Override
    public ObjectType getSelectedObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectType");
        }
        return selectedObjectType;
    }

    @Override
    public void setSelectedObjectType(ObjectType selectedObjectType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectType");
        }
        this.selectedObjectType = selectedObjectType;
    }

    @Override
    public IntegrationProfile getSelectedIntegrationProfileForCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfileForCreator");
        }
        return selectedIntegrationProfileForCreator;
    }

    @Override
    public void setSelectedIntegrationProfileForCreator(IntegrationProfile selectedIntegrationProfileForCreator) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfileForCreator");
        }
        this.selectedIntegrationProfileForCreator = selectedIntegrationProfileForCreator;
    }

    @Override
    public Actor getSelectedActorForCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActorForCreator");
        }
        return selectedActorForCreator;
    }

    @Override
    public void setSelectedActorForCreator(Actor selectedActorForCreator) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActorForCreator");
        }
        this.selectedActorForCreator = selectedActorForCreator;
    }

    @Override
    public IntegrationProfileOption getSelectedIntegrationProfileOptionForCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfileOptionForCreator");
        }
        return selectedIntegrationProfileOptionForCreator;
    }

    @Override
    public void setSelectedIntegrationProfileOptionForCreator(
            IntegrationProfileOption selectedIntegrationProfileOptionForCreator) {
        this.selectedIntegrationProfileOptionForCreator = selectedIntegrationProfileOptionForCreator;
    }

    @Override
    public String getCreatorDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCreatorDescription");
        }
        return creatorDescription;
    }

    @Override
    public void setCreatorDescription(String creatorDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCreatorDescription");
        }
        this.creatorDescription = creatorDescription;
    }

    @Override
    public IntegrationProfile getSelectedIntegrationProfileForReader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfileForReader");
        }
        return selectedIntegrationProfileForReader;
    }

    @Override
    public void setSelectedIntegrationProfileForReader(IntegrationProfile selectedIntegrationProfileForReader) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfileForReader");
        }
        this.selectedIntegrationProfileForReader = selectedIntegrationProfileForReader;
    }

    @Override
    public Actor getSelectedActorForReader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActorForReader");
        }
        return selectedActorForReader;
    }

    @Override
    public void setSelectedActorForReader(Actor selectedActorForReader) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActorForReader");
        }
        this.selectedActorForReader = selectedActorForReader;
    }

    @Override
    public IntegrationProfileOption getSelectedIntegrationProfileOptionForReader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfileOptionForReader");
        }
        return selectedIntegrationProfileOptionForReader;
    }

    @Override
    public void setSelectedIntegrationProfileOptionForReader(
            IntegrationProfileOption selectedIntegrationProfileOptionForReader) {
        this.selectedIntegrationProfileOptionForReader = selectedIntegrationProfileOptionForReader;
    }

    @Override
    public String getReaderDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReaderDescription");
        }
        return readerDescription;
    }

    @Override
    public void setReaderDescription(String readerDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReaderDescription");
        }
        this.readerDescription = readerDescription;
    }

    @Override
    public ObjectFileType getSelectedFileType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedFileType");
        }
        return selectedFileType;
    }

    @Override
    public void setSelectedFileType(ObjectFileType selectedFileType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedFileType");
        }
        this.selectedFileType = selectedFileType;
    }

    @Override
    public String getSelectedFileDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedFileDescription");
        }
        return selectedFileDescription;
    }

    @Override
    public void setSelectedFileDescription(String selectedFileDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedFileDescription");
        }
        this.selectedFileDescription = selectedFileDescription;
    }

    @Override
    public String getCreatedAttributeType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCreatedAttributeType");
        }
        return createdAttributeType;
    }

    @Override
    public void setCreatedAttributeType(String createdAttributeType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCreatedAttributeType");
        }
        this.createdAttributeType = createdAttributeType;
    }

    @Override
    public String getCreatedAttributeDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCreatedAttributeDescription");
        }
        return createdAttributeDescription;
    }

    @Override
    public void setCreatedAttributeDescription(String createdAttributeDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCreatedAttributeDescription");
        }
        this.createdAttributeDescription = createdAttributeDescription;
    }

    @Override
    public ObjectCreator getSelectedCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedCreator");
        }
        return selectedCreator;
    }

    @Override
    public void setSelectedCreator(ObjectCreator selectedCreator) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedCreator");
        }
        this.selectedCreator = selectedCreator;
    }

    @Override
    public ObjectReader getSelectedReader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedReader");
        }
        return selectedReader;
    }

    @Override
    public void setSelectedReader(ObjectReader selectedReader) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedReader");
        }
        this.selectedReader = selectedReader;
    }

    @Override
    public ObjectFile getSelectedObjectFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectFile");
        }
        return selectedObjectFile;
    }

    @Override
    public void setSelectedObjectFile(ObjectFile selectedObjectFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectFile");
        }
        this.selectedObjectFile = selectedObjectFile;
    }

    @Override
    public ObjectAttribute getSelectedObjectAttribute() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectAttribute");
        }
        return selectedObjectAttribute;
    }

    @Override
    public void setSelectedObjectAttribute(ObjectAttribute selectedObjectAttribute) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectAttribute");
        }
        this.selectedObjectAttribute = selectedObjectAttribute;
    }

    // ~ methods /////////////////////////////////////////////////////////////////////////

    @Override
    public List<ObjectType> getListOfAllObjectTypeForSelectedStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAllObjectTypeForSelectedStatus");
        }
        if (this.selectedObjectTypeStatus != null) {
            return ObjectType.getObjectTypeFiltered(null, this.selectedObjectTypeStatus, null, null);
        } else {
            return ObjectType.getAllObjectType();
        }
    }

    @Override
    public List<ObjectType> getListOfAllObjectTypeReady() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAllObjectTypeReady");
        }
        List<ObjectType> res = ObjectType.getObjectTypeFiltered("ready", null, null, null);
        Collections.sort(res);
        return res;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListOfObjectCreatorForSelectedObjectType', null)}")
    public List<ObjectCreator> getListOfObjectCreatorForSelectedObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfObjectCreatorForSelectedObjectType");
        }
        if (selectedObjectType != null) {
            if (selectedObjectType.getId() != null) {
                return ObjectCreator.getObjectCreatorFiltered(null, selectedObjectType);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListOfObjectReaderForSelectedObjectType', null)}")
    public List<ObjectReader> getListOfObjectReaderForSelectedObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfObjectReaderForSelectedObjectType");
        }
        if (selectedObjectType != null) {
            if (selectedObjectType.getId() != null) {
                return ObjectReader.getObjectReaderFiltered(null, selectedObjectType);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListOfObjectFileForSelectedObjectTypeForCreator', null)}")
    public List<ObjectFile> getListOfObjectFileForSelectedObjectTypeForCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfObjectFileForSelectedObjectTypeForCreator");
        }
        if (selectedObjectType != null) {
            if (selectedObjectType.getId() != null) {
                return ObjectFile.getObjectFileFiltered(selectedObjectType, "creator");
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListOfObjectFileForSelectedObjectTypeForReaders', null)}")
    public List<ObjectFile> getListOfObjectFileForSelectedObjectTypeForReaders() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfObjectFileForSelectedObjectTypeForReaders");
        }
        if (selectedObjectType != null) {
            if (selectedObjectType.getId() != null) {
                return ObjectFile.getObjectFileFiltered(selectedObjectType, "reader");
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListOfObjectAttributeForSelectedObjectType', null)}")
    public List<ObjectAttribute> getListOfObjectAttributeForSelectedObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfObjectAttributeForSelectedObjectType");
        }
        if (selectedObjectType != null) {
            if (selectedObjectType.getId() != null) {
                return ObjectAttribute.getObjectAttributeFiltered(selectedObjectType);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public List<IntegrationProfile> getPossibleIntegrationProfilesForCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfilesForCreator");
        }
        if (this.selectedDomainForCreators != null) {
            return this.getPossibleIntegrationProfiles(this.selectedDomainForCreators);
        }
        return IntegrationProfile.listAllIntegrationProfiles();
    }

    @Override
    public List<IntegrationProfile> getPossibleIntegrationProfilesForReader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfilesForReader");
        }
        if (this.selectedDomainForReaders != null) {
            return this.getPossibleIntegrationProfiles(this.selectedDomainForReaders);
        }
        return IntegrationProfile.listAllIntegrationProfiles();
    }

    @Override
    public List<Actor> getPossibleActorsForCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleActorsForCreator");
        }
        List<Actor> actorL = null;
        if (this.selectedIntegrationProfileForCreator != null) {
            List<ActorIntegrationProfileOption> listOfTestParticipants = ActorIntegrationProfileOption
                    .getActorIntegrationProfileOptionFiltered(null, this.selectedIntegrationProfileForCreator, null,
                            null);

            HashSet<Actor> setOfActor = new HashSet<Actor>();
            if (listOfTestParticipants == null) {
                return null;
            }
            for (ActorIntegrationProfileOption tp : listOfTestParticipants) {
                setOfActor.add(tp.getActorIntegrationProfile().getActor());
            }

            actorL = new ArrayList<Actor>(setOfActor);
            Collections.sort(actorL);

        }
        return actorL;
    }

    @Override
    public List<Actor> getPossibleActorsForReader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleActorsForReader");
        }
        List<Actor> actorL = null;
        if (this.selectedIntegrationProfileForReader != null) {
            List<ActorIntegrationProfileOption> listOfTestParticipants = ActorIntegrationProfileOption
                    .getActorIntegrationProfileOptionFiltered(null, this.selectedIntegrationProfileForReader, null,
                            null);

            HashSet<Actor> setOfActor = new HashSet<Actor>();
            if (listOfTestParticipants == null) {
                return null;
            }
            for (ActorIntegrationProfileOption tp : listOfTestParticipants) {
                setOfActor.add(tp.getActorIntegrationProfile().getActor());
            }

            actorL = new ArrayList<Actor>(setOfActor);
            Collections.sort(actorL);

        }
        return actorL;
    }

    @Override
    public List<IntegrationProfileOption> getPossibleOptionsForCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleOptionsForCreator");
        }
        List<IntegrationProfileOption> ipOL = null;
        if ((this.selectedIntegrationProfileForCreator != null) && (this.selectedActorForCreator != null)) {
            List<ActorIntegrationProfileOption> listOfTestParticipants = ActorIntegrationProfileOption
                    .getActorIntegrationProfileOptionFiltered(null, this.selectedIntegrationProfileForCreator, null,
                            this.selectedActorForCreator);

            HashSet<IntegrationProfileOption> setOfOption = new HashSet<IntegrationProfileOption>();
            if (listOfTestParticipants == null) {
                return null;
            }
            for (ActorIntegrationProfileOption tp : listOfTestParticipants) {
                setOfOption.add(tp.getIntegrationProfileOption());
            }

            ipOL = new ArrayList<IntegrationProfileOption>(setOfOption);
            Collections.sort(ipOL);

        } else {

        }
        return ipOL;
    }

    @Override
    public List<IntegrationProfileOption> getPossibleOptionsForReader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleOptionsForReader");
        }
        List<IntegrationProfileOption> ipOL = null;
        if ((this.selectedIntegrationProfileForReader != null) && (this.selectedActorForReader != null)) {
            List<ActorIntegrationProfileOption> listOfTestParticipants = ActorIntegrationProfileOption
                    .getActorIntegrationProfileOptionFiltered(null, this.selectedIntegrationProfileForReader, null,
                            this.selectedActorForReader);

            HashSet<IntegrationProfileOption> setOfOption = new HashSet<IntegrationProfileOption>();
            if (listOfTestParticipants == null) {
                return null;
            }
            for (ActorIntegrationProfileOption tp : listOfTestParticipants) {
                setOfOption.add(tp.getIntegrationProfileOption());
            }

            ipOL = new ArrayList<IntegrationProfileOption>(setOfOption);
            Collections.sort(ipOL);

        } else {

        }

        return ipOL;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'addNewCreatorOfcurrentObjectType', null)}")
    public void addNewCreatorOfcurrentObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewCreatorOfcurrentObjectType");
        }
        if ((this.selectedIntegrationProfileForCreator != null) && (this.selectedActorForCreator != null)
                && (this.selectedIntegrationProfileOptionForCreator != null)) {
            if (creatorDescription == null) {
                creatorDescription = "";
            }
            ActorIntegrationProfileOption AIPO = ActorIntegrationProfileOption.getActorIntegrationProfileOption(
                    this.selectedActorForCreator, this.selectedIntegrationProfileForCreator,
                    this.selectedIntegrationProfileOptionForCreator);
            ObjectCreator objectCreator = new ObjectCreator(creatorDescription, AIPO, this.selectedObjectType);
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.merge(objectCreator);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.objectcreatorsaved']}");
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.youmustchoosepao']}");
        }
    }

    @Override
    public void resetSelectionValuesForCreator(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetSelectionValuesForCreator");
        }
        if (i == 1) {
            selectedIntegrationProfileForCreator = null;
            selectedActorForCreator = null;
            selectedIntegrationProfileOptionForCreator = null;
        }
        if (i == 2) {
            selectedActorForCreator = null;
            selectedIntegrationProfileOptionForCreator = null;
        }
        if (i == 3) {
            selectedIntegrationProfileOptionForCreator = null;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'addNewReaderOfcurrentObjectType', null)}")
    public void addNewReaderOfcurrentObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewReaderOfcurrentObjectType");
        }
        if ((this.selectedIntegrationProfileForReader != null) && (this.selectedActorForReader != null)
                && (this.selectedIntegrationProfileOptionForReader != null)) {
            if (this.readerDescription == null) {
                this.readerDescription = "";
            }
            ActorIntegrationProfileOption AIPO = ActorIntegrationProfileOption.getActorIntegrationProfileOption(
                    this.selectedActorForReader, this.selectedIntegrationProfileForReader,
                    this.selectedIntegrationProfileOptionForReader);
            ObjectReader objectReader = new ObjectReader(this.readerDescription, AIPO, this.selectedObjectType);
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.merge(objectReader);
            entityManager.flush();

            FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.newobjreadersaved']}");
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.youmustchoosepao']}");
        }
    }

    @Override
    public void resetSelectionValuesForReader(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetSelectionValuesForReader");
        }
        if (i == 1) {
            selectedIntegrationProfileForReader = null;
            selectedActorForReader = null;
            selectedIntegrationProfileOptionForReader = null;
        }
        if (i == 2) {
            selectedActorForReader = null;
            selectedIntegrationProfileOptionForReader = null;
        }
        if (i == 3) {
            selectedIntegrationProfileOptionForReader = null;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getPossibleFileTypes', null)}")
    public List<ObjectFileType> getPossibleFileTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleFileTypes");
        }
        if (selectedObjectType != null) {
            if (selectedObjectType.getId() != null) {
                return ObjectFileType.getListOfAllObjectFileType();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'addNewObjectFileTypeToObjectType', null)}")
    public void addNewObjectFileTypeToObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewObjectFileTypeToObjectType");
        }
        if (selectedFileType != null) {
            if (selectedObjectType.getId() != null) {
                if (selectedFileMaximum < selectedFileMinimum) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.maxvaluebiggermin']}");
                } else {
                    if (selectedFileDescription == null) {
                        selectedFileDescription = "";
                    }
                    ObjectFile objectFile = new ObjectFile(selectedObjectType, selectedFileDescription,
                            selectedFileType, selectedFileMinimum, selectedFileMaximum, null);
                    EntityManager entityManager = EntityManagerService.provideEntityManager();
                    entityManager.merge(objectFile);
                    entityManager.flush();
                    resetSelectionValuesForFileType();
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.newobjfilesaved']}");
                }
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.probincreationobjfile']}");
        }
    }

    private void resetSelectionValuesForFileType() {
        selectedFileType = null;
        selectedFileDescription = null;
        selectedFileMinimum = 1;
        selectedFileMaximum = 1;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'addNewObjectAttributeToObjectType', null)}")
    public void addNewObjectAttributeToObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewObjectAttributeToObjectType");
        }
        if (this.createdAttributeType != null) {
            if (this.createdAttributeDescription == null) {
                this.createdAttributeDescription = "";
            }
            ObjectAttribute objectAttribute = new ObjectAttribute(this.selectedObjectType, this.createdAttributeType,
                    this.createdAttributeDescription);
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.merge(objectAttribute);
            entityManager.flush();
            resetSelectionValuesForAttributeType();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.newobjattrsaved']}");
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.problemchooseattrtype']}");
        }
    }

    private void resetSelectionValuesForAttributeType() {
        this.createdAttributeType = null;
        this.createdAttributeDescription = null;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'updateSelectedObjectType', null)}")
    public void updateSelectedObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedObjectType");
        }
        if (selectedObjectType != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedObjectType = entityManager.merge(selectedObjectType);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.selectedobjtypeupdated']}");
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getNumberOfObjectInstanceByObjectType', null)}")
    public String getNumberOfObjectInstanceByObjectTypeForCurrentTestingSession(ObjectType objectType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfObjectInstanceByObjectTypeForCurrentTestingSession");
        }
        String result = "";
        TestingSession testingSession = TestingSession.getSelectedTestingSession();
        List<ObjectInstance> listObjectInstance = ObjectInstance.getObjectInstanceFiltered(objectType, null,
                testingSession);
        if (listObjectInstance != null) {
            result = String.valueOf(listObjectInstance.size());
            return result;
        } else {
            return "None";
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getNumberOfObjectInstanceByObjectType', null)}")
    public String getNumberOfObjectInstanceByObjectType(ObjectType objectType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfObjectInstanceByObjectType");
        }
        String result = "";
        List<ObjectInstance> listObjectInstance = ObjectInstance.getObjectInstanceFiltered(objectType, null, null);
        if (listObjectInstance != null) {
            result = String.valueOf(listObjectInstance.size());
            return result;
        } else {
            return "None";
        }
    }

    @Override
    public List<SystemInSession> getListSystemInSessionOfAIPO(ActorIntegrationProfileOption AIPO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListSystemInSessionOfAIPO");
        }
        TestingSession testingSession = TestingSession.getSelectedTestingSession();
        EntityManager em = EntityManagerService.provideEntityManager();
        return SystemInSession.getSystemInSessionFiltered(em, null, testingSession, null, null, null, null, null, AIPO,
                null, null, null, null);
    }

    @Override
    public GazelleTreeNodeImpl<Object> getTreeNode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTreeNode");
        }
        GazelleTreeNodeImpl<Object> rootNode = new GazelleTreeNodeImpl<Object>();
        List<ObjectCreator> listOC = this.getListOfObjectCreatorForSelectedObjectType();
        int i = 0;
        int j;
        for (ObjectCreator objectCreator : listOC) {
            GazelleTreeNodeImpl<Object> AIPOTreeNode = new GazelleTreeNodeImpl<Object>();
            AIPOTreeNode.setData(objectCreator.getAIPO().getId());
            List<SystemInSession> listSIS = this.getListSystemInSessionOfAIPO(objectCreator.getAIPO());
            j = 0;
            for (SystemInSession SIS : listSIS) {
                GazelleTreeNodeImpl<Object> AIPOTreeNodeChild = new GazelleTreeNodeImpl<Object>();
                AIPOTreeNodeChild.setData(SIS);
                AIPOTreeNode.addChild(j, AIPOTreeNodeChild);
                j++;
            }
            rootNode.addChild(i, AIPOTreeNode);
            i++;
        }
        return rootNode;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getNumberOfObjectInstanceBySISForSelectedObjectType', null)}")
    public String getNumberOfObjectInstanceBySISForSelectedObjectType(SystemInSession SIS) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfObjectInstanceBySISForSelectedObjectType");
        }
        String result = "";
        TestingSession testingSession = TestingSession.getSelectedTestingSession();
        List<ObjectInstance> listObjectInstance = ObjectInstance.getObjectInstanceFiltered(selectedObjectType, SIS,
                testingSession);
        int nbre = 0;
        if (listObjectInstance != null) {
            nbre = listObjectInstance.size();
            result = String.valueOf(nbre);
            return result;
        } else {
            return "0";
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListSISofSelectedObjectTypeForCreation', null)}")
    public List<SystemInSession> getListSISofSelectedObjectTypeForCreation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListSISofSelectedObjectTypeForCreation");
        }
        List<SystemInSession> result = new ArrayList<SystemInSession>();
        if (this.selectedObjectType != null) {
            List<ObjectCreator> listOC = this.getListOfObjectCreatorForSelectedObjectType();
            for (ObjectCreator objectCreator : listOC) {
                List<SystemInSession> listSIS = this.getListSystemInSessionOfAIPO(objectCreator.getAIPO());
                for (SystemInSession SIS : listSIS) {
                    if (!result.contains(SIS)
                            && (SIS.getRegistrationStatus() == null || !SIS.getRegistrationStatus().equals(
                            SystemInSessionRegistrationStatus.DROPPED))) {
                        result.add(SIS);
                    }
                }
            }
            Collections.sort(result);
        }
        return result;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getTableOfSIS', null)}")
    public String getTableOfSIS(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTableOfSIS");
        }
        String result = "";
        if (systemInSession != null) {
            if (systemInSession.getTableSession() != null) {
                result = systemInSession.getTableSession().getTableKeyword();
            }
        }
        return result;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListOfAIPOCreatorImplementedBySISForSelectedObjectType', null)}")
    public List<ActorIntegrationProfileOption> getListOfAIPOCreatorImplementedBySISForSelectedObjectType(
            SystemInSession systemInSession) {
        List<ActorIntegrationProfileOption> result = new ArrayList<ActorIntegrationProfileOption>();
        List<ObjectCreator> listOC = this.getListOfObjectCreatorForSelectedObjectType();
        for (ObjectCreator objectCreator : listOC) {
            List<SystemInSession> listSIS = this.getListSystemInSessionOfAIPO(objectCreator.getAIPO());
            if (listSIS.contains(systemInSession)) {
                result.add(objectCreator.getAIPO());
            }
        }
        return result;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListSISofSelectedObjectTypeForReading', null)}")
    public List<SystemInSession> getListSISofSelectedObjectTypeForReading() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListSISofSelectedObjectTypeForReading");
        }
        List<SystemInSession> result = new ArrayList<SystemInSession>();
        if (this.selectedObjectType != null) {
            List<ObjectReader> listOR = this.getListOfObjectReaderForSelectedObjectType();
            for (ObjectReader objectReader : listOR) {
                List<SystemInSession> listSIS = this.getListSystemInSessionOfAIPO(objectReader.getAIPO());
                for (SystemInSession SIS : listSIS) {
                    if (!result.contains(SIS)
                            && (SIS.getRegistrationStatus() == null || !SIS.getRegistrationStatus().equals(
                            SystemInSessionRegistrationStatus.DROPPED))) {
                        result.add(SIS);
                    }
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListOfAIPOReaderImplementedBySISForSelectedObjectType', null)}")
    public List<ActorIntegrationProfileOption> getListOfAIPOReaderImplementedBySISForSelectedObjectType(
            SystemInSession systemInSession) {
        List<ActorIntegrationProfileOption> result = new ArrayList<ActorIntegrationProfileOption>();
        List<ObjectReader> listOR = this.getListOfObjectReaderForSelectedObjectType();
        for (ObjectReader objectReader : listOR) {
            List<SystemInSession> listSIS = this.getListSystemInSessionOfAIPO(objectReader.getAIPO());
            if (listSIS.contains(systemInSession)) {
                result.add(objectReader.getAIPO());
            }
        }
        return result;
    }

    @Override
    public void inisializeSelectedObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("inisializeSelectedObjectType");
        }
        ObjectTypeStatus inObjectTypeStatus = ObjectTypeStatus.getSTATUS_READY();
        this.selectedObjectType = new ObjectType("", "", "", "", inObjectTypeStatus);
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'mergeSelectedObjectType', null)}")
    public void mergeSelectedObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeSelectedObjectType");
        }
        if (selectedObjectType != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedObjectType = entityManager.merge(selectedObjectType);
            entityManager.flush();
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'mergeObjectType', null)}")
    public void mergeObjectType(ObjectType inObjectType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeObjectType");
        }
        if (inObjectType != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.merge(inObjectType);
            entityManager.flush();
        }
    }

    @Override
    public List<ObjectTypeStatus> getPossibleObjectTypeStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleObjectTypeStatus");
        }
        return ObjectTypeStatus.getListStatus();
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'updateSelectedCreatorOfcurrentObjectType', null)}")
    public void updateSelectedCreatorOfcurrentObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedCreatorOfcurrentObjectType");
        }
        if ((selectedCreator != null) && (selectedObjectType != null)) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedCreator = entityManager.merge(selectedCreator);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.objcreatorupdated']}");
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'updateSelectedReaderOfcurrentObjectType', null)}")
    public void updateSelectedReaderOfcurrentObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedReaderOfcurrentObjectType");
        }
        if ((selectedReader != null) && (selectedObjectType != null)) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedReader = entityManager.merge(selectedReader);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.objreaderupdatd']}");
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'mergeObjectFileTypeOfObjectType', null)}")
    public void mergeObjectFileTypeOfObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeObjectFileTypeOfObjectType");
        }
        if ((selectedObjectFile != null) && (selectedObjectType != null)) {
            if (selectedObjectFile.getType() != null) {
                if (selectedObjectFile.getDescription().length() > 255) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.probdescfilesolong']}");
                } else if (selectedObjectFile.getMax() >= selectedObjectFile.getMin()) {
                    EntityManager entityManager = EntityManagerService.provideEntityManager();
                    selectedObjectFile.setObject(this.selectedObjectType);
                    selectedObjectFile = entityManager.merge(selectedObjectFile);
                    entityManager.flush();
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.objfilesaved']}");
                } else {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.maxvaluebiggermin']}");
                }
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.typeoffilenotspecified']}");
            }
        }
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'mergeObjectAttributeOfObjectType', null)}")
    public void mergeObjectAttributeOfObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeObjectAttributeOfObjectType");
        }
        if ((selectedObjectAttribute != null) && (selectedObjectType != null)) {
            if (selectedObjectAttribute.getKeyword().equals("")) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.youmustspecifykeyword']}");
            } else {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                selectedObjectAttribute.setObject(this.selectedObjectType);
                selectedObjectAttribute = entityManager.merge(selectedObjectAttribute);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.attributesaved']}");
            }
        }
    }

    @Override
    public void initializeSelectedObjectFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeSelectedObjectFile");
        }
        this.selectedObjectFile = new ObjectFile();
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListObjectInstanceForSelectedObjectType', null)}")
    public List<ObjectInstance> getListObjectInstanceForSelectedObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListObjectInstanceForSelectedObjectType");
        }
        if (this.selectedObjectType == null) {
            return null;
        }
        if (this.selectedObjectType.getId() == null) {
            return null;
        }
        if (this.selectedObjectType.getId() == 0) {
            return null;
        }
        return this.getListObjectInstanceForObjectType(this.selectedObjectType);
    }

    @Restrict("#{s:hasPermission('ObjectTypeManager', 'getListObjectInstanceForObjectType', null)}")
    private List<ObjectInstance> getListObjectInstanceForObjectType(ObjectType objectType) {
        List<ObjectInstance> listObjectInstance = new ArrayList<ObjectInstance>();
        if (objectType != null) {
            if (objectType.getId() != 0) {
                listObjectInstance = ObjectInstance.getObjectInstanceFiltered(objectType, null, null);
            }
        }
        return listObjectInstance;
    }

    @Override
    public void initializeSelectedObjectAttribute() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeSelectedObjectAttribute");
        }
        this.selectedObjectAttribute = new ObjectAttribute();
    }

    @Override
    public List<ObjectInstanceFile> getListObjectInstanceFileForSelectedObjectFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListObjectInstanceFileForSelectedObjectFile");
        }
        List<ObjectInstanceFile> result = new ArrayList<ObjectInstanceFile>();
        if (this.selectedObjectFile == null) {
            return result;
        }
        if (this.selectedObjectFile.getId() == null) {
            return result;
        }
        if (this.selectedObjectFile.getId() == 0) {
            return result;
        }

        try {
            result = ObjectInstanceFile.getObjectInstanceFileFiltered(null, selectedObjectFile, null, null, null);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'viewListOIFOnDeleting', null)}")
    public boolean viewListOIFOnDeleting() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewListOIFOnDeleting");
        }
        List<ObjectInstanceFile> listOIF;
        try {
            listOIF = this.getListObjectInstanceFileForSelectedObjectFile();
        } catch (Exception e) {
            return false;
        }
        if (listOIF.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'editObjectType', null)}")
    public String editObjectType(ObjectType inObjectType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editObjectType");
        }
        this.setSelectedObjectType(inObjectType);
        return "/objects/editObjectType.xhtml";
    }

    @Override
    public String getNumberOfObjectInstanceToReadBySIS(SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfObjectInstanceToReadBySIS");
        }
        int result = 0;
        TestingSession testingSession = TestingSession.getSelectedTestingSession();
        List<ObjectType> list_OT_OnR = this.getListOfObjectTypeForCurrentSessionOnReading(inSystemInSession);
        List<ObjectInstance> list_OI_tmp;
        for (ObjectType inObjectType : list_OT_OnR) {
            list_OI_tmp = ObjectInstance.getObjectInstanceFiltered(inObjectType, null, testingSession);
            for (ObjectInstance inObjectInstance : list_OI_tmp) {
                if (inObjectInstance.isCompleted()) {
                    result++;
                }
            }
        }
        return String.valueOf(result);
    }

    private List<ObjectType> getListOfObjectTypeForCurrentSessionOnReading(SystemInSession inSystemInSession) {
        List<ObjectType> listOT = new ArrayList<ObjectType>();
        if (inSystemInSession != null) {
            listOT = ObjectType.getObjectTypeFiltered(null, null, null, inSystemInSession.getSystem());
        }
        return listOT;
    }

    @Override
    public String getNumberOfCompletedObjectInstanceBySISForSelectedObjectType(SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfCompletedObjectInstanceBySISForSelectedObjectType");
        }
        if (selectedObjectType != null) {
            return this.getNumberOfCompletedObjectInstanceBySISForSelectedObjectType(inSystemInSession,
                    selectedObjectType);
        }
        return "0";
    }

    private String getNumberOfCompletedObjectInstanceBySISForSelectedObjectType(SystemInSession inSystemInSession,
                                                                                ObjectType inObjectType) {
        int result = 0;
        List<ObjectInstance> listObjectInstance = ObjectInstance.getObjectInstanceFiltered(null, inSystemInSession,
                null);
        if (listObjectInstance != null) {
            for (ObjectInstance objectInstance : listObjectInstance) {
                if ((objectInstance.getObject().equals(inObjectType)) && (objectInstance.isCompleted())) {
                    result++;
                }
            }
        }
        return String.valueOf(result);
    }

    @Override
    public List<Domain> getPossibleDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleDomains");
        }
        return Domain.getPossibleDomains();
    }

    @Override
    public List<IntegrationProfile> getPossibleIntegrationProfiles(Domain inDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfiles");
        }
        return IntegrationProfile.getPossibleIntegrationProfiles(inDomain);
    }

    @Override
    public void getListOfObjectTypeByDataModel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfObjectTypeByDataModel");
        }
        foundObjectTypes = new ObjectTypeDataModel(selectedObjectTypeStatus);
    }

    // ~delete methods ////////////////////////////////////////////////////////////////

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'deleteSelectedObjectTypeFromDataBaseForSelectedObjectType', null)}")
    public void deleteSelectedObjectTypeFromDataBaseForSelectedObjectType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedObjectTypeFromDataBaseForSelectedObjectType");
        }
        ObjectType.deleteObjectType(this.selectedObjectType);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.objtypeselecteddeleted']}");
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'deleteSelectedCreatorFromDataBase', null)}")
    public void deleteSelectedCreatorFromDataBase() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedCreatorFromDataBase");
        }
        ObjectCreator.deleteObjectCreator(this.selectedCreator);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.typeofcreatorselecteddeleted']}");
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'deleteSelectedReaderFromDataBase', null)}")
    public void deleteSelectedReaderFromDataBase() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedReaderFromDataBase");
        }
        ObjectReader.deleteObjectReader(this.selectedReader);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.typeofreaderspecifieddeleted']}");
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'deleteSelectedObjectFileFromDataBase', null)}")
    public void deleteSelectedObjectFileFromDataBase() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedObjectFileFromDataBase");
        }
        ObjectFile.deleteObjectFile(this.selectedObjectFile);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.typeoffilespecifieddeleted']}");
    }

    @Override
    @Restrict("#{s:hasPermission('ObjectTypeManager', 'deleteSelectedObjectAttributeFromDataBase', null)}")
    public void deleteSelectedObjectAttributeFromDataBase() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedObjectAttributeFromDataBase");
        }
        ObjectAttribute.deleteObjectAttribute(this.selectedObjectAttribute);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.testmanagement.object.attrselecteddeleted']}");
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
