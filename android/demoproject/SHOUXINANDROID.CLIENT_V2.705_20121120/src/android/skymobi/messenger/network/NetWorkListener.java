
package android.skymobi.messenger.network;

/**
 * @ClassName: NetWorkListener
 * @Description: NetWork Listener
 * @author Michael.Pan
 * @date 2012-2-7 上午09:32:02
 */
public interface NetWorkListener {
    public final static int ON_LINE = 2000;
    public final static int OFF_LINE = 2001;
    public final static int RE_ON_LINE = 2002;

    /**
     * @param what
     * @param wparam
     * @param lparam
     * @param obj
     */
    public void onNotify(int what, Object obj);
}
