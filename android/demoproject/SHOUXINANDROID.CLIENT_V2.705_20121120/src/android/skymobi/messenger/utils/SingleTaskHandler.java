
package android.skymobi.messenger.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.skymobi.common.log.SLog;

/**
 * @ClassName: SingleTaskHandler
 * @Description: 以单队列的方式完成提交的任务(Runnable)
 * @author zzy
 * @date 2012-10-18 下午7:26:36
 */
public final class SingleTaskHandler {

    private final static String TAG = SingleTaskHandler.class.getSimpleName();

    // 用于标记任务，可以清除未开始的任务
    private final static String TOKEN = "token";

    // 负责处理task的handler
    private Handler handler = null;

    // 负责处理task的thread
    private HandlerThread thread = null;

    // 名称
    private String name = TAG;

    public SingleTaskHandler(String name) {
        if (name == null || name.length() == 0) {
            this.name = name;
        }

        // 开启任务队列处理
        thread = new HandlerThread(this.name);
        thread.start();

        handler = new Handler(thread.getLooper());
    }

    /**
     * 向任务队列提交任务，延时处理
     * 
     * @param r 执行对象
     * @param delayMillis 延时(毫秒)
     */
    public void postDelayed(Runnable r, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        if (handler.postAtTime(r, TOKEN, SystemClock.uptimeMillis() + delayMillis) == false) {
            SLog.e(TAG, name + " postAtTime failed!");
        }
    }

    /**
     * 向任务队列提交任务，立即处理。
     * 
     * @param r 执行对象
     */
    public void post(Runnable r) {

        if (handler.postAtTime(r, TOKEN, SystemClock.uptimeMillis()) == false) {
            SLog.e(TAG, name + " postAtTime failed!");
        }
    }

    /**
     * 清除排队任务,但是不会中断正在执行的任务
     */
    public void clearPendingTask() {
        handler.removeCallbacksAndMessages(TOKEN);
    }
}
