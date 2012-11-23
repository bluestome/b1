
package android.skymobi.messenger.database.observer;

import android.database.ContentObserver;
import android.os.Handler;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.database.task.ContactSyncTask;
import android.skymobi.messenger.database.task.SyncTask;
import android.skymobi.messenger.service.CoreService;

import java.util.LinkedList;

/**
 * @ClassName: ContactsObserver
 * @author Sean.Xie
 * @date 2012-4-11 上午10:05:29
 */
public class ContactsObserver extends ContentObserver {

    private static final long DELAYMILLIS = 20000;
    private static final String TAG = ContactsObserver.class.getSimpleName();
    private final Handler handler;
    private final LinkedList<SyncTask> tasklist = new LinkedList<SyncTask>();
    private final CoreService service;

    public static long shouxinUpdateTime = 0;

    /**
     * Description: 通讯录监控
     * 
     * @param handler
     * @param coreService
     */
    public ContactsObserver(Handler handler, CoreService coreService) {
        super(handler);
        this.handler = handler;
        this.service = coreService;
    }

    @Override
    public void onChange(boolean selfChange) {
        // SLog.d(TAG, "监控联系人数据 变化 生效 " + ContactsObserver.shouxinUpdateTime);
        long currentTime = System.currentTimeMillis();
        long time = currentTime - ContactsObserver.shouxinUpdateTime;
        if (time < DELAYMILLIS || MainApp.i().isSyncContacts()) {
            // SLog.d(TAG, "监控联系人数据 变化 生效 手信修改, 不需要同步");
            return;
        }
        ContactSyncTask task = new ContactSyncTask(service);
        SyncTask t = null;
        try {
            // 移除未启动的任务
            while ((t = tasklist.remove()) != null) {
                handler.removeCallbacks(t);
            }
        } catch (Exception e) {

        }
        tasklist.add(task);
        // 插入新任务
        handler.postDelayed(task, DELAYMILLIS);
    }
}
