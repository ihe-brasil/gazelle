package net.ihe.gazelle.tm.application.action;

import net.ihe.gazelle.common.session.GazelleSessionListener;
import net.ihe.gazelle.common.session.GazelleSessionListener.HttpSessionUser;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

@Name("sessionView")
@Scope(ScopeType.PAGE)
public class SessionView implements Serializable {

    private static final long serialVersionUID = -3844550964664529519L;

    private static final Logger LOG = LoggerFactory.getLogger(SessionView.class);

    public SessionView() {
        super();
    }

    public List<HttpSessionUser> getHttpSessions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHttpSessions");
        }
        return new ArrayList<HttpSessionUser>(GazelleSessionListener.getSortedSessions().values());
    }

    public String interpolation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("interpolation");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        String mode = requestParameterMap.get("mode");
        if (mode.equals("memory")) {
            return "linear";
        } else {
            return "step-after";
        }
    }

    public String javascriptSeries() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("javascriptSeries");
        }
        ApplicationManagerLocal applicationManagerLocal = (ApplicationManagerLocal) Component
                .getInstance("applicationManager");
        Map<Date, Usage> usages = applicationManagerLocal.getUsages();

        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

        String mode = requestParameterMap.get("mode");

        List<Entry<Date, Usage>> entrySetUsages = new ArrayList<Map.Entry<Date, Usage>>(usages.entrySet());

        Map<Date, Long> values = new TreeMap<Date, Long>();
        for (int i = 0; i < entrySetUsages.size(); i++) {
            Date time = entrySetUsages.get(i).getKey();
            Usage value = entrySetUsages.get(i).getValue();

            long lvalue = -1;
            if (mode.equals("memory")) {
                lvalue = value.getUsedMem();
            } else if (mode.equals("sessions")) {
                lvalue = value.getSessions();
            }
            values.put(time, lvalue);
        }

        Set<Entry<Date, Long>> entrySet = values.entrySet();
        List<Entry<Date, Long>> filtered = null;

        filtered = new ArrayList<Map.Entry<Date, Long>>();

        if (mode.equals("memory")) {
            int i = 0;
            for (Entry<Date, Long> entry : entrySet) {

                boolean remove = false;

                if ((i > 1) && (i != (entrySet.size() - 1))) {

                    Entry<Date, Long> prelast = filtered.get(filtered.size() - 2);
                    Entry<Date, Long> last = filtered.get(filtered.size() - 1);

                    double dt = entry.getKey().getTime() - prelast.getKey().getTime();

                    double dvRef = last.getValue() - prelast.getValue();
                    double dtRef = last.getKey().getTime() - prelast.getKey().getTime();

                    if (dtRef > 0) {
                        double cRef = dvRef / dtRef;

                        double vTarget = prelast.getValue() + (cRef * dt);
                        Long target = entry.getValue();

                        double error = 0;
                        if (target >= 1) {
                            error = Math.abs(vTarget - target) / target;
                        } else {
                            error = Math.abs(vTarget);
                        }

                        if (error < 0.05) {
                            remove = true;
                        }
                    }

                }

                if (remove) {
                    filtered.remove(filtered.size() - 1);
                }
                filtered.add(entry);

                i++;
            }
        } else {

            int i = 0;
            for (Entry<Date, Long> entry : entrySet) {
                boolean add = true;
                if ((i > 0) && (i != (entrySet.size() - 1))) {
                    Entry<Date, Long> previous = filtered.get(filtered.size() - 1);
                    long previousValue = previous.getValue();
                    long value = entry.getValue();
                    if (previousValue == value) {
                        add = false;
                    }
                }
                if (add) {
                    filtered.add(entry);
                }
                i++;
            }
        }

        StringBuilder result = new StringBuilder("var seriesData = ");
        addInterval(result, filtered);
        result.append(";");

        return result.toString();
    }

    public void addInterval(StringBuilder result, Iterable<Entry<Date, Long>> added) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addInterval");
        }
        result.append("[");
        boolean first = true;
        for (Entry<Date, Long> entry : added) {
            if (first) {
                first = false;
            } else {
                result.append(",\r\n");
            }
            result.append("{ x: ");
            result.append(entry.getKey().getTime() / 1000);
            result.append(", y: ");
            result.append(entry.getValue());
            result.append(" }");
        }
        result.append("]");
    }

    public List<Ehcache> getCaches() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCaches");
        }
        List<Ehcache> caches = new ArrayList<Ehcache>();
        List<CacheManager> allCacheManagers = new ArrayList<CacheManager>(CacheManager.ALL_CACHE_MANAGERS);
        for (CacheManager cacheManager : allCacheManagers) {
            try {
                String[] cacheNames = cacheManager.getCacheNames();
                for (String cacheName : cacheNames) {
                    caches.add(cacheManager.getEhcache(cacheName));
                }
            } catch (Throwable e) {
                LOG.error("Failed to clear cache for " + cacheManager);
            }
        }
        return caches;
    }

}
