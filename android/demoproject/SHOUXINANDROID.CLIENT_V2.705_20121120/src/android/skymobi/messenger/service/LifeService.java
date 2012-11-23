
package android.skymobi.messenger.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;

/**
 * 维持手信在后台运行的Service,被杀死后能够进行重启
 * 
 * @author zzy
 */
public class LifeService extends Service {

    private static final String TAG = LifeService.class.getSimpleName();

    private static boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        SLog.d(TAG, "onCreate-" + this);
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        isRunning = true;
        SLog.d(TAG, "onStartCommand-" + this + " intent=" + intent);
        flags = START_STICKY;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SLog.d(TAG, "onDestroy-" + this);

        // 一般是被系统关闭，尽量能够释放一些资源
        MainApp.freeMemory();
        isRunning = false;
        // 进程被关闭后能够自动启动
        Intent serviceIntent = new Intent(this, LifeService.class);
        serviceIntent.putExtra("caller", "LifeService");
        this.startService(serviceIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
