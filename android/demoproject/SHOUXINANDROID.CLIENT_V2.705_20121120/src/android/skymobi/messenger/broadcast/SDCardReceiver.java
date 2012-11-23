
package android.skymobi.messenger.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.R;
import android.skymobi.messenger.utils.AndroidSysUtils;
import android.skymobi.messenger.utils.dialog.ToastTool;

public class SDCardReceiver extends BroadcastReceiver {

    static final String TAG = SDCardReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
       
        SLog.d(TAG, "BroadcastReceiver:" + intent.getAction());
        // 需要先判断，部分手机虽然拨除sdcard，但系统有自带的，还是可以正常使用功能，不提示
        if (AndroidSysUtils.isAvailableSDCard(context)) {
            SLog.w(TAG, "SDcard状态正常!");
            return;
        }
        if (Intent.ACTION_MEDIA_REMOVED.equals(intent.getAction())
                || Intent.ACTION_MEDIA_SHARED.equals(intent.getAction())
                || Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())
                || Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())) {
            ToastTool.showShort(context, R.string.no_sdcard_tip);
        }
    }

}
