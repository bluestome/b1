
package org.bluestome.satelliteweather.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.bluestome.satelliteweather.biz.SatelliteWeatherBiz;

/**
 * 维持手信在后台运行的Service,被杀死后能够进行重启
 * 
 * @author zzy
 */
public class LifeService extends Service {

    private static boolean isRunning = false;
    private SatelliteWeatherBiz biz = null;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        isRunning = true;
        flags = START_STICKY;
        if (null == biz) {
            biz = new SatelliteWeatherBiz();
        }
        biz.initAlarmRecevier();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (null != biz) {
            biz.uninitAlarmRecevier();
        }
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

    /**
     * @return the biz
     */
    public SatelliteWeatherBiz getBiz() {
        return biz;
    }

    /**
     * @param biz the biz to set
     */
    public void setBiz(SatelliteWeatherBiz biz) {
        this.biz = biz;
    }

}
