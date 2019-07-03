package net.ihe.gazelle.tm.gazelletest.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfoCookies {

    private static final Logger LOG = LoggerFactory.getLogger(InfoCookies.class);
    private String cookieName;

    private String cookieFullName;

    private String cookieUrl;

    private String encodedCookieUri;

    private boolean fav;

    public InfoCookies(String cookieFullName, String cookieName, String cookieUrl) {
        super();
        this.cookieName = cookieName;
        this.cookieFullName = cookieFullName;
        this.cookieUrl = cookieUrl;
        this.fav = false;
    }

    public String getEncodedCookieUri() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEncodedCookieUri");
        }
        return encodedCookieUri;
    }

    public void setEncodedCookieUri(String encodedCookieUri) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEncodedCookieUri");
        }
        this.encodedCookieUri = encodedCookieUri;
    }

    public String getCookieFullName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCookieFullName");
        }
        return cookieFullName;
    }

    public void setCookieFullName(String cookieFullName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCookieFullName");
        }
        this.cookieFullName = cookieFullName;
    }

    public boolean isFav() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isFav");
        }
        return fav;
    }

    public void setFav(boolean fav) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFav");
        }
        this.fav = fav;
    }

    public String getCookieName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCookieName");
        }
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCookieName");
        }
        this.cookieName = cookieName;
    }

    public String getCookieUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCookieUrl");
        }
        return cookieUrl;
    }

    public void setCookieUrl(String cookieUrl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCookieUrl");
        }
        this.cookieUrl = cookieUrl;
    }

}
