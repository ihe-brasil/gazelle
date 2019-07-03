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
import net.ihe.gazelle.util.Md5Encryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author jean-francois
 * @version $Revision: 1.0 $
 */
public class GetDocumentFromUrl {

    private static final Logger LOG = LoggerFactory.getLogger(GetDocumentFromUrl.class);
    byte[] document_from_web;
    URL url;
    Document document;

    public GetDocumentFromUrl(URL url) {
        this.url = url;
    }

    /**
     * Method getDocument. Retreive the pdf document Stores it in document_from_web.
     *
     * @throws IOException
     */
    public void getDocument() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocument");
        }
        int bytesRead = 0;
        int offset = 0;

        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
        int contentLength = httpUrlConnection.getContentLength();
        document_from_web = new byte[contentLength];

        InputStream is = new BufferedInputStream(httpUrlConnection.getInputStream());

        while (offset < contentLength) {
            bytesRead = is.read(document_from_web, offset, document_from_web.length - offset);
            if (bytesRead == -1) {
                break;
            }
            offset += bytesRead;
        }
        is.close();

        if (offset != contentLength) {
            throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
        }
    }

    /**
     * Method saveDocument.
     *
     * @throws IOException
     */
    public void saveDocument() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveDocument");
        }
        try (FileOutputStream out = new FileOutputStream("download.pdf")) {
            out.write(document_from_web);
            out.flush();
            out.close();
        }
    }

    /**
     * Method computeDocumentMD5.
     *
     * @return String
     * @throws Exception
     */
    public String computeDocumentMD5() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("computeDocumentMD5");
        }
        return Md5Encryption.calculateMD5ChecksumForInputStream(new ByteArrayInputStream(document_from_web));
    }

}
