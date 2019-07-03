package net.ihe.gazelle.menu;

public enum Icons {

    GAZELLE("/gazelle.png"),

    REGISTER_ACCOUNT("gzl-icon-user-plus"),

    REGISTRATION("gzl-icon-register"),

    COMPANY("gzl-icon-institution"),

    SYSTEMS("gzl-icon-system"),

    USERS("gzl-icon-users"),

    CONTACTS("gzl-icon-user"),

    PARTICIPANTS("gzl-icon-user"),

    FINANCIAL("gzl-icon-usd"),

    FINANCIAL_SUMMARY("gzl-icon-usd"),

    INTRO("gzl-icon-calendar-o"),

    SEARCH("gzl-icon-search"),

    STATISTICS("gzl-icon-pie-chart"),

    EDITOR("gzl-icon-pencil-square-o"),

    TF_OVERVIEW("gzl-icon-street-view"),

    DOCUMENT("gzl-icon-book"),

    CONFIGURATIONS("gzl-icon-configure"),

    CAT("gzl-icon-gears"),

    CAT_PRE("gzl-icon-configure"),

    CAT_RUNNING("gzl-icon-play-black"),

    CAT_RESULTS("gzl-icon-check-square-o"),

    TD("gzl-icon-tests"),

    CALENDAR("gzl-icon-calendar"),

    NETWORK_SERVER("gzl-icon-server"),

    CERTIFICATES("gzl-icon-key"),

    MONITOR("gzl-icon-eye"),

    SEARCH_MONITOR("gzl-icon-search"),

    SAMPLE("gzl-icon-sample"),

    PATIENTS("gzl-icon-users"),

    ADMINISTRATION("gzl-icon-configure"),

    CRAWLER("gzl-icon-tasks"),

    CHECK("gzl-icon-check-square-o"),

    USER_PREFERENCES("gzl-icon-configure"),;

    private String icon;

    Icons(String icon) {
        this.icon = icon;
    }

    String getIcon() {
        return icon;
    }

}
