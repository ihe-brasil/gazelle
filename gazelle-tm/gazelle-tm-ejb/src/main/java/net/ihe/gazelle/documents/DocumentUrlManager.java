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

package net.ihe.gazelle.documents;

import net.ihe.gazelle.tf.model.Document;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;

public class DocumentUrlManager {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentUrlManager.class);
    static ValidateDocumentUrl urlChecker = new ValidateDocumentUrl();
    Document document;
    GetDocumentFromUrl document_pdf;

    public DocumentUrlManager(Document doc) {
        this.document = doc;
    }

    public DocumentUrlManager() {
    }

    public static DocumentURLStatusType checkUrlIsPointingToPDF(String url) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("DocumentURLStatusType checkUrlIsPointingToPDF");
        }
        urlChecker = new ValidateDocumentUrl();
        DocumentURLStatusType urlStatus = DocumentURLStatusType.CHECKING;
        if (url != null) {
            boolean urlIsResponding = false;
            boolean urlIsPointingToApdf = false;
            try {
                urlChecker.setUrl(url);
            } catch (MalformedURLException e1) {
                urlStatus = DocumentURLStatusType.THIS_URL_IS_NOT_WELL_FORMED;
            }
            if (urlStatus.equals(DocumentURLStatusType.CHECKING)) {
                urlChecker.connect();
                try {
                    urlIsResponding = urlChecker.isThisUrlRespondingHTTP_OK();
                } catch (IOException e1) {
                    urlStatus = DocumentURLStatusType.THIS_URL_IS_NOT_RESPONDING;
                }
                if (urlIsResponding) {
                    try {
                        urlIsPointingToApdf = urlChecker.isthisUrlPointingToApdf();
                    } catch (IOException e1) {
                        urlStatus = DocumentURLStatusType.THIS_URL_IS_NOT_RESPONDING;
                    }
                    if (urlIsPointingToApdf) {
                        urlStatus = DocumentURLStatusType.THIS_URL_IS_POINTING_TO_A_PDF;
                    } else {
                        urlStatus = DocumentURLStatusType.THIS_URL_IS_NOT_POINTING_TO_A_PDF_FILE;
                    }
                } else {
                    urlStatus = DocumentURLStatusType.THIS_URL_IS_NOT_RESPONDING;
                }
            }
        } else {
            urlStatus = DocumentURLStatusType.PLEASE_PROVIDE_A_URL;
        }
        urlChecker.disconnect();
        return urlStatus;
    }

    public boolean analyseUrl(String url) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("analyseUrl");
        }
        DocumentURLStatusType urlStatus = checkUrlStatus(url);
        if (urlStatus.equals(DocumentURLStatusType.OK)) {
            return true;
        } else {
            sendWarnMessage(urlStatus.getFriendlyName());
            return false;
        }
    }

    public DocumentURLStatusType checkUrlStatus(String url) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkUrlStatus");
        }
        DocumentURLStatusType urlStatus = checkUrlIsPointingToPDF(url);
        if (urlStatus.equals(DocumentURLStatusType.THIS_URL_IS_POINTING_TO_A_PDF)) {
            urlStatus = retreiveDocumentInformationFromUrl(url);
        }
        return urlStatus;
    }

    private DocumentURLStatusType retreiveDocumentInformationFromUrl(String url) {
        DocumentURLStatusType urlStatus;
        document_pdf = new GetDocumentFromUrl(urlChecker.getUrl());
        try {
            document_pdf.getDocument();
        } catch (IOException e1) {
            urlStatus = DocumentURLStatusType.CANNOT_RETRIEVE_THE_PDF_FILE;
        }
        String md5Code;
        try {
            md5Code = document_pdf.computeDocumentMD5();
            document.setDocument_md5_hash_code(md5Code);
            document.setName(getNameFromFilePathWithoutExtension(url));
            urlStatus = DocumentURLStatusType.OK;
        } catch (Exception e) {
            urlStatus = DocumentURLStatusType.CANNOT_COMPUTE_DOCUMENT_MD5;
        }
        return urlStatus;
    }

    private void sendWarnMessage(String message) {
        StatusMessages.instance().addToControlFromResourceBundleOrDefault("newDocumentUrlInput",
                StatusMessage.Severity.WARN, "gazelle.tm.documents.urlError", message);
    }

    /**
     * Method getNameFromFilePathWithoutExtension.
     *
     * @param file String
     *             <p/>
     *             file is structured as follow : /some/path/to/my_file.pdf getNameFromFilePathWithoutExtension takes the file string as a
     *             parameter and splits it to keep the last part, here:
     *             my_file.pdf then it removes the extension from the file name
     */
    public String getNameFromFilePathWithoutExtension(String file) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNameFromFilePathWithoutExtension");
        }
        String result = "";
        if (file != null) {
            if (file.length() != 0) {
                String[] tmp = file.split("/");
                if (tmp.length != 0) {
                    result = tmp[tmp.length - 1];
                }

                // Remove extension from file name
                int indexOfDot = result.lastIndexOf('.');
                if (indexOfDot != -1) {
                    result = result.substring(0, indexOfDot);
                }
            }
        }
        return result;
    }

}
