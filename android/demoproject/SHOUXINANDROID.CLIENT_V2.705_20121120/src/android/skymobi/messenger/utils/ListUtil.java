
package android.skymobi.messenger.utils;

import java.util.Comparator;
import java.util.List;

/**
 * @ClassName: ListUtil
 * @author Sean.Xie
 * @date 2012-3-9 下午5:26:01
 */
public class ListUtil {

    public static <T> boolean contains(List<T> list, T obj, Comparator<T> comparator) {
        for (T t : list) {
            int result = comparator.compare(t, obj);
            if (result == 0)
                return true;
        }
        return false;
    }

    public static <T> int getIndex(List<T> list, T obj, Comparator<T> comparator) {
        for (int i = 0; i < list.size(); i++) {
            int result = comparator.compare(list.get(i), obj);
            if (result == 0)
                return i;
        }
        return -1;
    }

    public static <T> T getObject(List<T> list, T obj, Comparator<T> comparator) {
        T t = null;
        if (list == null) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            t = list.get(i);
            int result = comparator.compare(t, obj);
            if (result == 0) {
                return t;
            } else {
                t = null;
            }
        }
        return t;
    }

    public static <T> void removeObject(List<T> list, T obj, Comparator<T> comparator) {
        for (int i = 0; i < list.size(); i++) {
            int result = comparator.compare(list.get(i), obj);
            if (result == 0)
                list.remove(i);
        }
    }
}
