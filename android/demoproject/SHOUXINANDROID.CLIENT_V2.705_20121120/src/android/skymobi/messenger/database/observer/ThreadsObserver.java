
package android.skymobi.messenger.database.observer;

import android.database.ContentObserver;
import android.os.Handler;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.util.Log;

/**
 * @ClassName: SMSObserver
 * @author Sean.Xie
 * @date 2012-4-11 上午10:05:29
 */
public class ThreadsObserver extends ContentObserver {
    private final static String TAG = ThreadsObserver.class.getSimpleName();
    private final CoreService service;
    private final int INTERVAL_MILLIONS = 300;// 两次onchage事件必须间隔200毫秒以上，才处理
    private final int CLEAR_IGNORE_MILLIONS = 5000;// 5秒没有收到onchage事件，则重置ignore
    private final Handler handler;
    private int ignore = 0;
    private long lastOnchageTime;
    private boolean locked = false;
    private int removeCount = 0;

    /**
     * Description: 短信监控
     * 
     * @param handler
     * @param aidlService
     */
    public ThreadsObserver(Handler handler, CoreService coreService) {
        super(null);
        this.handler = handler;
        this.service = coreService;

    }

    /**
     * 1。<=INTERVAL_MILLIONS 不处理 |2。ignore>0 不处理
     */
    @Override
    public void onChange(boolean selfChange) {
        boolean isBind = SettingsPreferences.getBindStatus().equals(
                SettingsPreferences.BIND_LOCAL);
        if (!isBind || CommonPreferences.getLogoutedStatus())
        {
            Log.i(TAG,
                    "-----onChange getLastSyncThreadsTime ="
                            + MainApp.i().getLastSyncThreadsTime()
                            + " CommonPreferences.getLogoutedStatus="
                            + CommonPreferences.getLogoutedStatus() + " return!");
            return;
        }
        Log.i(TAG, "-----onChange ignore=" + ignore);
        if (locked) {
            Log.i(TAG, "-----locked!");
            return;
        }
        long last = lastOnchageTime;
        lastOnchageTime = System.currentTimeMillis();
        handler.removeCallbacks(rClearIgnore);
        handler.postDelayed(rClearIgnore, CLEAR_IGNORE_MILLIONS);
        if (ignore > 0) {
            Log.i(TAG, "-----不处理 (ignore > 0)");
            ignore--;
            return;
        }
        if ((lastOnchageTime - last) <= INTERVAL_MILLIONS) {
            Log.i(TAG, "-----不处理 (onChange <= INTERVAL_MILLIONS)");
            removeCount++;
            handler.removeCallbacks(rCheckSms);
            handler.post(rCheckSms);
            return;
        }
        handler.post(rCheckSms);
    }

    @Override
    public boolean deliverSelfNotifications() {
        Log.i(TAG, "-----deliverSelfNotifications-----");
        return super.deliverSelfNotifications();
    }

    public void setIgnore(int i) {
        ignore = i;
    }

    public int getIgnore() {
        return ignore;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    private final Runnable rClearIgnore = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "-----Runnable rClearIgnore ignore=" + ignore);
            if ((System.currentTimeMillis() - lastOnchageTime) >= CLEAR_IGNORE_MILLIONS) {
                Log.i(TAG, "-----ClearIgnore ignore!!!");
                ignore = 0;
            }
        }
    };

    private final Runnable rCheckSms = new Runnable() {
        @Override
        public void run() {
            service.getMessageModule().checkSMS();
        }
    };

}
