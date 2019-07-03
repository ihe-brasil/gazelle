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

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.servletfilter.FileGenerator;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.objects.model.*;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.util.ThumbnailCreator;
import org.apache.commons.io.FileUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.kohsuke.MetaInfServices;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Abderrazek Boufahja / INRIA Rennes IHE development Project
 */

@Name("objectInstanceManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("ObjectInstanceManagerLocal")
@MetaInfServices(FileGenerator.class)
public class ObjectInstanceManager implements ObjectInstanceManagerLocal, Serializable, FileGenerator {

    private static final long serialVersionUID = -8734856733751607120L;

    // ~ Statics variables and Class initializer /////////////////////////////////////////

    private static final Logger LOG = LoggerFactory.getLogger(ObjectInstanceManager.class);
    private static String SAMPLE_PAGE = "/objects/sample.seam";

    private static String OBJECTS_PAGE = "/objects/system_object.seam";

    private static String UPLOAD = "upload";

    private static String WRITE = "write";

    // ~ Attribute ///////////////////////////////////////////////////////////////////////

    private ObjectInstance selectedObjectInstance;

    private ObjectFile selectedObjectFile;

    private ObjectInstanceFile selectedObjectInstanceFile;

    private List<ObjectAttribute> objectTypeAttributes;

    private List<ObjectAttribute> objectAttributesOfSelectedObjectInstance;

    private List<ObjectInstanceAttribute> objectInstanceAttributes;

    private List<ObjectFile> objectTypeFiles;

    private boolean registrationDisabled;

    /**
     * This is a temp file created at upload time, it will be move to the correct location later
     */
    private File uploadedFile;

    private String returnPage;

    private String downloadFile = UPLOAD;

    private String sampleContentText;

    private String sampleContentName;

    private transient String uploadedFilename;

    public String backToSamplePage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("backToSamplePage");
        }
        return "/objects/sample.seam?id=" + selectedObjectInstance.getId();
    }

    @Override
    public String getSampleContentName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSampleContentName");
        }
        return sampleContentName;
    }

    @Override
    public void setSampleContentName(String sampleContentName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSampleContentName");
        }
        this.sampleContentName = sampleContentName;
    }

    @Override
    public String getSampleContentText() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSampleContentText");
        }
        return sampleContentText;
    }

    @Override
    public void setSampleContentText(String sampleContentText) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSampleContentText");
        }
        this.sampleContentText = sampleContentText;
    }

    @Override
    public String getDownloadFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDownloadFile");
        }
        return downloadFile;
    }

    @Override
    public void setDownloadFile(String downloadFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDownloadFile");
        }
        this.downloadFile = downloadFile;
    }

    @Override
    public String getReturnPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReturnPage");
        }
        return returnPage;
    }

    @Override
    public void setReturnPage(String returnPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReturnPage");
        }
        this.returnPage = returnPage;
    }

    @Override
    public boolean isRegistrationDisabled() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRegistrationDisabled");
        }
        return registrationDisabled;
    }

    @Override
    public void setRegistrationDisabled(boolean registrationDisabled) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRegistrationDisabled");
        }
        this.registrationDisabled = registrationDisabled;
    }

    @Override
    public ObjectInstance getSelectedObjectInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectInstance");
        }
        return selectedObjectInstance;
    }

    @Override
    public void setSelectedObjectInstance(ObjectInstance selectedObjectInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectInstance");
        }
        this.selectedObjectInstance = selectedObjectInstance;
    }

    @Override
    public List<ObjectAttribute> getObjectTypeAttributes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getObjectTypeAttributes");
        }
        return objectTypeAttributes;
    }

    @Override
    public void setObjectTypeAttributes(List<ObjectAttribute> objectTypeAttributes) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setObjectTypeAttributes");
        }
        this.objectTypeAttributes = objectTypeAttributes;
    }

    @Override
    public List<ObjectInstanceAttribute> getObjectInstanceAttributes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getObjectInstanceAttributes");
        }
        return objectInstanceAttributes;
    }

    @Override
    public void setObjectInstanceAttributes(List<ObjectInstanceAttribute> objectInstanceAttributes) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setObjectInstanceAttributes");
        }
        this.objectInstanceAttributes = objectInstanceAttributes;
    }

    @Override
    public List<ObjectFile> getObjectTypeFiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getObjectTypeFiles");
        }
        return objectTypeFiles;
    }

    @Override
    public void setObjectTypeFiles(List<ObjectFile> objectTypeFiles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setObjectTypeFiles");
        }
        this.objectTypeFiles = objectTypeFiles;
    }

    @Override
    public List<ObjectAttribute> getObjectAttributesOfSelectedObjectInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getObjectAttributesOfSelectedObjectInstance");
        }
        return objectAttributesOfSelectedObjectInstance;
    }

    @Override
    public void setObjectAttributesOfSelectedObjectInstance(
            List<ObjectAttribute> objectAttributesOfSelectedObjectInstance) {
        this.objectAttributesOfSelectedObjectInstance = objectAttributesOfSelectedObjectInstance;
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
    public ObjectInstanceFile getSelectedObjectInstanceFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectInstanceFile");
        }
        return selectedObjectInstanceFile;
    }

    @Override
    public void setSelectedObjectInstanceFile(ObjectInstanceFile selectedObjectInstanceFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectInstanceFile");
        }
        this.selectedObjectInstanceFile = selectedObjectInstanceFile;
    }

    @Override
    public void initializeForCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeForCreator");
        }
        if (selectedObjectInstance != null) {
            objectTypeAttributes = ObjectAttribute.getObjectAttributeFiltered(selectedObjectInstance.getObject());
            objectInstanceAttributes = ObjectInstanceAttribute
                    .getObjectInstanceAttributeFiltered(selectedObjectInstance);
            objectTypeFiles = ObjectFile.getObjectFileFiltered(selectedObjectInstance.getObject(), "creator");

            objectAttributesOfSelectedObjectInstance = new ArrayList<ObjectAttribute>();
            for (ObjectInstanceAttribute objectInstanceAttribute : objectInstanceAttributes) {
                objectAttributesOfSelectedObjectInstance.add(objectInstanceAttribute.getAttribute());
            }
        }
    }

    @Override
    public void initializeForReaders() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeForReaders");
        }
        if (selectedObjectInstance != null) {
            objectTypeFiles = ObjectFile.getObjectFileFiltered(selectedObjectInstance.getObject(), "reader");
            registrationDisabled = true;
        }
    }

    @Override
    public List<ObjectAttribute> getAttributesOfObjectTypeForSelectedObjectInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributesOfObjectTypeForSelectedObjectInstance");
        }
        List<ObjectAttribute> result = new ArrayList<ObjectAttribute>();
        if (selectedObjectInstance != null) {
            result = ObjectAttribute.getObjectAttributeFiltered(selectedObjectInstance.getObject());
        } else {

        }
        return result;
    }

    @Override
    public boolean canAddObjectInstanceToCurrentObjectFile(ObjectFile inObjectFile, SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canAddObjectInstanceToCurrentObjectFile");
        }
        if (inObjectFile != null) {
            int numberOfInstanceFile = this.getNumberObjectInstanceFileForSelectedObjectInstance(inObjectFile,
                    inSystemInSession);
            if (inObjectFile.getMax() > numberOfInstanceFile) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canAddObjectInstanceToCurrentObjectFile(ObjectFile inObjectFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canAddObjectInstanceToCurrentObjectFile");
        }
        if (this.selectedObjectInstance == null) {
            return false;
        }
        if (this.selectedObjectInstance.getSystem() == null) {
            return false;
        }
        return this.canAddObjectInstanceToCurrentObjectFile(inObjectFile, this.selectedObjectInstance.getSystem());
    }

    private int getNumberObjectInstanceFileForSelectedObjectInstance(ObjectFile objectFile,
                                                                     SystemInSession inSystemInSession) {
        if ((objectFile != null) && (inSystemInSession != null)) {
            List<ObjectInstanceFile> listOIF = this.getListObjectInstanceFileForSelectedObjectInstance(objectFile,
                    inSystemInSession);
            return listOIF.size();
        } else {
            return 1000;
        }
    }

    @Override
    public List<ObjectInstanceFile> getListObjectInstanceFileForSelectedObjectInstance(ObjectFile inObjectFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListObjectInstanceFileForSelectedObjectInstance");
        }
        if (this.selectedObjectInstance == null) {
            return null;
        }
        if (this.selectedObjectInstance.getSystem() == null) {
            return null;
        }
        return this.getListObjectInstanceFileForSelectedObjectInstance(inObjectFile,
                this.selectedObjectInstance.getSystem());
    }

    @Override
    public List<ObjectInstanceFile> getListObjectInstanceFileForSelectedObjectInstance(ObjectFile objectFile,
                                                                                       SystemInSession inSystemInSession) {
        if ((objectFile != null) && (this.selectedObjectInstance != null) && (inSystemInSession != null)) {
            if ((objectFile.getId() != null) && (this.selectedObjectInstance.getId() != null)) {
                List<ObjectInstanceFile> list_OIF = ObjectInstanceFile.getObjectInstanceFileFiltered(
                        selectedObjectInstance, objectFile, inSystemInSession, null, null);
                Collections.sort(list_OIF);
                return list_OIF;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void listener(FileUploadEvent event) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listener");
        }
        UploadedFile item = event.getUploadedFile();
        uploadedFilename = item.getName();
        uploadedFile = File.createTempFile("sample", uploadedFilename);
        try (FileOutputStream fos = new FileOutputStream(uploadedFile)) {
            fos.write(item.getData());
            fos.close();
        }
        this.registrationDisabled = false;
    }

    @Override
    public void updateSelectedInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedInstance");
        }
        updateSelectedInstanceOnly();
        updateListAttribute();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Sample summary updated");
    }

    private void updateSelectedInstanceOnly() {
        if (selectedObjectInstance != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedObjectInstance = entityManager.merge(selectedObjectInstance);
            entityManager.flush();
        }
    }

    @Override
    public String mergeSelectedInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeSelectedInstance");
        }
        List<ObjectInstance> loi = ObjectInstance.getObjectInstanceFiltered(null, this.selectedObjectInstance.getSystem(), null,
                this.selectedObjectInstance.getName());
        if (loi != null) {
            if (loi.size() > 0) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This name is already used, for this system in that testing session. Choose please an other name to your sample.");
                return null;
            }
        }
        updateSelectedInstanceOnly();
        this.initializeObjectInstanceAndReturnToObjects(this.selectedObjectInstance);
        return "/objects/EditSample.xhtml";
    }

    private void updateListAttribute() {
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (objectAttributesOfSelectedObjectInstance != null) {
            objectInstanceAttributes = ObjectInstanceAttribute
                    .getObjectInstanceAttributeFiltered(selectedObjectInstance);
            for (ObjectInstanceAttribute objectInstanceAttribute : objectInstanceAttributes) {
                ObjectInstanceAttribute.deleteObjectInstanceAttribute(objectInstanceAttribute);
            }
            for (ObjectAttribute objectAttribute : objectAttributesOfSelectedObjectInstance) {
                ObjectInstanceAttribute objectInstanceAttribute = new ObjectInstanceAttribute(
                        this.selectedObjectInstance, objectAttribute, "");
                objectInstanceAttribute = entityManager.merge(objectInstanceAttribute);
                entityManager.flush();
            }
        }
    }

    @Override
    public void addNewObjectInstanceFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewObjectInstanceFile");
        }
        if (this.selectedObjectInstance == null) {
            return;
        }
        if (this.selectedObjectInstance.getSystem() == null) {
            return;
        }
        this.addNewObjectInstanceFile(this.selectedObjectInstance.getSystem());

    }

    @Override
    public void addNewObjectInstanceFile(SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewObjectInstanceFile");
        }
        if (this.downloadFile.equals(UPLOAD)) {
            if ((selectedObjectFile != null) && (this.uploadedFile != null) && (inSystemInSession != null)) {
                saveObjectFile(inSystemInSession, uploadedFilename);
            }
        } else if (this.downloadFile.equals(WRITE)) {
            boolean isEmpty = true;
            if (this.sampleContentText != null) {
                if (!this.sampleContentText.equals("")) {
                    isEmpty = false;
                }
            }
            if (!isEmpty) {
                if ((this.selectedObjectFile != null) && (inSystemInSession != null)) {
                    String filename = createFileNameForFileToSave(inSystemInSession);
                    filename = refactorFileName(filename);
                    FileOutputStream fos = null;
                    try {
                        uploadedFile = File.createTempFile("sample", filename);
                        fos = new FileOutputStream(uploadedFile);
                        fos.write(sampleContentText.getBytes(Charset.forName(StandardCharsets.UTF_8.name())));
                    } catch (FileNotFoundException e) {
                        LOG.error(e.getMessage(), e);
                    } catch (IOException e) {
                        LOG.error(e.getMessage());
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                LOG.error(e.getMessage());
                            }
                        }
                    }
                    saveObjectFile(inSystemInSession, filename);
                }
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                        "The content of the inputText area is empty, you have to add the content of the file to add.");
            }
        }
    }

    private String refactorFileName(String filename) {
        if (filename != null && !filename.isEmpty()) {
            if (filename.contains("é")) {
                filename = filename.replace("é", "e");
            }
            if (filename.contains("è")) {
                filename = filename.replace("è", "e");
            }
            if (filename.contains("ê")) {
                filename = filename.replace("ê", "e");
            }
            if (filename.contains("â")) {
                filename = filename.replace("â", "a");
            }
            if (filename.contains("à")) {
                filename = filename.replace("à", "a");
            }
            if (filename.contains("ù")) {
                filename = filename.replace("ù", "u");
            }
            if (filename.contains("û")) {
                filename = filename.replace("û", "u");
            }
            if (filename.contains("ü")) {
                filename = filename.replace("ü", "u");
            }
            if (filename.contains("î")) {
                filename = filename.replace("î", "i");
            }
            if (filename.contains("ï")) {
                filename = filename.replace("ï", "i");
            }
            if (filename.contains("ô")) {
                filename = filename.replace("ô", "o");
            }
            if (filename.contains("ö")) {
                filename = filename.replace("ö", "o");
            }
            if (filename.contains("ç")) {
                filename = filename.replace("ç", "c");
            }

            Pattern regex = Pattern.compile("[^0-9A-Za-z._]+", Pattern.DOTALL);
            Matcher regexMatcher = regex.matcher(filename);
            if (regexMatcher.find()) {
                filename = regexMatcher.replaceAll("_");
            }
            return filename;
        } else {
            return null;
        }
    }

    private void saveObjectFile(SystemInSession inSystemInSession, String filename) {
        ObjectInstanceFile objectInstanceFile = new ObjectInstanceFile(selectedObjectInstance, selectedObjectFile,
                inSystemInSession, filename);
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        objectInstanceFile = entityManager.merge(objectInstanceFile);
        entityManager.flush();
        storeUploadedFileOnDisk(objectInstanceFile);
    }

    private String createFileNameForFileToSave(SystemInSession inSystemInSession) {
        String res = "";
        List<String> listIndex = Arrays.asList(this.selectedObjectFile.getType().getExtensions().split(","));
        if (this.sampleContentName != null) {
            if (!this.sampleContentName.isEmpty()) {
                int i = this.sampleContentName.indexOf(".");
                if (i > 0) {
                    String index = this.sampleContentName.substring(i);
                    if ((listIndex.contains(index)) || (listIndex.size() == 0)) {
                        return this.sampleContentName;
                    } else {
                        return this.sampleContentName.substring(0, i) + "." + listIndex.get(0);
                    }
                } else {
                    res = this.sampleContentName;
                    if (listIndex.size() > 0) {
                        res = res + "." + listIndex.get(0);
                    }
                }
            } else {
                res = this.getNameFromSystemAndListExtensions(inSystemInSession, listIndex);
            }
        } else {
            res = this.getNameFromSystemAndListExtensions(inSystemInSession, listIndex);
        }
        return res;
    }

    private String getNameFromSystemAndListExtensions(SystemInSession inSystemInSession, List<String> listIndex) {
        String res = "";
        if (inSystemInSession != null) {
            if (inSystemInSession.getSystem() != null) {
                String keyword = inSystemInSession.getSystem().getKeyword();
                if (keyword != null) {
                    res = keyword;
                }
            }
        }
        if (listIndex != null) {
            if (listIndex.size() > 0) {
                res = res + "." + listIndex.get(0);
            }
        }
        return res;
    }

    /**
     * save the file uploaded
     *
     * @param objectInstanceFile : the correspondant objectInstanceFile
     * @param inFile             : the content of the file
     */
    private void storeUploadedFileOnDisk(ObjectInstanceFile objectInstanceFile) {
        String ObjectPath = ApplicationManager.instance().getObjectsPath();
        String directoryPathForThisFile = ObjectPath + File.separatorChar + objectInstanceFile.getInstance().getId();
        File newDirectoryForFile = new File(directoryPathForThisFile);
        if (newDirectoryForFile.mkdirs()) {
            LOG.info("newDirectoryForFile directory created");
        } else {
            LOG.error("Failed to create newDirectoryForFile");
        }
        String directoryPathForThisFileInstance = directoryPathForThisFile + File.separatorChar
                + objectInstanceFile.getFile().getId();
        File newDirectoryForFileInstance = new File(directoryPathForThisFileInstance);
        if (newDirectoryForFileInstance.mkdirs()) {
            LOG.info("newDirectoryForFileInstance directory created");
        } else {
            LOG.error("Failed to create newDirectoryForFileInstance");
        }

        String filePathForThisFileInstance = directoryPathForThisFileInstance + File.separatorChar
                + objectInstanceFile.getId() + "_" + objectInstanceFile.getUrl();
        File fileToStore = new File(filePathForThisFileInstance);
        try {
            if (fileToStore.exists()) {
                if (fileToStore.delete()) {
                    LOG.info("fileToStore deleted");
                } else {
                    LOG.error("Failed to delete fileToStore");
                }
            }
            if (uploadedFile != null && uploadedFile.exists()) {
                FileUtils.moveFile(uploadedFile, fileToStore);
                if (!fileToStore.exists()) {
                    LOG.error("Unable to move the sample from /tmp to gazelle directory");
                    LOG.error("tmp file: " + uploadedFile.getAbsolutePath());
                    LOG.error("dest file: " + filePathForThisFileInstance);
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Unable to move the sample from /tmp to gazelle directory");
                } else if (objectInstanceFile.getFile().getType().getKeyword().equals("SNAPSHOT")) {
                    try {
                        this.createThumbnail(filePathForThisFileInstance);
                    } catch (InterruptedException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
            FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, e.getMessage());
        } catch (SecurityException e) {
            LOG.error(e.getMessage(), e);
        } catch (NullPointerException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void createThumbnail(String filename) throws FileNotFoundException, InterruptedException, IOException {
        String newFileName = ThumbnailCreator.createNewFileName(filename);
        ThumbnailCreator.createThumbnail(filename, 80, 80, 100, newFileName);
    }

    @Override
    public void deleteSelectedObjectInstanceFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedObjectInstanceFile");
        }
        deleteObjectInstanceFile(selectedObjectInstanceFile);
    }

    @Override
    public void deleteObjectInstanceFile(ObjectInstanceFile inObjectInstanceFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteObjectInstanceFile");
        }
        if (inObjectInstanceFile != null) {
            ObjectInstanceFile.deleteObjectInstanceFile(inObjectInstanceFile);
        }
    }

    @Override
    public void initialiseSelectedObjectInstance(ObjectType objectType, SystemInSession SIS) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initialiseSelectedObjectInstance");
        }
        selectedObjectInstance = new ObjectInstance(objectType, SIS, objectType.getDefault_desc(), "", false, null);
        selectedObjectInstance.setCompleted(true);
    }

    @Override
    public void initializeObjectInstanceAndReturnToSample(ObjectInstance oi, boolean b) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeObjectInstanceAndReturnToSample");
        }
        if (b) {
            initializeObjectInstanceAndReturnToObjects(oi);
        } else {
            this.selectedObjectInstance = oi;
            this.setReturnPage(SAMPLE_PAGE);
        }
    }

    @Override
    public void initializeObjectInstanceAndReturnToObjects(ObjectInstance oi) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeObjectInstanceAndReturnToObjects");
        }
        this.selectedObjectInstance = oi;
        this.setReturnPage(OBJECTS_PAGE);
    }

    @Override
    public boolean returnPageIsSample() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("returnPageIsSample");
        }
        if (this.returnPage != null) {
            if (this.returnPage.equals(SAMPLE_PAGE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initializeDownloadFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeDownloadFile");
        }
        this.setDownloadFile(UPLOAD);
        this.setSampleContentText(null);
        this.setSampleContentName(null);
    }

    @Override
    public boolean canUploadFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canUploadFile");
        }
        if (this.downloadFile != null) {
            if (this.downloadFile.equals(UPLOAD)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean userCanWriteContent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("userCanWriteContent");
        }
        if (this.selectedObjectFile != null) {
            if (this.selectedObjectFile.getType() != null) {
                return this.selectedObjectFile.getType().userCanWriteContent();
            }
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

    public File getUploadedFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUploadedFile");
        }
        return uploadedFile;
    }

    public void setUploadedFile(File uploadedFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setUploadedFile");
        }
        this.uploadedFile = uploadedFile;
    }


    public String getPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPath");
        }
        return "/sampleSnapshot/";
    }

    @Override
    public void process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("process");
        }
        String servletPath = httpServletRequest.getServletPath();
        StringTokenizer stringTokenizer = new StringTokenizer(servletPath, "/");
        stringTokenizer.nextToken();
        String size = stringTokenizer.nextToken();
        String id = stringTokenizer.nextToken();
        ObjectInstanceFileQuery objectInstFileQ = new ObjectInstanceFileQuery();
        objectInstFileQ.id().eq(Integer.parseInt(id));
        ObjectInstanceFile oif = objectInstFileQ.getUniqueResult();
        String filePath = null;
        if ("thumbnail".equals(size)) {
            filePath = oif.getThumbnailFileAbsolutePath();
        } else {
            filePath = oif.getFileAbsolutePath();
        }
        String fileName = oif.getUrl();
        try {
            net.ihe.gazelle.common.util.DocumentFileUpload.showFile(httpServletRequest, httpServletResponse,
                    new FileInputStream(filePath), fileName, false);
        } catch (FileNotFoundException e) {
            LOG.error("File not found : " + filePath, e);
            throw new IllegalArgumentException("File not found : " + filePath, e);
        }
    }
}
