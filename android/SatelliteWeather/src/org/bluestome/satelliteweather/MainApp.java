
package org.bluestome.satelliteweather;

import android.app.Application;
import android.content.Intent;

import junit.framework.Assert;

import org.bluestome.satelliteweather.biz.Biz;
import org.bluestome.satelliteweather.services.LifeService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 该类提供全局方法和变量功能，由系统启动时自动构造 */
public class MainApp extends Application {

    private static final String TAG = MainApp.class.getSimpleName();
    private static MainApp instance = null;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Biz biz = null;

    public static MainApp i() {
        if (instance == null) {
            Assert.assertTrue("获取到的application 为空", instance != null);
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (null == biz) {
            biz = new Biz();
        }
        biz.initAlarmRecevier();
    }

    public void startLifeService() {
        // 启动后台服务
        Intent intentService = new Intent(this, LifeService.class);
        intentService.putExtra("caller", "MainApp");
        startService(intentService);
    }

    public void stopService() {
        // 先停止服务，防止服务被重启
        Intent intentService = new Intent(this, LifeService.class);
        stopService(intentService);

        if (null != biz) {
            biz.uninitAlarmRecevier();
        }
    }

    /**
     * @return the executorService
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

}
