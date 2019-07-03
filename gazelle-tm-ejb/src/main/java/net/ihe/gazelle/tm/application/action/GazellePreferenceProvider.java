package net.ihe.gazelle.tm.application.action;

import net.ihe.gazelle.common.action.CacheRequest;
import net.ihe.gazelle.common.action.CacheUpdater;
import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.servletfilter.IEHeaderFilter;
import net.ihe.gazelle.common.util.DateDisplayUtil;
import net.ihe.gazelle.geoip.GeoIP;
import net.ihe.gazelle.geoip.result.Location;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.preferences.PreferenceProvider;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.users.action.UserManagerExtra;
import net.ihe.gazelle.tm.users.action.UserManagerExtraLocal;
import net.ihe.gazelle.tm.users.model.UserPreferences;
import net.ihe.gazelle.users.model.User;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.web.MultipartFilter;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.TimeZone;

@MetaInfServices(PreferenceProvider.class)
public class GazellePreferenceProvider implements PreferenceProvider {

    private static final String NUMBER_OF_ITEMS_PER_PAGE = "NUMBER_OF_ITEMS_PER_PAGE";
    private static final String CACHE_REQUEST = "cacheRequest";
    private static final Logger LOG = LoggerFactory.getLogger(GazellePreferenceProvider.class);

