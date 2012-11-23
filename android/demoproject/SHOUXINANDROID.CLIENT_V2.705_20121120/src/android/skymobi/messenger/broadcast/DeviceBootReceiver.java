
package android.skymobi.messenger.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;

/**
 * @ClassName: DeviceBootReceiver
 * @Description: 启动手机时同时启动手信service
 * @author Michael.Pan
 * @date 2012-2-22 下午02:39:42
 */
public class DeviceBootReceiver extends BroadcastReceiver {
    private static final String TAG = DeviceBootReceiver.class.getSimpleName();

    /*
     * comment by zzy private static final int MSG_DELAY_START_SERVICE = 0x01;
     * private static final int DELAY_MILLIS = 3000;// 3000ms private final
     * Handler mHandler = new Handler() {
     * @Override public void handleMessage(Message msg) { switch (msg.what) {
     * case MSG_DELAY_START_SERVICE: Context context = (Context) msg.obj; Intent
     * IntentService = new Intent(context, CoreService.class);
     * context.startService(IntentService); SLog.d(TAG,
     * "Start CoreService ......"); break; } } };
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SLog.d(TAG, "ACTION_BOOT_COMPLETED ");
            // 延时启动service， Fix 缺陷 #14430
            // http://redmine.sky-mobi.com/redmine/issues/14430
            /*
             * comment by zzy 因为初始化工作已经移到MainApp中进行，另外2.7开始登录过程将与IMSI无关，所以取消该措施
             * mHandler
             * .sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_START_SERVICE
             * , context), DELAY_MILLIS);
             */
            MainApp.i().startLifeService();

        }
    }
}
