
package android.skymobi.messenger.database.observer;

import android.database.ContentObserver;
import android.os.Handler;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;

import java.util.ArrayList;

/**
 * @ClassName: SMSObserver
 * @author Sean.Xie
 * @date 2012-4-11 上午10:05:29
 */
public class SMSObserver extends ContentObserver {

    private CoreService service;
    private Handler handler;

    /**
     * Description: 短信监控
     * 
     * @param handler
     * @param aidlService
     */
    public SMSObserver(Handler handler, CoreService coreService) {
        super(handler);
        this.handler = handler;
        this.service = coreService;
    }

    @Override
    public void onChange(boolean selfChange) {
        ArrayList<String> obj = null;
        obj = service.inComeSMS();
        if (handler != null && obj != null && obj.size() > 0) {
            handler.sendMessage(handler.obtainMessage(CoreServiceMSG.MSG_SMSMSG_RECEIVE_COMMON,
                    obj));
        }
    }
}