    @Override
    public String getString(String key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getString");
        }
        if ("user_time_zone".equals(key) || "time_zone".equals(key)) {
            String userTimeZone = getUserTimeZone();
            if (userTimeZone != null) {
                return userTimeZone;
            }
        }
        return ApplicationPreferenceManager.getStringValue(key);
    }

    public String getUserTimeZone() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserTimeZone");
        }
        CacheRequest cacheRequest = (CacheRequest) Component.getInstance(CACHE_REQUEST);
        Object result = cacheRequest.getValueUpdater("user_time_zone_value", new CacheUpdater() {
            @Override
            public String getValue(String key, Object parameter) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getValue");
                }
                return guessJSTimeZoneId();
            }
        }, null);
        return (String) result;
    }

    public String guessJSTimeZoneId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("guessJSTimeZoneId");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        if ((fc != null) && (fc.getExternalContext() != null)) {
            HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
            if (request != null) {
                HttpSession session = request.getSession();
                if (session != null) {

                    Object jsTimezoneObject = session.getAttribute("jsTimezone");
                    if (jsTimezoneObject != null) {
                        return (String) jsTimezoneObject;
                    }

                    Object jsTimezoneOffsetObject = session.getAttribute("jsTimezoneOffset");
                    if (jsTimezoneOffsetObject != null) {
                        String jsTimezoneOffset = (String) jsTimezoneOffsetObject;
                        Integer timezoneOffset = Integer.valueOf(jsTimezoneOffset);
                        String result = DateDisplayUtil.guessTimeZone(-timezoneOffset);

                        HttpServletRequest httpServletRequest = IEHeaderFilter.REQUEST.get();
                        if (httpServletRequest != null) {
                            String remoteIP = httpServletRequest.getRemoteAddr();
                            result = guessWithIp(remoteIP, result);
                        }

                        if (result != null) {
                            session.setAttribute("jsTimezone", result);
                        }
                        return result;
                    }

                }
            }
        }
        return TimeZone.getDefault().getID();
    }

    private String guessWithIp(String remoteIP, String guessedTimeZoneFromJavascript) {

        Location location = GeoIP.getLocation_(remoteIP);
        if (location != null) {
            String guessedTimeZoneFromIp = location.getTimezone();
            if (guessedTimeZoneFromIp != null) {
                TimeZone timeZoneFromIp = TimeZone.getTimeZone(guessedTimeZoneFromIp);
                TimeZone timeZoneFromJavascript = TimeZone.getTimeZone(guessedTimeZoneFromJavascript);
                long date = new Date().getTime();
                if (timeZoneFromIp.getOffset(date) == timeZoneFromJavascript.getOffset(date)) {
                    return guessedTimeZoneFromIp;
                }
            }
        }
        return guessedTimeZoneFromJavascript;
    }

    @Override
    public Integer getInteger(String key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInteger");
        }
        if (NUMBER_OF_ITEMS_PER_PAGE.equals(key)) {
            CacheRequest cacheRequest = (CacheRequest) Component.getInstance(CACHE_REQUEST);
            Object result = cacheRequest.getValueUpdater(NUMBER_OF_ITEMS_PER_PAGE, new CacheUpdater() {
                @Override
                public Integer getValue(String key, Object parameter) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getValue");
                    }
                    UserManagerExtraLocal userManager = (UserManagerExtraLocal) Component.getInstance("userManagerExtra");
                    if (userManager != null) {
                        return userManager.getSelectedUserPreferences().getNumberOfResultsPerPage();
                    }
                    return UserManagerExtra.DEFAULT_NUMBER_OF_RESULTS_PER_PAGE;
                }
            }, null);
            return (Integer) result;
        }
        if ("upload_max_size".equals(key)) {
            MultipartFilter multipartFilter = (MultipartFilter) Component.getInstance(MultipartFilter.class);
            return multipartFilter.getMaxRequestSize();
        }

        String stringValue = ApplicationPreferenceManager.getStringValue(key);
        if (StringUtils.isNumeric(stringValue)) {
            return Integer.valueOf(stringValue);
        } else {
            return null;
        }
    }

    @Override
    public Boolean getBoolean(String key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBoolean");
        }
        return ApplicationPreferenceManager.getBooleanValue(key);
    }

    @Override
    public Date getDate(String key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDate");
        }
        return ApplicationPreferenceManager.getDateValue(key);
    }

    @Override
    public Object getObject(Object key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getObject");
        }
        if ("user_testing_session".equals(key)) {
            CacheRequest cacheRequest = (CacheRequest) Component.getInstance(CACHE_REQUEST);
            Object result = cacheRequest.getValueUpdater("user_testing_session", new CacheUpdater() {
                @Override
                public Object getValue(String key, Object parameter) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getValue");
                    }
                    UserManagerExtraLocal userManager = (UserManagerExtraLocal) Component.getInstance("userManagerExtra");
                    if (userManager != null) {
                        TestingSession selectedTestingSession = userManager.getSelectedTestingSession();
                        return selectedTestingSession;
                    }
                    return null;
                }
            }, null);
            return result;
        }
        return null;
    }

    @Override
    public void setObject(Object key, Object value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setObject");
        }
        // TODO Auto-generated method stub

    }

    @Override
    public void setString(String key, String value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setString");
        }
        // TODO Auto-generated method stub

    }

    @Override
    public void setInteger(String key, Integer value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInteger");
        }
        if (NUMBER_OF_ITEMS_PER_PAGE.equals(key) && (User.loggedInUser() != null)) {
            UserManagerExtraLocal userManager = (UserManagerExtraLocal) Component.getInstance("userManagerExtra");
            if (userManager != null) {
                UserPreferences userPreferences = userManager.getSelectedUserPreferences();
                userPreferences.setNumberOfResultsPerPage(value);
                EntityManagerService.provideEntityManager().merge(userPreferences);
                CacheRequest cacheRequest = (CacheRequest) Component.getInstance(CACHE_REQUEST);
                cacheRequest.removeValue(NUMBER_OF_ITEMS_PER_PAGE);
            }
        }

    }

    @Override
    public void setBoolean(String key, Boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setBoolean");
        }
        // TODO Auto-generated method stub

    }

    @Override
    public void setDate(String key, Date value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDate");
        }
        // TODO Auto-generated method stub

    }

    @Override
    public Integer getWeight() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWeight");
        }
        if (Contexts.isApplicationContextActive()) {
            return -100;
        } else {
            return 100;
        }
    }

    @Override
    public int compareTo(PreferenceProvider o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compareTo");
        }
        return getWeight().compareTo(o.getWeight());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
