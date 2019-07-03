package net.ihe.gazelle.tm.tee.util;

import java.util.Collection;

/**
 * Contains utility methods for java.util.Collection instances
 *
 * @author tnabeel
 */
public class CollectionUtil {

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
