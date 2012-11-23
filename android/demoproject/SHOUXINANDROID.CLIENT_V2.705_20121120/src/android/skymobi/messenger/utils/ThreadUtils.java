
package android.skymobi.messenger.utils;

import android.util.Log;

/**
 * @ClassName: ThreadUtils
 * @Description: Thread Log
 * @author Michael.Pan
 * @date 2012-9-19 上午10:22:17
 */
public class ThreadUtils {
    public static void PrintThreadInfo(String TAG) {
        Thread thread = Thread.currentThread();
        long threadID = thread.getId();
        String name = thread.getName();
        Log.e("Thread", TAG + "-->threadID = " + threadID + " , name = " + name);
    }
}
