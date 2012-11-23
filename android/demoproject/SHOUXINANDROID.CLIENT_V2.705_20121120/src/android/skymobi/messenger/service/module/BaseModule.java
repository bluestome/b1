
package android.skymobi.messenger.service.module;

import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.skymobi.messenger.network.NetWorkMgr;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.Observer;
import android.skymobi.messenger.utils.Constants;

import java.util.concurrent.ExecutorService;
/**
 * @ClassName: BaseModule
 * @author Sean.Xie
 * @date 2012-3-2 上午11:28:44
 */
public class BaseModule {
	
	protected static final String TIME_TAG = "Time-consuming";

    protected CoreService service;
    protected ExecutorService mThreadPool;
    protected ExecutorService mSyncPool;
    protected NetWorkMgr netWorkMgr;

    protected DaoFactory daoFactory;

    // 进行进度更新
    protected Observer processObserver = new Observer() {
        // 同步分成下面几个步骤
        // 1. 会话同步 0~5
        // 2. 消息同步 6~25
        // 3. 联系人同步 30~100

        @Override
        public void notifyObserver(int what, Object obj) {
            switch (what) {
                case CoreServiceMSG.MSG_THREADS_SYNC_BEGIN:
                    service.notifyObservers(
                            CoreServiceMSG.MSG_THREADS_SYNC_PROGRESS,
                            Constants.SYNC_PROCESS_THREADS_END);
                    break;
                case CoreServiceMSG.MSG_THREADS_SYNC_END:
                    service.notifyObservers(
                            CoreServiceMSG.MSG_THREADS_SYNC_PROGRESS,
                            Constants.SYNC_PROCESS_MESSAGES_BEGIN);
                    break;
                case CoreServiceMSG.MSG_MESSAGES_SYNC_BEGIN:

                    service.notifyObservers(
                            CoreServiceMSG.MSG_THREADS_SYNC_PROGRESS,
                            Constants.SYNC_PROCESS_MESSAGES_END);
                    break;
                case CoreServiceMSG.MSG_MESSAGES_SYNC_END:
                    service.notifyObservers(
                            CoreServiceMSG.MSG_THREADS_SYNC_PROGRESS,
                            Constants.SYNC_PROCESS_CONTACTS_BEGIN);
                    break;
                case CoreServiceMSG.MSG_CONTACTS_SYNC_PROCESS:
                    service.notifyObservers(
                            CoreServiceMSG.MSG_THREADS_SYNC_PROGRESS, obj);
                    break;
                default:
                    break;

            }
        }
    };
    public BaseModule(CoreService service) {
        this.service = service;
        mThreadPool = service.getThreadPool();
        netWorkMgr = NetWorkMgr.getInstance();
        daoFactory = DaoFactory.getInstance(MainApp.i());
        mSyncPool = service.getSyncPool();
    }
}
