
package android.skymobi.messenger.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 移动网络状态变更接收器
 * 
 * @ClassName: MobileStateChangeReciever
 * @Description: 用户将系统中移动网络状态广播拦截，然后使用自己的广播类接收系统广播，并发送自定义的广播。
 * @author Bluestome.Zhang
 * @date 2012-3-20 上午11:38:24
 */
public class MobileStateChangeReciever extends BroadcastReceiver {

    private ConnectivityManager connManager = null;
    public final static String NEW_SHOUXI_NETWORK_CHANGE = "android.net.conn.NETWORKCHANGE";

    private void init(Context context) {
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        init(context);
        NetworkInfo network = connManager.getActiveNetworkInfo();
        Intent networkChange = new Intent(NEW_SHOUXI_NETWORK_CHANGE);
        if (null != network && network.isAvailable() && network.isConnected()) {
            int type = network.getType();
            String typeName = network.getTypeName();
            networkChange.putExtra("network_type", type);
            networkChange.putExtra("network_type_name", typeName);
        } else {
            networkChange.putExtra("network_disconnect", new Integer(1));
        }
        // 再次发送已约束的广播
        context.sendBroadcast(networkChange);
    }

}
