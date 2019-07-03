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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ValidateDocumentUrl {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateDocumentUrl.class);
    URL url;
    HttpURLConnection urlConnection;

    public ValidateDocumentUrl() {
        HttpURLConnection.setFollowRedirects(false);
    }

    public boolean connect() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("connect");
        }
        String protocol = url.getProtocol();
        boolean status = false;
        if (protocol.equals("http") | protocol.equals("https")) {
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("HEAD");
                status = true;
            } catch (IOException e) {
                return false;
            }
        }
        return status;
    }

    public void disconnect() {
        LOG.debug("disconnect");
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    public boolean isThisUrlRespondingHTTP_OK() throws IOException {
        LOG.debug("isThisUrlRespondingHTTP_OK");
        if (urlConnection != null) {
            return urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
        }
        return false;
    }

    public boolean isthisUrlPointingToApdf() throws IOException {
        LOG.debug("isthisUrlPointingToApdf");
        if (urlConnection != null) {
            return urlConnection.getContentType().equalsIgnoreCase("application/pdf");
        }
        return false;
    }

    public URL getUrl() {
        LOG.debug("getUrl");
        return url;
    }

    public void setUrl(String url) throws MalformedURLException {
        LOG.debug("setUrl");
        this.url = new URL(url);
        String protocol = this.url.getProtocol();
        if (!(protocol.equals("http") | protocol.equals("https"))) {
            throw new MalformedURLException();
        }
        urlConnection = null;
    }
}
