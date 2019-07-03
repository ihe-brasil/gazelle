package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.filter.util.MapNotifierListener;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tf.model.constraints.*;
import net.ihe.gazelle.tm.gazelletest.action.AIPOSelector;
import net.ihe.gazelle.tm.gazelletest.test.AssertionsManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.richfaces.event.DropEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Name("rulesManager")
@Scope(ScopeType.PAGE)
public class RulesManager implements Serializable, QueryModifier<AipoRule>, MapNotifierListener {

    private static final long serialVersionUID = 8568043008516245639L;
    private static final Logger LOG = LoggerFactory.getLogger(RulesManager.class);
    private FilterDataModel<AipoRule> filterDataModel;

    private Filter<AipoRule> filter;

    private Filter<ActorIntegrationProfileOption> filterEditAIPOs;

    private FilterDataModel<ActorIntegrationProfileOption> editAIPOs;

    private AipoRule itemToDelete;

    private AipoRule editedItem;

    private List<AipoCriterion> board;

    private AipoSingle popupAipoSingle;

    private List<AipoRule> aipoR;

    public List<AipoRule> getAipoR() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoR");
        }
        getRules();
        return aipoR;
    }

    public void setAipoR(List<AipoRule> aipoR) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAipoR");
        }
        this.aipoR = aipoR;
    }

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

        String paramId = requestParameterMap.get("id");
        if ((paramId == null) || "null".equals(paramId)) {
            this.editedItem = new AipoRule();
        } else {
            this.editedItem = EntityManagerService.provideEntityManager().find(AipoRule.class,
                    Integer.parseInt(paramId));
        }
        this.board = new ArrayList<AipoCriterion>();

        AIPOSelector aipoSelector = (AIPOSelector) Component.getInstance("aipoSelector");

        aipoSelector.getFilter().getFilterValues().addListener(this);
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public AipoSingle getPopupAipoSingle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPopupAipoSingle");
        }
        return popupAipoSingle;
    }

    public void setPopupAipoSingle(AipoSingle aipoSingle) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPopupAipoSingle");
        }
        this.popupAipoSingle = aipoSingle;
        filterEditAIPOs = null;
        editAIPOs = null;
    }

    public Filter<ActorIntegrationProfileOption> getFilterEditAIPOs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterEditAIPOs");
        }
        if (filterEditAIPOs == null) {
            ActorIntegrationProfileOptionQuery aipoQuery = new ActorIntegrationProfileOptionQuery();
            HQLCriterionsForFilter<ActorIntegrationProfileOption> hqlCriterionsForFilter = aipoQuery
                    .getHQLCriterionsForFilter();

            String actorNotNull = null;
            if (popupAipoSingle != null) {
                actorNotNull = StringUtils.trimToNull(popupAipoSingle.getActor());
            }
            if (actorNotNull != null) {
                hqlCriterionsForFilter.addPath("actor", aipoQuery.actorIntegrationProfile().actor().keyword(),
                        actorNotNull);
            } else {
                hqlCriterionsForFilter.addPath("actor", aipoQuery.actorIntegrationProfile().actor().keyword());
            }

            String ipNotNull = null;
            if (popupAipoSingle != null) {
                ipNotNull = StringUtils.trimToNull(popupAipoSingle.getIntegrationProfile());
            }
            if (ipNotNull != null) {
                hqlCriterionsForFilter.addPath("ip",
                        aipoQuery.actorIntegrationProfile().integrationProfile().keyword(), ipNotNull);
            } else {
                hqlCriterionsForFilter
                        .addPath("ip", aipoQuery.actorIntegrationProfile().integrationProfile().keyword());
            }

            String optionNotNull = null;
            if (popupAipoSingle != null) {
                optionNotNull = StringUtils.trimToNull(popupAipoSingle.getOption());
            }
            if (optionNotNull != null) {
                hqlCriterionsForFilter.addPath("option", aipoQuery.integrationProfileOption().keyword(), optionNotNull);
            } else {
                hqlCriterionsForFilter.addPath("option", aipoQuery.integrationProfileOption().keyword());
            }

            filterEditAIPOs = new Filter<ActorIntegrationProfileOption>(hqlCriterionsForFilter);
        }

        return filterEditAIPOs;
    }

    public FilterDataModel<ActorIntegrationProfileOption> getEditAIPOs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditAIPOs");
        }
        if (editAIPOs == null) {
            editAIPOs = new FilterDataModel<ActorIntegrationProfileOption>(getFilterEditAIPOs()) {
                @Override
                protected Object getId(ActorIntegrationProfileOption t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return editAIPOs;
    }

    public void addNew() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNew");
        }
        edit(new AipoRule());
    }

    public void view(AipoRule aipoRule) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("view");
        }
        this.editedItem = aipoRule;
    }

    public void edit(AipoRule aipoRule) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("edit");
        }
        redirect(aipoRule, "/tf/rules/edit.xhtml");
    }

    private void redirect(AipoRule aipoRule, String viewId) {
        Redirect redirect = new Redirect();
        redirect.setViewId(viewId);
        if (aipoRule.getId() != null) {
            redirect.setParameter("id", aipoRule.getId());
        } else {
            redirect.setParameter("id", "null");
        }
        redirect.execute();
    }

    public void preDelete(AipoRule aipoRule) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("preDelete");
        }
        itemToDelete = aipoRule;
    }

    public void delete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("delete");
        }
        if (itemToDelete != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            itemToDelete = entityManager.find(AipoRule.class, itemToDelete.getId());
            entityManager.remove(itemToDelete);
            entityManager.flush();
            getFilter().modified();
        }
    }

    public FilterDataModel<AipoRule> getRules() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRules");
        }
        if (filterDataModel == null) {
            filterDataModel = new FilterDataModel<AipoRule>(getFilter()) {
                @Override
                protected Object getId(AipoRule t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        aipoR = (List<AipoRule>) filterDataModel.getAllItems(fc);
        Collections.sort(aipoR);
        return filterDataModel;
    }

    public Filter<AipoRule> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            AipoRuleQuery aipoRuleQuery = new AipoRuleQuery();
            HQLCriterionsForFilter<AipoRule> hqlCriterionsForFilter = aipoRuleQuery.getHQLCriterionsForFilter();
            hqlCriterionsForFilter.addQueryModifier(this);
            filter = new Filter<AipoRule>(hqlCriterionsForFilter);
        }
        return filter;
    }

    public AipoRule getEditedItem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditedItem");
        }
        return editedItem;
    }

    public List<AipoCriterion> getBoard() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBoard");
        }
        return board;
    }

    public void removeCriterion(int criterionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeCriterion");
        }
        AipoCriterion element = null;
        for (AipoCriterion boardElement : board) {
            if (boardElement.getFakeId() == criterionId) {
                element = boardElement;
            }
        }
        if (element == null) {
            element = getCriterionWithFakeid(criterionId, true);
            if (element != null) {
                board.add(element);
            }
        } else {
            board.remove(element);
        }
    }

    public void onDropItem(DropEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("onDropItem");
        }
        String dropValue = e.getDropValue().toString();

        String dragValueIdString = e.getDragValue().toString();
        int dragValueId = Integer.parseInt(dragValueIdString);

        // prevent drop on itself in a child
        if (StringUtils.isNumeric(dropValue)) {
            AipoCriterion dragValue = getCriterionWithFakeid(dragValueId, false);
            AipoCriterion targetList = getCriterionWithFakeid(Integer.parseInt(dropValue), false);

            AipoCriterion isAChild = getCriterionWithFakeidRecursive(targetList.getFakeId(), dragValue, false);
            if (isAChild != null) {
                return;
            }
        }

        AipoCriterion dragValue = getCriterionWithFakeid(dragValueId, true);

        if ("cause".equals(dropValue)) {
            if (editedItem.getCause() != null) {
                board.add(editedItem.getCause());
            }
            editedItem.setCause(dragValue);
        } else if ("consequence".equals(dropValue)) {
            if (editedItem.getConsequence() != null) {
                board.add(editedItem.getConsequence());
            }
            editedItem.setConsequence(dragValue);
        } else if ("board".equals(dropValue)) {
            board.add(dragValue);
        } else {
            AipoCriterion targetList = getCriterionWithFakeid(Integer.parseInt(dropValue), false);

            AipoCriterion isAChild = getCriterionWithFakeidRecursive(targetList.getFakeId(), dragValue, false);
            if (isAChild == null) {
                if (targetList instanceof AipoList) {
                    ((AipoList) targetList).getAipoCriterions().add(dragValue);
                }
            }
        }
    }

    private AipoCriterion getCriterionWithFakeid(int fakeId, boolean remove) {
        AipoCriterion result = null;
        if ((editedItem.getCause() != null) && (editedItem.getCause().getFakeId() == fakeId)) {
            result = editedItem.getCause();
            if (remove) {
                editedItem.setCause(null);
            }
            return result;
        }
        result = getCriterionWithFakeidRecursive(fakeId, editedItem.getCause(), remove);
        if (result != null) {
            return result;
        }

        if ((editedItem.getConsequence() != null) && (editedItem.getConsequence().getFakeId() == fakeId)) {
            result = editedItem.getConsequence();
            if (remove) {
                editedItem.setConsequence(null);
            }
            return result;
        }
        result = getCriterionWithFakeidRecursive(fakeId, editedItem.getConsequence(), remove);
        if (result != null) {
            return result;
        }

        result = getCriterionWithFakeidFromList(fakeId, board, remove);
        if (result != null) {
            return result;
        }

        return result;
    }

    private AipoCriterion getCriterionWithFakeidRecursive(int fakeId, AipoCriterion criterion, boolean remove) {
        if (criterion != null) {
            if (criterion instanceof AipoList) {
                return getCriterionWithFakeidFromList(fakeId, ((AipoList) criterion).getAipoCriterions(), remove);
            }
        }
        return null;
    }

    private AipoCriterion getCriterionWithFakeidFromList(int fakeId, List<AipoCriterion> aipoCriterions, boolean remove) {
        AipoCriterion result = null;
        boolean toRemove = false;
        for (AipoCriterion aipoCriterion : aipoCriterions) {
            if (result == null) {
                if (aipoCriterion.getFakeId() == fakeId) {
                    result = aipoCriterion;
                    if (remove) {
                        toRemove = true;
                    }
                } else {
                    result = getCriterionWithFakeidRecursive(fakeId, aipoCriterion, remove);
                }
            }
        }
        if (toRemove) {
            aipoCriterions.remove(result);
        }
        return result;
    }

    public boolean isList(AipoCriterion aipoCriterion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isList");
        }
        return aipoCriterion instanceof AipoList;
    }

    public void addBoardAipoRule() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addBoardAipoRule");
        }
        board.add(new AipoSingle());
    }

    public void addBoardAipoList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addBoardAipoList");
        }
        board.add(new AipoList());
    }

    public void removeCriterionFromList(AipoList list, AipoCriterion criterion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeCriterionFromList");
        }
        list.getAipoCriterions().remove(criterion);
    }

    public void removeCriterionFromCause() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeCriterionFromCause");
        }
        board.add(editedItem.getCause());
        editedItem.setCause(null);
    }

    public void removeCriterionFromConsequence() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeCriterionFromConsequence");
        }
        board.add(editedItem.getConsequence());
        editedItem.setConsequence(null);
    }

    public void removeCriterionFromBoard(AipoCriterion criterion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeCriterionFromBoard");
        }
        board.remove(criterion);
    }

    public void updateAIPOSingle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateAIPOSingle");
        }
        String actor = (String) filterEditAIPOs.getFilterValues().get("actor");
        String ip = (String) filterEditAIPOs.getFilterValues().get("ip");
        String option = (String) filterEditAIPOs.getFilterValues().get("option");
        popupAipoSingle.setActor(Actor.findActorWithKeyword(actor));
        popupAipoSingle.setIntegrationProfile(IntegrationProfile.findIntegrationProfileWithKeyword(ip));
        popupAipoSingle.setOption(IntegrationProfileOption.findIntegrationProfileOptionWithKeyword(option));
    }

    public void saveRule() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveRule");
        }
        if ((editedItem.getCause() != null) && (editedItem.getConsequence() != null)) {
            editedItem.setCause(editedItem.getCause().simplify());
            editedItem.setConsequence(editedItem.getConsequence().simplify());
            if (editedItem.getId() == null) {
                EntityManagerService.provideEntityManager().persist(editedItem);
            } else {
                EntityManagerService.provideEntityManager().merge(editedItem);
            }
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Rule " + showAsText(editedItem) + " saved");
        }
    }

    public String showAsText(AipoRule rule) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showAsText");
        }
        if (rule == null) {
            return "";
        } else {
            String res = rule.toString();
            res = StringEscapeUtils.escapeHtml(res);
            res = res.replaceAll(" ", "&nbsp;");
            res = res.replaceAll("\n", "<br />");
            return res;
        }
    }

    public String index() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("index");
        }
        return "/tf/rules/index.seam";
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<AipoRule> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        AIPOSelector aipoSelector = (AIPOSelector) Component.getInstance("aipoSelector");
        Actor actor = (Actor) aipoSelector.getFilter().getFilterValues().get("actor");
        IntegrationProfile ip = (IntegrationProfile) aipoSelector.getFilter().getFilterValues()
                .get("integrationProfile");
        IntegrationProfileOption option = (IntegrationProfileOption) aipoSelector.getFilter().getFilterValues()
                .get("integrationProfileOption");

        AipoRuleQuery aipoRuleQuery = new AipoRuleQuery(queryBuilder);

        if (actor != null) {
            aipoRuleQuery.aipoRules().actor().eq(actor);
        }

        if (ip != null) {
            aipoRuleQuery.aipoRules().integrationProfile().eq(ip);
        }

        if (option != null) {
            aipoRuleQuery.aipoRules().option().eq(option);
        }
    }

    @Override
    public void modified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modified");
        }
        getRules().getFilter().modified();
    }

    public int getNbAssertionsFor(int ruleId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNbAssertionsFor");
        }
        AssertionsManager client = new AssertionsManager();

        return client.getRuleAssertions(ruleId).size();
    }
}
