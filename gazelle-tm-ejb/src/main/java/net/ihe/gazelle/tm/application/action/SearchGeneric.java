package net.ihe.gazelle.tm.application.action;

import net.ihe.gazelle.common.LinkDataProvider;
import net.ihe.gazelle.common.LinkDataProviderService;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.filter.entities.SearchType;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("searchGeneric")
@Scope(ScopeType.PAGE)
public class SearchGeneric implements Serializable {

    private static final long serialVersionUID = 607808178295918225L;

    private static final Logger LOG = LoggerFactory.getLogger(SearchGeneric.class);
    public static Pattern patternId = Pattern.compile(getRegexpId());
    private static int MAX_RESULT_PAGE = 20;
    private List<SearchResult> values;
    private String criteriaValue;
    private String criteriaValue2 = null;
    private SearchType criteriaType;
    private Map<SearchType, Integer> countTypes = new HashMap<SearchType, Integer>();

    public static String getRegexpId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getRegexpId");
        }
        StringBuffer res = new StringBuffer();
        res.append("(");
        SearchType[] values = SearchType.values();
        boolean first = true;
        for (SearchType searchType : values) {
            if (!first) {
                res.append("|");
            }
            res.append(searchType.getShortId());
            first = false;
        }
        res.append("):[0-9]*");
        return res.toString();
    }

    private static String getSuggestedId(String inputString) {
        Matcher matcher = patternId.matcher(inputString);
        if (matcher.find()) {
            return inputString.substring(matcher.start(), matcher.end());
        }
        return null;
    }

    @SuppressWarnings("all")
    public List<SearchResult> search(String query, SearchType type, EntityManager entityManager, int maxCount,
                                     boolean addItemIfOverflow) {
        List<SearchResult> results = new ArrayList();
        if (StringUtils.trimToNull(query) != null) {

            // we retrieve queries containing with ti:14474 or m:142, etc
            // (issued by the suggester)
            String suggestedId = getSuggestedId(query);
            if (suggestedId != null) {
                int indexOfDouble = suggestedId.indexOf(":");
                SearchType providedType = null;
                String shortId = suggestedId.substring(0, indexOfDouble);
                SearchType[] values = SearchType.values();
                for (SearchType searchType : values) {
                    if (searchType.getShortId().equals(shortId)) {
                        providedType = searchType;
                    }
                }
                String idString = suggestedId.substring(indexOfDouble + 1, suggestedId.length());
                Integer id = null;
                try {
                    id = Integer.parseInt(idString);
                } catch (NumberFormatException e) {
                    return null;
                }
                if (id != null) {
                    HQLQueryBuilder queryBuilder = new HQLQueryBuilder(providedType.getEntityClass());
                    queryBuilder.addEq("id", id);
                    Object result = queryBuilder.getUniqueResult();
                    addResult(results, providedType, result);
                }
            }

            if (results.size() == 0) {
                if (type != null) {
                    searchForType(query, entityManager, results, type, maxCount, addItemIfOverflow);
                } else {
                    SearchType[] values = SearchType.values();
                    for (SearchType searchType : values) {
                        searchForType(query, entityManager, results, searchType, maxCount, addItemIfOverflow);
                    }
                }
            }
        }
        return results;
    }

    @SuppressWarnings("all")
    private void searchForType(String query, EntityManager entityManager, List<SearchResult> results,
                               SearchType searchType, int maxCount, boolean addItemIfOverflow) {
        List searchResults = searchType.search(query, entityManager, maxCount + 1);
        int max = Math.min(maxCount, searchResults.size());
        for (int i = 0; i < max; i++) {
            Object value = searchResults.get(i);
            addResult(results, searchType, value);
        }
        if (searchResults.size() == (maxCount + 1)) {
            if (addItemIfOverflow) {
                SearchResult searchResult = new SearchResult(searchType, null);
                searchResult.valueAsString = ResourceBundle.instance().getString("net.ihe.gazelle.tm.More") + " "
                        + searchType.getLabel();
                searchResult.query = query + " type:" + searchType.getShortId();
                results.add(searchResult);
            }
        }
        countTypes.put(searchType, searchResults.size());
    }

    public List<String> getNumberOfResultPerTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfResultPerTypes");
        }
        List<SearchType> searchTypesFixedArray = Arrays.asList(SearchType.values());
        List<SearchType> searchTypes = new ArrayList<SearchType>(searchTypesFixedArray);
        Collections.sort(searchTypes, new Comparator<SearchType>() {
            @Override
            public int compare(SearchType o1, SearchType o2) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("compare");
                }
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });

        List<String> result = new ArrayList<String>(searchTypes.size());
        for (SearchType searchType : searchTypes) {
            Integer count = countTypes.get(searchType);
            if (count != null) {
                String message = "";
                if (count > MAX_RESULT_PAGE) {
                    message = StatusMessage.getBundleMessage("gazelle.tm.search.overflow", "");
                } else if (count == 1) {
                    message = StatusMessage.getBundleMessage("gazelle.tm.search.count.unique", "");
                } else {
                    message = StatusMessage.getBundleMessage("gazelle.tm.search.count", "");
                }
                message = Interpolator.instance().interpolate(message, searchType.getLabel(), count, MAX_RESULT_PAGE);
                result.add(message);
            }
        }

        return result;
    }

    private void addResult(List<SearchResult> results, SearchType searchType, Object value) {
        results.add(new SearchResult(searchType, value));
    }

    public List<SelectItem> getCriteriaTypeList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriteriaTypeList");
        }
        List<SelectItem> criteriaList = new ArrayList<SelectItem>();
        criteriaList.add(new SelectItem("", ResourceBundle.instance().getString("gazelle.tm.search.searchCriteria")));
        SearchType[] values = SearchType.values();
        for (SearchType searchType : values) {
            criteriaList.add(new SelectItem(searchType.toString(), searchType.getLabel()));
        }
        return criteriaList;
    }

    public String getCriteriaValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriteriaValue");
        }
        return criteriaValue;
    }

    public void setCriteriaValue(String criteriaValue) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCriteriaValue");
        }
        this.criteriaValue = criteriaValue;
    }

    public String getCriteriaValue2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriteriaValue2");
        }
        return criteriaValue2;
    }

    public void setCriteriaValue2(String criteriaValue2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCriteriaValue2");
        }
        this.criteriaValue2 = criteriaValue2;
    }

    public SearchType getCriteriaType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriteriaType");
        }
        return criteriaType;
    }

    public void setCriteriaType(SearchType criteriaType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCriteriaType");
        }
        this.criteriaType = criteriaType;
        countTypes = new HashMap<SearchType, Integer>();
    }

    public void search() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("search");
        }

        Redirect redirect = Redirect.instance();
        redirect.setViewId("/search/search.seam");
        redirect.setParameter("q", criteriaValue);
        redirect.execute();
    }

    public void searchGlobal() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchGlobal");
        }
        boolean directInput = false;
        // just coming on that page
        if (criteriaValue2 == null) {
            directInput = true;
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestParameterMap();
            criteriaValue2 = params.get("q");

            if (criteriaValue2 != null) {
                SearchType[] values = SearchType.values();
                for (SearchType searchType : values) {
                    String typeMarker = searchType.getShortId() + ":";
                    if (criteriaValue2.startsWith(typeMarker)) {
                        criteriaType = searchType;
                        criteriaValue2 = criteriaValue2.replaceFirst(typeMarker, "");
                    }
                }
            }
        }
        countTypes = new HashMap<SearchType, Integer>();
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        values = search(criteriaValue2, criteriaType, entityManager, MAX_RESULT_PAGE, false);
        if (directInput && (values.size() == 1)) {
            redirectToResult(values.get(0), criteriaValue2);
        }
    }

    private void redirectToResult(SearchResult searchResult, String criteriaValue2) {
        LinkDataProvider providerForClass = LinkDataProviderService.getProviderForClass(searchResult.type
                .getEntityClass());
        String link = providerForClass.getLink(searchResult.value);
        Redirect redirect = Redirect.instance();
        redirect.clearDirty();
        redirect.setViewId("/" + link);

        redirect.setParameter("id", criteriaValue2);
        redirect.execute();
    }

    public List<SearchResult> getValues() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValues");
        }
        return values;
    }

    public void searchJSON() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchJSON");
        }
        if (!Identity.instance().isLoggedIn()) {
            return;
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse catalinaResponse = (HttpServletResponse) externalContext.getResponse();
        HttpServletRequest catalinaRequest = (HttpServletRequest) externalContext.getRequest();

        catalinaResponse.reset();
        catalinaResponse.setContentType("text/x-json");

        String query = catalinaRequest.getParameter("q");
        String json = searchJSON(query);
        ServletOutputStream out = catalinaResponse.getOutputStream();
        out.write(json.getBytes(StandardCharsets.UTF_8.name()));
        out.flush();
        out.close();
        facesContext.responseComplete();
    }

    private String searchJSON(String query) {
        List<SearchResult> results = searchSuggest(query);
        String[] values = new String[results.size()];

        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            if (result.value == null) {
                values[i] = result.query;
            } else {
                values[i] = result.valueAsString + " " + result.query;
            }
        }

        return jsonArray(false, jsonQuoted(query), jsonArray(true, values));
    }

    private List<SearchResult> searchSuggest(String query) {
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        List<SearchResult> results = new ArrayList<SearchResult>();
        if (StringUtils.trimToNull(query) != null) {
            SearchType[] values = SearchType.values();
            for (SearchType searchType : values) {
                searchForType(query, entityManager, results, searchType, 4, true);
            }
        }
        return results;
    }

    private String jsonArray(boolean quoted, String... strings) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            if (quoted) {
                result.append(jsonQuoted(strings[i]));
            } else {
                result.append(strings[i]);
            }
        }
        result.append("]");
        return result.toString();
    }

    private String jsonQuoted(String str) {
        return "\"" + StringEscapeUtils.escapeJavaScript(str) + "\"";
    }

    public List<SearchResult> autocomplete(String suggest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("autocomplete");
        }

        String query = (String) suggest;
        List<SearchResult> results = searchSuggest(query);

        return results;
    }

    public static class SearchResult implements Serializable {
        private static final long serialVersionUID = -4768129784233634846L;
        SearchType type;
        Object value;
        String valueAsString;
        String query = "";

        public SearchResult(SearchType type, Object value) {
            super();
            this.type = type;
            this.value = value;
            if (value != null) {
                updateValue();
            }
        }

        public SearchType getType() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getType");
            }
            return type;
        }

        public Object getValue() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getValue");
            }
            return value;
        }

        public String getValueAsString() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getValueAsString");
            }
            return valueAsString;
        }

        public String getQuery() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getQuery");
            }
            return query;
        }

        private void updateValue() {
            LinkDataProvider providerForClass = LinkDataProviderService.getProviderForClass(type.getEntityClass());
            Object id;
            try {
                id = PropertyUtils.getProperty(value, "id");
            } catch (Exception e) {
                id = null;
            }
            valueAsString = providerForClass.getLabel(value, true);
            if (id != null) {
                query = type.getShortId() + ":" + id.toString();
            }
        }
    }
}
