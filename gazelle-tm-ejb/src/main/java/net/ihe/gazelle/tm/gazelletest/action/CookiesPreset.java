package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tm.gazelletest.bean.InfoCookies;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemAIPOResultForATestingSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import org.apache.commons.codec.binary.Base64;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Scope(ScopeType.SESSION)
@Name("cookiesPreset")
@GenerateInterface("CookiesPresetLocal")
public class CookiesPreset implements CookiesPresetLocal, Serializable {

    private static final long serialVersionUID = 7458309991005248929L;
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
    private static final Logger LOG = LoggerFactory.getLogger(CookiesPreset.class);
    private final String APP_BASENAME = ApplicationPreferenceManager.getStringValue("application_url_basename");
    private String shortcutName;

    private String selectedCatPreset = "None";

    private String selectedPreCatPreset = "None";

    private String selectedTestPreset = "None";

    private boolean mainPageCookie;

    public static CookiesPresetLocal instance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("CookiesPresetLocal instance");
        }
        return (CookiesPresetLocal) Component.getInstance("cookiesPreset");
    }

    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

    public boolean isMainPageCookie() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMainPageCookie");
        }
        return mainPageCookie;
    }

    public void setMainPageCookie(boolean mainPageCookie) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMainPageCookie");
        }
        this.mainPageCookie = mainPageCookie;
    }

    public String getSelectedCatPreset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedCatPreset");
        }
        if (selectedCatPreset == null) {
            setSelectedCatPreset("None");
        }
        return selectedCatPreset;
    }

    public void setSelectedCatPreset(String selectedCatPreset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedCatPreset");
        }
        this.selectedCatPreset = selectedCatPreset;
    }

    public String getSelectedPreCatPreset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedPreCatPreset");
        }
        if (selectedPreCatPreset == null) {
            setSelectedCatPreset("None");
        }
        return selectedPreCatPreset;
    }

    public void setSelectedPreCatPreset(String selectedPreCatPreset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedPreCatPreset");
        }
        this.selectedPreCatPreset = selectedPreCatPreset;
    }

    public String getSelectedTestPreset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestPreset");
        }
        if (selectedTestPreset == null) {
            setSelectedTestPreset("None");
        }
        return selectedTestPreset;
    }

    public void setSelectedTestPreset(String selectedPreCatPreset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestPreset");
        }
        this.selectedTestPreset = selectedPreCatPreset;
    }

    public String getShortcutName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getShortcutName");
        }
        return shortcutName;
    }

    public void setShortcutName(String shortcutName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShortcutName");
        }
        this.shortcutName = shortcutName;
    }

    // PreCat and Cat presets
    public boolean containCookie(String cookieBaseName, Filter<SystemAIPOResultForATestingSession> filter,
                                 Filter<TestInstanceParticipants> filter2) {
        return containCookieMain(cookieBaseName, filter, filter2, null);
    }

    public String getUriWithParam(HttpServletRequest request, Filter<SystemAIPOResultForATestingSession> filter,
                                  Filter<TestInstanceParticipants> filter2) {
        return getUriWithParamMain(request, filter, filter2, null);
    }

    public String getPresetName(String cookieBaseName, Filter<SystemAIPOResultForATestingSession> filter,
                                Filter<TestInstanceParticipants> filter2) {
        return getPresetNameMain(cookieBaseName, filter, filter2, null);
    }

    public void createCookie(String cookieBaseName, Filter<SystemAIPOResultForATestingSession> filter,
                             Filter<TestInstanceParticipants> filter2) {
        createCookieMain(cookieBaseName, filter, filter2, null, "/testing/test");
    }

    public void createSimpleCookie(String cookieName, String encodedUri) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createSimpleCookie");
        }
        createSimpleCookieMain(cookieName, encodedUri, "/testing/test");
    }

    public void deleteCookieToLoad(String cookieName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteCookieToLoad");
        }
        deleteCookieToLoadMain(cookieName, "/testing/test");
    }

    public void deleteSelectedCookie(String presetName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedCookie");
        }
        deleteSelectedCookieMain(presetName, "/testing/test");
    }

    // tests definition presets
    public boolean containTestCookie(String cookieBaseName, Filter<Test> filter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("containTestCookie");
        }
        return containCookieMain(cookieBaseName, null, null, filter);
    }

    public String getUriWithParamTest(HttpServletRequest request, Filter<Test> filter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUriWithParamTest");
        }
        return getUriWithParamMain(request, null, null, filter);
    }

    public String getPresetNameTest(String cookieBaseName, Filter<Test> filter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPresetNameTest");
        }
        return getPresetNameMain(cookieBaseName, null, null, filter);
    }

    public void createCookieTest(String cookieBaseName, Filter<Test> filter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createCookieTest");
        }
        createCookieMain(cookieBaseName, null, null, filter, "/testing/testsDefinition");
    }

    public void createSimpleTestCookie(String cookieName, String encodedUri) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createSimpleTestCookie");
        }
        createSimpleCookieMain(cookieName, encodedUri, "/testing/testsDefinition");
    }

    public void deleteCookieToLoadTest(String cookieName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteCookieToLoadTest");
        }
        deleteCookieToLoadMain(cookieName, "/testing/testsDefinition");
    }

    public void deleteSelectedTestCookie(String presetName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedTestCookie");
        }
        deleteSelectedCookieMain(presetName, "/testing/testsDefinition");

    }

    // Main classes
    public boolean containCookieMain(String cookieBaseName, Filter<SystemAIPOResultForATestingSession> filter,
                                     Filter<TestInstanceParticipants> filter2, Filter<Test> filter3) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        String uri = getUriWithParamMain(request, filter, filter2, filter3);
        if (uri != null) {
            // encoding byte array into base 64
            String encodedUri;
            try {
                encodedUri = Base64.encodeBase64URLSafeString(uri.getBytes(UTF_8));

                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().contains(cookieBaseName) && cookie.getValue().equals(encodedUri)) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (UnsupportedEncodingException e) {
                LOG.error("" + e);
                return false;
            }
        }
        return false;
    }

    public String getUriWithParamMain(HttpServletRequest request, Filter<SystemAIPOResultForATestingSession> filter,
                                      Filter<TestInstanceParticipants> filter2, Filter<Test> filter3) {
        String param = null;
        if (filter != null) {
            param = filter.getUrlParameters();
        } else if (filter2 != null) {
            param = filter2.getUrlParameters();
        } else {
            param = filter3.getUrlParameters();
        }

        String uri = request.getRequestURI().toString();
        if (uri.contains(APP_BASENAME)) {
            uri = uri.substring(APP_BASENAME.length() + 1);
            StringBuilder sb = new StringBuilder(uri);
            if (param != null) {
                sb.append("?").append(param);
            }
            return uri = sb.toString();
        } else {
            return null;
        }
    }

    public String getPresetNameMain(String cookieBaseName, Filter<SystemAIPOResultForATestingSession> filter,
                                    Filter<TestInstanceParticipants> filter2, Filter<Test> filter3) {

        String presetName = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        String uri = getUriWithParamMain(request, filter, filter2, filter3);
        if (uri != null) {
            // encoding byte array into base 64
            String encodedUri;
            try {
                encodedUri = Base64.encodeBase64URLSafeString(uri.getBytes(UTF_8));
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().contains(cookieBaseName) && cookie.getValue().equals(encodedUri)) {
                            presetName = cookie.getName().substring(cookieBaseName.length() + 3);
                        }
                    }
                }
            } catch (UnsupportedEncodingException e) {
                LOG.error("" + e);
            }
        }
        return presetName;
    }

    public void createCookieMain(String cookieBaseName, Filter<SystemAIPOResultForATestingSession> filter,
                                 Filter<TestInstanceParticipants> filter2, Filter<Test> filter3, String path) {

        String cookieName = null;
        String uri = null;

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        uri = getUriWithParamMain(request, filter, filter2, filter3);
        if (uri != null) {
            // encoding byte array into base 64
            String encodedUri;
            try {
                encodedUri = Base64.encodeBase64URLSafeString(uri.getBytes(UTF_8));

                // Remove space and special characters from preset name
                String shortcut = getShortcutName().trim();
                String regExp = "[^\\w\\d]";
                shortcut = shortcut.replaceAll(regExp, "");
                if (shortcut.contains(" ")) {
                    shortcut = shortcut.replace(" ", "_");
                }

                if (shortcut != null && !shortcut.isEmpty()) {

                    cookieName = cookieBaseName + "_" + "1_" + shortcut;
                    Cookie[] cookies = request.getCookies();
                    int connectathonCookiesLength = 0;
                    if (cookies != null) {
                        for (int i = 1; i < 5; i++) {
                            for (Cookie cookie : cookies) {
                                if (cookie.getName().startsWith(cookieBaseName + "_")
                                        && cookie.getName().substring(0, cookieBaseName.length() + 2)
                                        .equals(cookieName.substring(0, cookieBaseName.length() + 2))) {
                                    cookieName = cookieBaseName + "_" + (i + 1) + "_" + shortcut;
                                    break;
                                }
                            }
                        }

                        setShortcutName(null);

                        for (Cookie cookie : cookies) {
                            if (cookie.getName().startsWith(cookieBaseName + "_")) {
                                connectathonCookiesLength = connectathonCookiesLength + 1;
                            }
                        }
                        if (connectathonCookiesLength < 4) {
                            try {
                                Cookie myCookie = new Cookie(cookieName, encodedUri);
                                // two weeks
                                myCookie.setMaxAge(604800 * 2);
                                myCookie.setPath("/" + APP_BASENAME + path);
                                response.addCookie(myCookie);
                                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Preset is created !");
                            } catch (IllegalArgumentException e) {
                                LOG.error("" + e);
                                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error in cookie creation !");
                            }
                        } else {
                            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Remove a preset before save a new one !");
                        }
                    } else {
                        FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No cookie !");
                    }
                } else {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "The preset name is empty !");
                }
            } catch (UnsupportedEncodingException e1) {
                LOG.error("" + e1);
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Application preference for application url is wrong !");
        }
    }

    public void createSimpleCookieMain(String cookieName, String encodedUri, String path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createSimpleCookieMain");
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        Cookie myCookie = new Cookie(cookieName, encodedUri);
        // two weeks
        myCookie.setMaxAge(604800 * 2);
        myCookie.setPath("/" + APP_BASENAME + path);
        response.addCookie(myCookie);
    }

    public void deleteCookieToLoadMain(String cookieName, String path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteCookieToLoadMain");
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    Cookie cookieToDelete = new Cookie(cookieName, "");
                    cookieToDelete.setPath("/" + APP_BASENAME + path);
                    cookieToDelete.setMaxAge(0);
                    response.addCookie(cookieToDelete);
                }
            }
        }
    }

    public void deleteSelectedCookieMain(String presetName, String path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedCookieMain");
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().contains(presetName)) {
                    Cookie cookieToDelete = new Cookie(cookie.getName(), "");
                    cookieToDelete.setPath("/" + APP_BASENAME + path);
                    cookieToDelete.setMaxAge(0);
                    response.addCookie(cookieToDelete);
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "Cookie " + presetName + " is deleted !");
                }
            }
        }
    }

    public List<InfoCookies> getAllPresets(String cookieBaseName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllPresets");
        }
        List<InfoCookies> allPresets = new ArrayList<InfoCookies>();

        String presetName = null;
        String url = null;

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().startsWith(cookieBaseName)) {
                    presetName = cookie.getName().substring(cookieBaseName.length() + 3);
                    try {
                        url = ApplicationPreferenceManager.instance().getApplicationUrl();
                        if (url.endsWith("/")) {
                            url = url.substring(0, url.length() - 1);
                        }
                        url = url + new String(Base64.decodeBase64(cookie.getValue()), UTF_8);
                        InfoCookies ic = new InfoCookies(cookie.getName(), presetName, url);
                        ic.setEncodedCookieUri(cookie.getValue());
                        allPresets.add(ic);
                    } catch (UnsupportedEncodingException e) {
                        LOG.error("" + e);
                    }
                }
            }
        }
        return allPresets;
    }

    public String getCookieUri(String cookieBaseName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCookieUri");
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getValue().equals("None")) {
                    return "None";
                } else if (cookie.getName().contains(cookieBaseName)) {
                    try {
                        return new String(Base64.decodeBase64(cookie.getValue()), UTF_8);
                    } catch (UnsupportedEncodingException e) {
                        LOG.error("" + e);
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public boolean shortcutsIsFull(String cookieBaseName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("shortcutsIsFull");
        }
        return (getAllPresets(cookieBaseName).size() == 4);
    }

    public List<SelectItem> getListOfPresets(List<InfoCookies> infoCookiesList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfPresets");
        }
        List<InfoCookies> presetsList = infoCookiesList;
        List<SelectItem> items = new ArrayList<SelectItem>();

        items.add(new SelectItem("None", "None"));
        for (InfoCookies preset : presetsList) {
            items.add(new SelectItem(preset.getEncodedCookieUri(), preset.getCookieName()));
        }
        return items;
    }

}
