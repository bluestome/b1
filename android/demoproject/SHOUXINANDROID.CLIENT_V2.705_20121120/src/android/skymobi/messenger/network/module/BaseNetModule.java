
package android.skymobi.messenger.network.module;

import android.skymobi.app.net.event.ISXListener;

/**
 * @ClassName: BaseNetModule
 * @author Sean.Xie
 * @date 2012-3-2 上午11:18:22
 */
public class BaseNetModule {
	
	protected static final String TAG = "NetModule";

    public static final int NET_SUCCESS = 1;
    public static final int NET_ERR = 2;
    public static final int NET_FAILED = 3;

    protected ISXListener netClient = null;

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param netClient
     */
    public BaseNetModule(ISXListener netClient) {
        this.netClient = netClient;
    }

}
