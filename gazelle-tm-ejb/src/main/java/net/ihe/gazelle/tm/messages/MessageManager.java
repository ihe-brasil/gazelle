package net.ihe.gazelle.tm.messages;

import net.ihe.gazelle.common.action.DateDisplay;
import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.util.GazelleCookie;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.messaging.MessagePropertyChanged;
import net.ihe.gazelle.messaging.MessagingProvider;
import net.ihe.gazelle.services.GenericServiceLoader;
import net.ihe.gazelle.tm.messages.model.Message;
import net.ihe.gazelle.tm.messages.model.MessageParameter;
import net.ihe.gazelle.tm.messages.model.MessageQuery;
import net.ihe.gazelle.users.model.User;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Base64;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Name("messageManager")
@Scope(ScopeType.PAGE)
@MetaInfServices(MessagingProvider.class)
public class MessageManager
        implements Serializable, MessagingProvider, QueryModifier<Message> {

    private static final String USERS_USERNAME = "users.username";

    private static final String DATE = "date";

    private static final String DOUBLE_QUOTE = "\", ";
    private static final long serialVersionUID = -495519156451804999L;
    private static final String LAST_READ_MESSAGE = "LAST_READ_MESSAGE";
    private static final String LAST_NOTIFICATION = "LAST_NOTIFICATION";
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    // Ten years
    private static final int MAX_AGE = 60 * 60 * 24 * 365 * 10;
    private static final Logger LOG = LoggerFactory.getLogger(MessageManager.class);
    private FilterDataModel<Message> messageDataModel = null;
    private Message selectedMessage = null;
    private String recipients = "";
    private String simpleMessage = "";
    private String link = "";
    private UserBean userBean;
    private Date lastReadDate;

    public MessageManager() {
        super();
    }

    public static <T> void addMessage(MessageSource<T> messageSource, T sourceObject, String... messageParameters) {
        try {
            if (ApplicationPreferenceManager.getBooleanValue("use_messages")) {
                EntityManager em = EntityManagerService.provideEntityManager();
                Message oMessage = new Message();
                oMessage.setDate(new Date());
                oMessage.setLink(messageSource.getLink(sourceObject, messageParameters));
                oMessage.setType(messageSource.getType(sourceObject, messageParameters));
                oMessage.setImage(messageSource.getImage(sourceObject, messageParameters));

                User loggedInUser = User.loggedInUser();
                if (loggedInUser == null) {
                    String remoteLogin = User.remoteLogin.get();
                    loggedInUser = User.FindUserWithUsername(remoteLogin);
                }

                if (loggedInUser != null) {
                    oMessage.setAuthor(loggedInUser.getUsername());
                } else {
                    oMessage.setAuthor("?");
                }

                List<MessageParameter> oMessageParameters = new ArrayList<MessageParameter>();
                for (String messageParameter : messageParameters) {
                    MessageParameter oMessageParameter = new MessageParameter();
                    oMessageParameter.setMessageParameter(messageParameter);
                    oMessageParameters.add(oMessageParameter);
                }
                oMessage.setMessageParameters(oMessageParameters);
                List<User> users = messageSource.getUsers(loggedInUser, sourceObject, messageParameters);
                oMessage.setUsers(users);

                em.persist(oMessage);
                em.flush();
            }
        } catch (Throwable e) {
            LOG.error(e.getMessage());
        }
    }

    public static String newMessagesNotificationsBase64() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String newMessagesNotificationsBase64");
        }
        return Base64.encodeBytes(newMessagesNotifications().getBytes(UTF_8), Base64.DONT_BREAK_LINES);
    }

    private static String newMessagesNotifications() {
        StringBuilder sb = new StringBuilder("([");

        Date lastReadDate = getLastReadDate(LAST_NOTIFICATION);

        HQLQueryBuilder<Message> queryBuilder = new HQLQueryBuilder<Message>(Message.class);
        User loggedInUser = User.loggedInUser();
        if (loggedInUser != null) {
            queryBuilder.addEq(USERS_USERNAME, loggedInUser.getUsername());
        } else {
            queryBuilder.addEq(USERS_USERNAME, "------------------");
        }
        queryBuilder.addOrder(DATE, true);

        queryBuilder.addRestriction(HQLRestrictions.ge(DATE, lastReadDate));

        List<Message> allItems = queryBuilder.getList();
        boolean first = true;
        for (Message message : allItems) {
            String title =
                    getFormattedType(message) + " (" + DateDisplay.instance().displayDateTime(message.getDate())
                            + ")";
            String text = getFormattedMessage(message);
            String link = message.getFormattedLink();
            String image = message.getFormattedImage();

            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append("[");

            sb.append("\"");
            sb.append(StringEscapeUtils.escapeJavaScript(title));
            sb.append(DOUBLE_QUOTE);
            sb.append("\"");
            sb.append(StringEscapeUtils.escapeJavaScript(text));
            sb.append(DOUBLE_QUOTE);
            sb.append("\"");
            sb.append(StringEscapeUtils.escapeJavaScript(link));
            sb.append(DOUBLE_QUOTE);
            sb.append("\"");
            sb.append(StringEscapeUtils.escapeJavaScript(message.getId().toString()));
            sb.append(DOUBLE_QUOTE);
            sb.append("\"");
            sb.append(StringEscapeUtils.escapeJavaScript(image));
            sb.append("\"");

            sb.append("]");
        }
        setLastReadDate(new Date(), LAST_NOTIFICATION);

        sb.append("])");
        return sb.toString();
    }

    private static Date getLastReadDate(String cookieName) {
        Date result = null;

        String value = GazelleCookie.getCookie(cookieName);
        if (value != null && !"".equals(value)) {
            Long ms = Long.parseLong(value);
            result = new Date(ms);
        }

        if (result == null) {
            result = new Date();
            setLastReadDate(result, cookieName);
        }
        return result;
    }

    private static void setLastReadDate(Date date, String cookieName) {
        String ms = Long.toString(date.getTime());
        GazelleCookie.setCookie(cookieName, ms);
    }

    public static int getUnreadCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("int getUnreadCount");
        }
        Date lastReadDate = getLastReadDate(LAST_READ_MESSAGE);

        HQLQueryBuilder<Message> queryBuilder = new HQLQueryBuilder<Message>(Message.class);
        User loggedInUser = User.loggedInUser();
        if (loggedInUser != null) {
            queryBuilder.addEq(USERS_USERNAME, loggedInUser.getUsername());
        }
        queryBuilder.addRestriction(HQLRestrictions.ge(DATE, lastReadDate));

        return queryBuilder.getCount();
    }

    public static String getFormattedType(Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getFormattedType");
        }
        return getFormatted(message, message.getType());
    }

    public static String getFormattedMessage(Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getFormattedMessage");
        }
        return getFormatted(message, message.getType() + ".message");
    }

    private static String getFormatted(Message message, String key) {
        String translated = StatusMessage.getBundleMessage(key, key);
        if ((message.getMessageParameters() != null) && (message.getMessageParameters().size() > 0)) {
            for (int i = 0; i < message.getMessageParameters().size(); i++) {
                MessageParameter messageParameter = message.getMessageParameters().get(i);
                String stringMessageParameter = messageParameter.getMessageParameter();
                stringMessageParameter = StatusMessage.getBundleMessage(stringMessageParameter, stringMessageParameter);
                translated = StringUtils.replace(translated, "{" + i + "}", stringMessageParameter);
            }
        }
        return translated;
    }

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public String getRecipients() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRecipients");
        }
        return recipients;
    }

    public void setRecipients(String recipients) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRecipients");
        }
        this.recipients = recipients;
    }

    public UserBean getUserBean() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserBean");
        }
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setUserBean");
        }
        this.userBean = userBean;
    }

    public String getSimpleMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSimpleMessage");
        }
        return simpleMessage;
    }

    public void setSimpleMessage(String simpleMessage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSimpleMessage");
        }
        this.simpleMessage = simpleMessage;
    }

    public String getLink() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLink");
        }
        return link;
    }

    public void setLink(String link) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLink");
        }
        this.link = link;
    }

    public void sendSimpleMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendSimpleMessage");
        }
        User loggedInUser = User.loggedInUser();
        String realRecipients = recipients.replaceAll(", ", " ");
        realRecipients = realRecipients.replaceAll(",", " ");
        if (StringUtils.trimToNull(link) != null) {
            addMessage(SimpleMessageSource.INSTANCE, loggedInUser, realRecipients, simpleMessage, link);
        } else {
            addMessage(SimpleMessageSource.INSTANCE, loggedInUser, realRecipients, simpleMessage);
        }
        FacesMessages.instance().add(Severity.INFO, "Message \"" + simpleMessage + "\" sent to " + recipients);
    }

    public List<UserBean> userAutocomplete(Object suggest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("userAutocomplete");
        }
        String search = (String) suggest;

        HQLQueryBuilder<User> builder = new HQLQueryBuilder<User>(User.class);

        builder.addRestriction(HQLRestrictions.or(HQLRestrictions
                        .or(HQLRestrictions.like("username", search), HQLRestrictions.like("firstname", search)),
                HQLRestrictions.like("lastname", search)));

        List<User> result = builder.getList();

        List<UserBean> beans = new ArrayList<UserBean>(result.size());
        for (User user : result) {
            beans.add(new UserBean(user));
        }

        return beans;
    }

    public void retrieveMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveMessage");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        String id = fc.getExternalContext().getRequestParameterMap().get("id");
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        selectedMessage = entityManager.find(Message.class, Integer.parseInt(id));
    }

    public FilterDataModel<Message> getMessages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessages");
        }
        return messageDataModel;
    }

    public List<Message> getAllMessages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllMessages");
        }
        List<Message> result = null;

        User loggedInUser = User.loggedInUser();
        if (loggedInUser != null) {
            MessageQuery q = new MessageQuery();
            q.users().username().eq(loggedInUser.getUsername());
            q.setMaxResults(100);
            q.setFirstResult(0);
            q.date().order(false);
            result = q.getListDistinctOrdered();
        } else {
            result = new ArrayList<Message>();
            Message message = new Message();
            message.setDate(new Date());
            message.setId(0);
            message.setLink(ApplicationPreferenceManager.instance().getApplicationUrl() + "users/loginCAS/login.seam");
            message.setMessageParameters(new ArrayList<MessageParameter>());
            message.setType("");
            result.add(message);
        }

        return result;
    }

    public void resetUnreadCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetUnreadCount");
        }
        lastReadDate = getLastReadDate(LAST_READ_MESSAGE);

        MessageQuery query = new MessageQuery();
        HQLCriterionsForFilter<Message> hqlCriterions = query.getHQLCriterionsForFilter();
        hqlCriterions.addQueryModifier(this);
        messageDataModel = new FilterDataModel<Message>(new Filter<Message>(hqlCriterions)) {
            @Override
            protected Object getId(Message t) {
                return t.getId();
            }
        };

        if (messageDataModel.getRowCount() == 0) {
            lastReadDate = null;
        }

        setLastReadDate(new Date(), LAST_READ_MESSAGE);
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<Message> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        User loggedInUser = User.loggedInUser();
        if (loggedInUser != null) {
            queryBuilder.addEq(USERS_USERNAME, loggedInUser.getUsername());
        } else {
            queryBuilder.addEq(USERS_USERNAME, "----------");
        }
        if (lastReadDate != null) {
            queryBuilder.addRestriction(HQLRestrictions.ge(DATE, lastReadDate));
        }
    }

    public Message getSelectedMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedMessage");
        }
        return selectedMessage;
    }

    @Override
    public void receiveMessage(Object message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("receiveMessage");
        }
        if (message instanceof MessagePropertyChanged) {
            List<MessageSourceFromPropertyChange> providers = GenericServiceLoader
                    .getServices(MessageSourceFromPropertyChange.class);
            MessagePropertyChanged messagePropertyChanged = (MessagePropertyChanged) message;
            for (MessageSourceFromPropertyChange messageSource : providers) {
                if (messageSource.getPropertyObjectClass()
                        .isAssignableFrom(messagePropertyChanged.getObject().getClass())) {
                    if (messageSource.getPropertyName().equals(messagePropertyChanged.getPropertyName())) {
                        messageSource.receivePropertyMessage(messagePropertyChanged);
                    }
                }
            }
        }
    }

    public class UserBean {

        private String username;
        private String firstname;
        private String lastname;

        public UserBean() {
            super();
        }

        public UserBean(User user) {
            this.username = user.getUsername();
            this.firstname = user.getFirstname();
            this.lastname = user.getLastname();
        }

        public String getUsername() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getUsername");
            }
            return username;
        }

        public void setUsername(String username) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setUsername");
            }
            this.username = username;
        }

        public String getFirstname() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getFirstname");
            }
            return firstname;
        }

        public void setFirstname(String firstname) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setFirstname");
            }
            this.firstname = firstname;
        }

        public String getLastname() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getLastname");
            }
            return lastname;
        }

        public void setLastname(String lastname) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("setLastname");
            }
            this.lastname = lastname;
        }

        @Override
        public int hashCode() {
            if (LOG.isDebugEnabled()) {
                LOG.debug("hashCode");
            }
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((firstname == null) ? 0 : firstname.hashCode());
            result = (prime * result) + ((lastname == null) ? 0 : lastname.hashCode());
            result = (prime * result) + ((username == null) ? 0 : username.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("equals");
            }
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            UserBean other = (UserBean) obj;
            if (firstname == null) {
                if (other.firstname != null) {
                    return false;
                }
            } else if (!firstname.equals(other.firstname)) {
                return false;
            }
            if (lastname == null) {
                if (other.lastname != null) {
                    return false;
                }
            } else if (!lastname.equals(other.lastname)) {
                return false;
            }
            if (username == null) {
                if (other.username != null) {
                    return false;
                }
            } else if (!username.equals(other.username)) {
                return false;
            }
            return true;
        }

    }

}
