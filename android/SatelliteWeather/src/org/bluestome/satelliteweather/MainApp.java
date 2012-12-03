
package org.bluestome.satelliteweather;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.bluestome.satelliteweather.biz.SatelliteWeatherBiz;
import org.bluestome.satelliteweather.services.LifeService;

import android.app.Application;
import android.content.Intent;

/** 该类提供全局方法和变量功能，由系统启动时自动构造 */
public class MainApp extends Application {

    private static MainApp instance = null;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SatelliteWeatherBiz biz = null;
    private String lastModifyTime = null;

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
            biz = new SatelliteWeatherBiz();
        }
        biz.initAlarmRecevier();
    }

    public void startLifeService() {
        Intent intentService = new Intent(this, LifeService.class);
        intentService.putExtra("caller", "MainApp");
        startService(intentService);
    }

    public void stopService() {
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

	public SatelliteWeatherBiz getBiz() {
		return biz;
	}

	public void setBiz(SatelliteWeatherBiz biz) {
		this.biz = biz;
	}

	public String getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}
    

}
