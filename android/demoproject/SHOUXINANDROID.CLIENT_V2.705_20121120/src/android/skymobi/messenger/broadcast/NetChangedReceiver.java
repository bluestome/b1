
package android.skymobi.messenger.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.skymobi.app.notify.INetChangeNotify;
import android.skymobi.app.notify.NetChangeNotify;
import android.skymobi.messenger.logreport.LogreportConstants;
import android.skymobi.messenger.logreport.SkymobiclickAgent;
import android.skymobi.messenger.network.NetWorkMgr;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.util.Log;

/**
 * @ClassName: NetChangedReceiver
 * @Description: 连网状态改变
 * @author Michael.Pan
 * @date 2012-2-22 下午02:41:21
 */
public class NetChangedReceiver extends BroadcastReceiver {
    private static final String TAG = NetChangedReceiver.class.getSimpleName();
    public static final String ACTION_NET = "android.net.conn.CONNECTIVITY_CHANGE";

    INetChangeNotify netChangeNotify = null;
    
    @Override
    public void onReceive(Context context, Intent intent) {

        // 统计连网状态改变事件数量。wing.hu edit at 2012-5-3
        SkymobiclickAgent.onEvent(context, LogreportConstants.EVENT_ID_NET_CHANGE, "连网状态改变,"
                + intent.getAction());
        if (intent.getAction().equals(ACTION_NET)) {
            // 只说变更，改变提示语，网络的状态还是由net层来决定
            if (CoreService.getInstance() != null)
                CoreService.getInstance().notifyObservers(CoreServiceMSG.MSG_NET_STATUE_CHANGE,
                        null);

            Log.w(TAG, "network changed...  netChangeNotify = " + netChangeNotify);
            if (netChangeNotify == null) {
                netChangeNotify = new NetChangeNotify(NetWorkMgr.getInstance().getClient());
            }
                ConnectivityManager mgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                // 获取多网络对象
                NetworkInfo[] networks = mgr.getAllNetworkInfo();
                
                netChangeNotify.netChange(networks);
        	
        	
            
        }
    }
}
