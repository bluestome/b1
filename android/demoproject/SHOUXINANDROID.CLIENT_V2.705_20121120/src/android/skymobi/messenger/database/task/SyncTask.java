package android.skymobi.messenger.database.task;

import android.skymobi.messenger.service.CoreService;

/**
 * @ClassName: SyncTask
 * @Description: TODO
 * @author Sean.Xie
 * @date 2012-7-13 下午2:14:46
 */
public abstract class SyncTask implements Runnable {
    public static boolean isRunningContactSync;
    protected static CoreService service;
    
}

