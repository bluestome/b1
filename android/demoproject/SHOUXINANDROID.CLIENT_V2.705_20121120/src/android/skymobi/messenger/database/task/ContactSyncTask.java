
package android.skymobi.messenger.database.task;

import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.utils.CommonPreferences;

/**
 * @ClassName: ContactSyncTask
 * @author Sean.Xie
 * @date 2012-7-13 下午2:15:27
 */
public class ContactSyncTask extends SyncTask {

    private static final String TAG = ContactSyncTask.class.getSimpleName();

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     */
    public ContactSyncTask(CoreService service) {
        this.service = service;
    }

    @Override
    public void run() {
        if (!isRunningContactSync && !CommonPreferences.getLogoutedStatus() && service != null) {

            if (!service.isNetConnected()) {
                SLog.d(TAG, "断网情况下不同步!!!!!");
                // 断网情况下不同步，重置同步时间，下次登录执行同步
                MainApp.i().setLastSyncTime(0);
                return;
            }

            // 开始执行联系人同步任务标示
            isRunningContactSync = true;
            service.getSyncPool().execute(new Runnable() {
                @Override
                public void run() {
                    SLog.d(TAG, "执行同步!!!!!");
                    service.getContactsModule().sync();
                    SLog.d(TAG, "执行同步结束!!!!!");
                }
            });
            // 结束执行联系人同步任务标示
            isRunningContactSync = false;
        }
    }

}
