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

import net.ihe.gazelle.common.action.DateDisplay;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.tree.GazelleTreeNodeImpl;
import net.ihe.gazelle.common.util.ZipUtility;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.html.generator.Attribute;
import net.ihe.gazelle.html.generator.Tag;
import net.ihe.gazelle.objects.model.*;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.security.Identity;
import org.richfaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Abderrazek Boufahja > INRIA Rennes IHE development Project
 */
@Synchronized(timeout = 10000)

@Name("objectManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("ObjectManagerLocal")
public class ObjectManager extends AbstractSampleManager implements ObjectManagerLocal, Serializable {

    private static final long serialVersionUID = -8734895233751607120L;
    private static final String TAB_CREATOR = "tabForCreating";
    private static final String TAB_READER = "tabForObjToRender";

    // ~ Statics variables and Class initializer ///////////////////////////////////////////////////////////////////////

    private static final Logger LOG = LoggerFactory.getLogger(ObjectManager.class);

    // ~ Attribute ///////////////////////////////////////////////////////////////////////

    private SystemInSession selectedSystemInSession;

    private Institution selectedInstitution;

    private String selectedSnapshotName;

    private GazelleTreeNodeImpl<Object> treeNodeCreatorList;

    private GazelleTreeNodeImpl<Object> treeNodeReaderList;

    private List<Institution> listInstitutionsForCurrentSession;

    private String selectedTab = TAB_CREATOR;

    // ~ getter && setter ////////////////////////////////////////////////////////////////

    @Override
    public String getSelectedTab() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTab");
        }
        if (this.selectedTab == null) {
            return TAB_CREATOR;
        }
        return selectedTab;
    }

    @Override
    public void setSelectedTab(String selectedTab) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTab");
        }
        if (!this.selectedTab.equals(selectedTab)) {
            this.selectedObjectInstance = null;
        }
        this.selectedTab = selectedTab;
    }

    @Override
    public List<Institution> getListInstitutionsForCurrentSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListInstitutionsForCurrentSession");
        }
        if (listInstitutionsForCurrentSession == null) {
            this.listInstitutionsForCurrentSession = this.getAllInstitutionsForCurrentSession();
        }
        Collections.sort(listInstitutionsForCurrentSession);
        return listInstitutionsForCurrentSession;
    }

    @Override
    public void setListInstitutionsForCurrentSession(List<Institution> listInstitutionsForCurrentSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListInstitutionsForCurrentSession");
        }
        this.listInstitutionsForCurrentSession = listInstitutionsForCurrentSession;
    }

    @Override
    public GazelleTreeNodeImpl<Object> getTreeNodeReaderList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTreeNodeReaderList");
        }
        return treeNodeReaderList;
    }

    @Override
    public void setTreeNodeReaderList(GazelleTreeNodeImpl<Object> treeNodeReaderList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTreeNodeReaderList");
        }
        this.treeNodeReaderList = treeNodeReaderList;
    }

    @Override
    public GazelleTreeNodeImpl<Object> getTreeNodeCreatorList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTreeNodeCreatorList");
        }
        return treeNodeCreatorList;
    }

    @Override
    public void setTreeNodeCreatorList(GazelleTreeNodeImpl<Object> treeNodeCreatorList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTreeNodeCreatorList");
        }
        this.treeNodeCreatorList = treeNodeCreatorList;
    }

    @Override
    public String getSelectedSnapshotName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSnapshotName");
        }
        return selectedSnapshotName;
    }

    @Override
    public void setSelectedSnapshotName(String selectedSnapshotName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSnapshotName");
        }
        this.selectedSnapshotName = selectedSnapshotName;
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
    public Institution getSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitution");
        }
        return selectedInstitution;
    }

    @Override
    public void setSelectedInstitution(Institution selectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInstitution");
        }
        this.selectedInstitution = selectedInstitution;
    }

    // ~ methods /////////////////////////////////////////////////////////////////////////
    @Override
    public void resetInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetInstitution");
        }
        setSelectedInstitution(null);
        setTreeNodeCreatorList(null);
        getListObjectInstanceToRenderForCurrentSIS();
    }

    @Override
    public List<Institution> getAllInstitutions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllInstitutions");
        }
        return Institution.listAllInstitutions();
    }

    @Override
    public List<SystemInSession> getSystemListForSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemListForSelectedInstitution");
        }
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        EntityManager em = EntityManagerService.provideEntityManager();
        if ((this.selectedInstitution != null) && (activatedTestingSession != null)) {
            List<SystemInSession> systemInSessionList = SystemInSession
                    .getNotDroppedSystemsInSessionForCompanyForSession(em, selectedInstitution, activatedTestingSession);
            Collections.sort(systemInSessionList);
            if (systemInSessionList.size() > 0) {
                return systemInSessionList;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public List<ObjectInstance> getListObjectInstanceForSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListObjectInstanceForSelectedSystemInSession");
        }
        if (this.selectedSystemInSession != null) {
            return ObjectInstance.getObjectInstanceFiltered(null, selectedSystemInSession, null);
        } else {
            return null;
        }
    }

    @Override
    public List<ObjectType> getListOfObjectTypeForCurrentSessionOnCreating() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfObjectTypeForCurrentSessionOnCreating");
        }
        // *
        List<ObjectType> listOT = new ArrayList<ObjectType>();
        if (selectedSystemInSession != null) {
            listOT = ObjectType.getObjectTypeFiltered("ready", null, selectedSystemInSession.getSystem(), null);
        }
        return listOT;
    }

    @Override
    public List<ObjectType> getListOfObjectTypeForCurrentSessionOnReading() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfObjectTypeForCurrentSessionOnReading");
        }
        // *
        List<ObjectType> listOT = new ArrayList<ObjectType>();
        if (selectedSystemInSession != null) {
            listOT = ObjectType.getObjectTypeFiltered("ready", null, null, selectedSystemInSession.getSystem());
        }

        return listOT;
    }

    /**
     * return list of SIS that can create this object type
     */
    @Override
    public List<SystemInSession> getListOfSystemInSessionForObjectType(ObjectType objectType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfSystemInSessionForObjectType");
        }
        List<SystemInSession> result = new ArrayList<SystemInSession>();
        EntityManager em = EntityManagerService.provideEntityManager();
        if (objectType != null) {
            TestingSession testingSession = TestingSession.getSelectedTestingSession();
            result = SystemInSession.getSystemInSessionFiltered(em, null, testingSession, null, null, null, null, null,
                    null, null, null, null, null, null, objectType, null, null, null);
        }
        return result;
    }

    @Override
    public List<Institution> getAllInstitutionsForCurrentSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllInstitutionsForCurrentSession");
        }
        TestingSession testingSession = TestingSession.getSelectedTestingSession();
        return TestingSession.getListOfInstitutionsParticipatingInSession(testingSession);
    }

    @Override
    public String getNumberOfObjectInstanceOfSISByObjectType(SystemInSession SIS, ObjectType objectType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfObjectInstanceOfSISByObjectType");
        }
        String result = "";
        List<ObjectInstance> listObjectInstance = ObjectInstance.getObjectInstanceFiltered(objectType, SIS, null);
        result = String.valueOf(listObjectInstance.size());
        return result;
    }

    @Override
    public List<ObjectInstance> getListObjectInstanceForObjectType(ObjectType objectType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListObjectInstanceForObjectType");
        }
        List<ObjectInstance> listObjectInstance = new ArrayList<ObjectInstance>();
        if (objectType != null) {
            listObjectInstance = ObjectInstance.getObjectInstanceFiltered(objectType, null, null);
        }
        return listObjectInstance;
    }

    @Override
    public List<ObjectInstance> getListObjectInstanceForSelectedSISByOType(ObjectType OT) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListObjectInstanceForSelectedSISByOType");
        }
        if (OT != null) {
            return ObjectInstance.getObjectInstanceFiltered(OT, selectedSystemInSession, null);
        } else {
            return null;
        }
    }

    @Override
    public File getSnapshotFile(ObjectInstanceFile inObjectInstanceFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSnapshotFile");
        }
        if (inObjectInstanceFile != null) {
            String linkpath = inObjectInstanceFile.getFileAbsolutePath();
            File result = new File(linkpath);
            if (result.exists()) {
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void deleteSelectedObjectInstanceFromDataBase() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedObjectInstanceFromDataBase");
        }
        if (selectedObjectInstance != null) {
            ObjectInstance.deleteObjectInstance(selectedObjectInstance);
            selectedObjectInstance = null;
        }
    }

    @Override
    public Institution getInstitutionOfCurrentUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionOfCurrentUser");
        }
        return Institution.getLoggedInInstitution();
    }

    private List<ObjectInstance> getListObjectInstanceForObjectTypeAndSIS(SystemInSession inSystemInSession,
                                                                          ObjectType inObjectType) {
        if ((inSystemInSession != null) && (inObjectType != null)) {
            return ObjectInstance.getObjectInstanceFiltered(inObjectType, inSystemInSession, null);
        } else {
            return new ArrayList<ObjectInstance>();
        }
    }

    @Override
    public void reinitializeAttribute(Institution inInstitution, SystemInSession inSystemInSession,
                                      ObjectInstance inObjectInstance, ObjectInstance inObjectInstanceOnReading, boolean reRenderTree) {
        this.selectedInstitution = inInstitution;
        this.selectedSystemInSession = inSystemInSession;
        this.selectedObjectInstance = inObjectInstance;
        List<SystemInSession> lsis = this.getSystemListForSelectedInstitution();
        if (lsis != null) {
            if (lsis.size() == 1) {
                this.selectedSystemInSession = lsis.get(0);
            }
        }
        this.getTreeNodeOfCreator();
        this.getTreeNodeOfReader();
    }

    @Override
    public void initialiseSystemObjectPage(SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initialiseSystemObjectPage");
        }
        if (inSystemInSession != null) {
            System inSystem = inSystemInSession.getSystem();
            InstitutionSystemQuery query = new InstitutionSystemQuery();
            query.id().eq(inSystem.getId());
            InstitutionSystem institutionSystem = query.getUniqueResult();
            if (selectedInstitution != null) {
                selectedInstitution = institutionSystem.getInstitution();
                selectedSystemInSession = inSystemInSession;
                selectedObjectInstance = null;
            }
        }
    }

    @Override
    public void initializeAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeAll");
        }
        this.selectedObjectInstanceFile = null;
        this.selectedSpecification = null;
        this.getTreeNodeOfCreator();
        this.getTreeNodeOfReader();
    }

    @Override
    public String backToListObjectInstance(ObjectInstance inObjectInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("backToListObjectInstance");
        }
        this.selectedObjectInstance = inObjectInstance;
        this.selectedTab = TAB_CREATOR;
        return "/objects/system_object.seam";
    }

    @Override
    public void saveCommentOfObjectInstanceForCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveCommentOfObjectInstanceForCreator");
        }
        this.saveCommentOfObjectInstance(commentOfCurrentSIS, selectedObjectInstance);
        this.commentOfCurrentSIS = null;
    }

    @Override
    public void setSelectedAnnotationAndComment(Annotation ann) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedAnnotationAndComment");
        }
        this.selectedAnnotation = ann;
        this.commentOfCurrentSIS = ann.getValue();
    }

    @Override
    public boolean objectInstanceFileCanBeParsed(ObjectInstanceFile inObjectInstanceFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("objectInstanceFileCanBeParsed");
        }
        return false;
    }

    @Override
    public String getValidationStatus(ObjectInstance inObjectInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationStatus");
        }
        String result = "";
        if (inObjectInstance.getValidation() != null) {
            if (inObjectInstance.getValidation().getDescription() != null) {
                result = "( " + inObjectInstance.getValidation().getDescription() + " )";
            }
        }
        return result;
    }

    @Override
    public String getValidationStatusValue(ObjectInstance inObjectInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationStatusValue");
        }
        String result = "";
        if (inObjectInstance.getValidation() != null) {
            if (inObjectInstance.getValidation().getValue() != null) {
                result = "( " + inObjectInstance.getValidation().getValue() + " )";
            }
        }
        return result;
    }

    @Override
    public String returnToSystemObjectPageForTheTabReader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("returnToSystemObjectPageForTheTabReader");
        }
        this.selectedTab = TAB_READER;
        return "/objects/system_object.seam";
    }

    @Override
    public String returnToSystemObjectPageForTheTabCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("returnToSystemObjectPageForTheTabCreator");
        }
        this.selectedTab = TAB_CREATOR;
        return "/objects/system_object.seam";
    }

    // Tree /////////////////////////////////////////////////////////////////////////////

    private void getTreeNodeOfCreator() {
        GazelleTreeNodeImpl<Object> rootNode = new GazelleTreeNodeImpl<Object>();
        List<ObjectType> listOT = this.getListOfObjectTypeForCurrentSessionOnCreating();
        Collections.sort(listOT);
        int i = 0;
        int j;
        for (ObjectType objectType : listOT) {
            GazelleTreeNodeImpl<Object> OTypeTreeNode = new GazelleTreeNodeImpl<Object>();
            OTypeTreeNode.setData(objectType);
            List<ObjectInstance> listOI = this.getListObjectInstanceForSelectedSISByOType(objectType);
            Collections.sort(listOI);
            j = 0;
            for (ObjectInstance objectInstance : listOI) {
                GazelleTreeNodeImpl<Object> OITreeNode = new GazelleTreeNodeImpl<Object>();
                OITreeNode.setData(objectInstance);
                OTypeTreeNode.addChild(j, OITreeNode);
                j++;
            }
            rootNode.addChild(i, OTypeTreeNode);
            i++;
        }
        this.treeNodeCreatorList = rootNode;
    }

    private void getTreeNodeOfReader() {
        GazelleTreeNodeImpl<Object> rootNode = new GazelleTreeNodeImpl<Object>();

        List<ObjectType> listOT = this.getListOfObjectTypeForCurrentSessionOnReading();
        Collections.sort(listOT);
        int i = 0;
        int j;
        int k;
        for (ObjectType objectType : listOT) {
            GazelleTreeNodeImpl<Object> OTypeTreeNode = new GazelleTreeNodeImpl<Object>();
            OTypeTreeNode.setData(objectType);
            List<SystemInSession> listSIS = this.getListOfSystemInSessionForObjectType(objectType);
            Collections.sort(listSIS);
            j = 0;
            for (SystemInSession systemInSession : listSIS) {
                GazelleTreeNodeImpl<Object> SISNode = new GazelleTreeNodeImpl<Object>();
                SISNode.setData(systemInSession);
                List<ObjectInstance> listOI = this
                        .getListObjectInstanceForObjectTypeAndSIS(systemInSession, objectType);
                List<ObjectInstance> listOICompleted = this.getCompletedObjectInstance(listOI);
                Collections.sort(listOICompleted);
                k = 0;
                for (ObjectInstance objectInstance : listOICompleted) {
                    GazelleTreeNodeImpl<Object> OINode = new GazelleTreeNodeImpl<Object>();
                    OINode.setData(objectInstance);
                    SISNode.addChild(k, OINode);
                    k++;
                }
                OTypeTreeNode.addChild(j, SISNode);
                j++;
            }
            rootNode.addChild(i, OTypeTreeNode);
            i++;
        }

        this.treeNodeReaderList = rootNode;
    }

    // //////////////////////

    private List<ObjectInstance> getCompletedObjectInstance(List<ObjectInstance> listOI) {
        List<ObjectInstance> result = new ArrayList<ObjectInstance>();
        for (ObjectInstance inObjectInstance : listOI) {
            if (inObjectInstance.isCompleted()) {
                result.add(inObjectInstance);
            }
        }
        return result;
    }

    @Override
    public TreeNode getTreeNodeOfDicomObjectInstanceFile(ObjectInstanceFile inObjectInstanceFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTreeNodeOfDicomObjectInstanceFile");
        }
        if (inObjectInstanceFile != null) {
            return super.getTreeNodeOfDicomObjectInstanceFile(inObjectInstanceFile);
        } else {
            return null;
        }
    }

    @Override
    public void deleteSelectedObjectInstanceFromDataBaseAndUpdateTree() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedObjectInstanceFromDataBaseAndUpdateTree");
        }
        this.deleteSelectedObjectInstanceFromDataBase();
        initializeAll();
    }

    @Override
    public String functionTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("functionTest");
        }
        return new java.util.Date().toString();

    }

    @Override
    public void downloadAllSampleOfType(ObjectType objectType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadAllSampleOfType");
        }
        String tmp_path = ApplicationManager.instance().getObjectsPath() + java.io.File.separatorChar + "tmp"
                + Identity.instance().getCredentials().getUsername();
        java.io.File tmpfile = new java.io.File(tmp_path);
        if (tmpfile.exists()) {
            ZipUtility.deleteDirectory(tmpfile);
        }
        if (tmpfile.mkdirs()) {
            LOG.info("tmpfile directory created");
        } else {
            LOG.error("Failed to create tmpfile");
        }
        List<SystemInSession> listSISCreator = this.getListOfSystemInSessionForObjectType(objectType);
        if (listSISCreator != null) {
            String sis_path = "";
            java.io.File sisfile = null;
            for (SystemInSession sis : listSISCreator) {
                sis_path = tmp_path + java.io.File.separatorChar + sis.getSystem().getKeyword();
                sisfile = new java.io.File(sis_path);
                if (sisfile.mkdirs()) {
                    LOG.info("sisfile directory created");
                } else {
                    LOG.error("Failed to create sisfile");
                }
                List<ObjectInstance> loitmp = this.getListObjectInstanceForObjectTypeAndSIS(sis, objectType);
                List<ObjectInstance> loi = this.getCompletedObjectInstance(loitmp);
                if (loi != null) {
                    String oi_path = "";
                    java.io.File oifile = null;
                    for (ObjectInstance oi : loi) {
                        oi_path = sis_path + java.io.File.separatorChar + oi.getId().toString();
                        oifile = new java.io.File(oi_path);
                        if (oifile.mkdirs()) {
                            LOG.info("oifile directory created");
                        } else {
                            LOG.error("Failed to create oifile");
                        }
                        List<ObjectInstanceFile> loif = this.getListInstanceFileFromObjectInstance(oi);
                        if (loif != null) {
                            for (ObjectInstanceFile oif : loif) {
                                String filepath = ApplicationManager.instance().getObjectsPath()
                                        + java.io.File.separatorChar + oif.getInstance().getId()
                                        + java.io.File.separatorChar + oif.getFile().getId()
                                        + java.io.File.separatorChar + oif.getId() + "_" + oif.getUrl();
                                java.io.File oiffile = new java.io.File(filepath);

                                String oifpathres = oi_path + java.io.File.separatorChar + oif.getId() + "_"
                                        + oif.getUrl();
                                java.io.File oiffileres = new java.io.File(oifpathres);

                                ZipUtility.copyFile(oiffile, oiffileres);

                            }
                        }
                        String descfile = oi_path + java.io.File.separatorChar + oi.getId() + ".html";
                        String desccontent = this.generateHtmlDescriptionToObjectInstance(oi);
                        try {
                            ZipUtility.writeFile(descfile, desccontent);
                        } catch (IOException e) {
                            LOG.error("", e);
                        }
                    }
                }
            }
        }
        String archive_file = ApplicationManager.instance().getObjectsPath() + java.io.File.separatorChar
                + Identity.instance().getCredentials().getUsername() + "archive.zip";
        try {
            ZipUtility.zipDirectory(tmpfile, new File(archive_file));
        } catch (IOException e) {
            LOG.error("", e);
        }

        net.ihe.gazelle.common.util.DocumentFileUpload.showFile(archive_file, objectType.getKeyword() + ".zip", true);

        ZipUtility.deleteDirectory(tmpfile);

        java.io.File archfile = new java.io.File(archive_file);

        if (archfile.delete()) {
            LOG.info("archfile deleted");
        } else {
            LOG.error("Failed to delete archfile");
        }

    }

    @SuppressWarnings("unchecked")
    private String generateHtmlDescriptionToObjectInstance(ObjectInstance oi) {
        Tag html = new Tag("html");
        Tag body = new Tag("body");

        // a simple header
        Tag head = new Tag("head");
        Tag title = new Tag("title");
        title.add("Sample description");
        head.add(title);
        body.add(head);

        Tag h1 = new Tag("h1");
        h1.add("Sample : " + oi.getId() + " - " + oi.getName());
        body.add(h1);
        body.add(new Tag("br", false));

        Tag p1 = new Tag("p");
        body.add(p1);

        Tag id = new Tag("b");
        id.add("id : ");
        p1.add(id);
        p1.add(oi.getId());
        p1.add(new Tag("br", false));

        Tag sampletype = new Tag("b");
        sampletype.add("Sample type : ");
        p1.add(sampletype);
        p1.add(oi.getObject().getKeyword());
        p1.add(new Tag("br", false));

        Tag systemcreator = new Tag("b");
        systemcreator.add("System Creator : ");
        p1.add(systemcreator);
        p1.add(oi.getSystem().getSystem().getKeyword());
        p1.add(new Tag("br", false));

        Tag company = new Tag("b");
        company.add("Institution : ");
        p1.add(company);
        p1.add(this.getInstitution(oi));
        p1.add(new Tag("br", false));

        Tag datecreation = new Tag("b");
        datecreation.add("Date Of Creation : ");
        p1.add(datecreation);
        p1.add(DateDisplay.instance().displayDateTime(oi.getLastChanged(), true));
        p1.add(new Tag("br", false));

        Tag link = new Tag("b");
        link.add("Permanent Link : ");
        p1.add(link);
        String ll = this.getPermanentLink(oi);

        Tag linkto = new Tag("a");
        linkto.getAttributes().add(new Attribute("href=" + ll));
        linkto.add(ll);
        p1.add(linkto);
        p1.add(new Tag("br", false));

        Tag description = new Tag("b");
        description.add("Description : ");
        p1.add(description);
        p1.add(oi.getDescription());
        p1.add(new Tag("br", false));

        Tag listOfFile = new Tag("h2");
        listOfFile.add("List Of Files");
        body.add(listOfFile);
        Tag listOfFiledesc = new Tag("p");
        body.add(listOfFiledesc);
        listOfFiledesc.add(new Tag("br", false));

        List<ObjectInstanceFile> loif = ObjectInstanceFile.getObjectInstanceFileFiltered(oi, null, null, "creator",
                null);

        if (loif != null) {
            if (loif.size() > 0) {
                Tag ulf = new Tag("ul");
                listOfFiledesc.add(ulf);
                for (ObjectInstanceFile oif : loif) {
                    Tag lif = new Tag("li");
                    lif.add(oif.getFile().getType().getKeyword() + ":" + oif.getUrl());
                    ulf.add(lif);
                }
            }
        }

        Tag listOfAttr = new Tag("h2");
        listOfAttr.add("List Of Attributes");
        body.add(listOfAttr);
        Tag listOfAttrdesc = new Tag("p");
        body.add(listOfAttrdesc);
        listOfAttrdesc.add(new Tag("br", false));

        List<ObjectInstanceAttribute> loia = this.getListInstanceAttributeFromObjectInstance(oi);
        if (loia != null) {
            if (loia.size() > 0) {
                Tag ulf = new Tag("ul");
                listOfAttrdesc.add(ulf);
                for (ObjectInstanceAttribute oia : loia) {
                    Tag lif = new Tag("li");
                    lif.add(oia.getAttribute().getKeyword());
                    ulf.add(lif);
                }
            }
        }

        html.add(head);
        html.add(body);

        return html.toString();

    }

    @Override
    public boolean canDownloadAllSampleOfType(ObjectType ot) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canDownloadAllSampleOfType");
        }
        boolean result = false;
        TestingSession inTestingSession = TestingSession.getSelectedTestingSession();
        int noi = ObjectInstance.getNumberObjectInstanceFiltered(ot, null, inTestingSession, null);
        if (noi == 0) {
            result = false;
        } else if (noi > 0) {
            result = true;
        }
        return result;
    }

    @Override
    public List<ObjectInstance> getListObjectInstanceToRenderForCurrentSIS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListObjectInstanceToRenderForCurrentSIS");
        }
        List<ObjectInstance> loi = new ArrayList<ObjectInstance>();
        List<ObjectType> lot = this.getListOfObjectTypeForCurrentSessionOnReading();
        List<ObjectInstance> loitmp = null;
        for (ObjectType ot : lot) {
            List<SystemInSession> lsisr = this.getListOfSystemInSessionForObjectType(ot);
            for (SystemInSession sis : lsisr) {
                loitmp = this.getListObjectInstanceForObjectTypeAndSIS(sis, ot);
                loi.addAll(loitmp);
            }
        }
        return loi;
    }

    @Override
    public void downloadAllSamplesForRender() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadAllSamplesForRender");
        }
        String tmp_path = ApplicationManager.instance().getObjectsPath() + java.io.File.separatorChar
                + (new Date()).getTime() + Identity.instance().getCredentials().getUsername();
        java.io.File tmpfile = new java.io.File(tmp_path);
        if (tmpfile.exists()) {
            ZipUtility.deleteDirectory(tmpfile);
        }
        if (tmpfile.mkdirs()) {
            LOG.info("tmpfile directory created");
        } else {
            LOG.error("Failed to create tmpfile");
        }
        List<ObjectType> lot = this.getListOfObjectTypeForCurrentSessionOnReading();
        for (ObjectType objectType : lot) {
            String otpath = tmp_path + java.io.File.separatorChar + objectType.getKeyword();
            java.io.File tmpotfile = new java.io.File(otpath);
            if (tmpotfile.mkdirs()) {
                LOG.info("tmpotfile directory created");
            } else {
                LOG.error("Failed to create tmpotfile");
            }
            List<SystemInSession> listSISCreator = this.getListOfSystemInSessionForObjectType(objectType);
            if (listSISCreator != null) {
                String sis_path = "";
                java.io.File sisfile = null;
                for (SystemInSession sis : listSISCreator) {
                    sis_path = otpath + java.io.File.separatorChar + sis.getSystem().getKeyword();
                    sisfile = new java.io.File(sis_path);
                    if (sisfile.mkdirs()) {
                        LOG.info("sisfile directory created");
                    } else {
                        LOG.error("Failed to create sisfile");
                    }
                    List<ObjectInstance> loitmp = this.getListObjectInstanceForObjectTypeAndSIS(sis, objectType);
                    List<ObjectInstance> loi = this.getCompletedObjectInstance(loitmp);
                    if (loi != null) {
                        String oi_path = "";
                        java.io.File oifile = null;
                        for (ObjectInstance oi : loi) {
                            oi_path = sis_path + java.io.File.separatorChar + oi.getId().toString();
                            oifile = new java.io.File(oi_path);
                            if (oifile.mkdirs()) {
                                LOG.info("oifile directory created");
                            } else {
                                LOG.error("Failed to create oifile");
                            }
                            List<ObjectInstanceFile> loif = this.getListInstanceFileFromObjectInstance(oi);
                            if (loif != null) {
                                for (ObjectInstanceFile oif : loif) {
                                    String filepath = ApplicationManager.instance().getObjectsPath()
                                            + java.io.File.separatorChar + oif.getInstance().getId()
                                            + java.io.File.separatorChar + oif.getFile().getId()
                                            + java.io.File.separatorChar + oif.getId() + "_" + oif.getUrl();
                                    java.io.File oiffile = new java.io.File(filepath);

                                    String oifpathres = oi_path + java.io.File.separatorChar + oif.getId() + "_"
                                            + oif.getUrl();
                                    java.io.File oiffileres = new java.io.File(oifpathres);

                                    ZipUtility.copyFile(oiffile, oiffileres);

                                }
                            }
                            String descfile = oi_path + java.io.File.separatorChar + oi.getId() + ".html";
                            String desccontent = this.generateHtmlDescriptionToObjectInstance(oi);
                            try {
                                ZipUtility.writeFile(descfile, desccontent);
                            } catch (IOException e) {
                                LOG.error("", e);
                            }
                        }
                    }
                }
            }
        }
        String archive_file = ApplicationManager.instance().getObjectsPath() + java.io.File.separatorChar
                + Identity.instance().getCredentials().getUsername() + "_archive.zip";
        try {
            ZipUtility.zipDirectory(tmpfile, new File(archive_file));
        } catch (IOException e) {
            LOG.error("", e);
        }

        net.ihe.gazelle.common.util.DocumentFileUpload.showFile(archive_file, String.valueOf((new Date()).getTime())
                + "_" + Identity.instance().getCredentials().getUsername() + ".zip", true);

        ZipUtility.deleteDirectory(tmpfile);

        java.io.File archfile = new java.io.File(archive_file);

        if (archfile.delete()) {
            LOG.info("archfile deleted");
        } else {
            LOG.error("Failed to delete archfile");
        }
    }

    @Override
    public void initializeRedirection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeRedirection");
        }

    }

    @Override
    public java.lang.Boolean adviseNodeSelected(org.richfaces.component.UITree tree) {
        if ((tree.getRowData() instanceof ObjectInstance)
                && ((ObjectInstance) tree.getRowData()).equals(this.selectedObjectInstance)) {
            return true;
        }
        return false;
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
