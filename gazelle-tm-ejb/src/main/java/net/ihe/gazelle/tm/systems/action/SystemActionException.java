package net.ihe.gazelle.tm.systems.action;

/**
 * Created by gthomazon on 10/07/17.
 */
public class SystemActionException extends Exception {

    private static final long serialVersionUID = 7172932247503090996L;

    public SystemActionException() {
    }

    public SystemActionException(String s) {
        super(s);
    }

    public SystemActionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SystemActionException(Throwable throwable) {
        super(throwable);
    }

    public SystemActionException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
