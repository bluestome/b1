
package android.skymobi.messenger.bizunit.contact;

import android.os.Handler;
import android.skymobi.app.c2v.RevData;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.bizunit.BaseBU;
import android.skymobi.messenger.ui.handler.event.EventMsg;

import com.skymobi.android.sx.codec.beans.sis.SxInformReq;
import com.skymobi.android.sx.codec.beans.sis.SxInformResp;

/**
 * @ClassName: InformBU
 * @Description: 举报任务处理
 * @author dylan.zhao
 * @date 2012-9-14 下午01:56:35
 */
public class InformBU extends BaseBU {
    private static final String TAG = InformBU.class.getSimpleName();

    public InformBU(Handler handler) {
        super(handler);

    }

    /**
     * @method 举报
     * @param skyId: 被举报者的 skyId
     * @param type: 举报类型
     */
    public void inform(final int skyId, final byte typeId) {
        // 调用网络层，发送举报请求
        if (!isLoggedIn()) {
            SLog.d(TAG, "举报-AO-未登录！");
            sendEventMsg(EventMsg.EVENT_INFORM_FAILED);
            return;
        }
        SLog.d(TAG, "举报-AO-skyId=" + skyId + "typeId=" + typeId);
        // submit(new Runnable() {
        // @Override
        // public void run() {
        client.getNetBiz().inform(getContext(), skyId, typeId);
        // }
        // });
    }

    /**
     * 处理举报的响应
     */
    @Override
    public void revData(RevData data) {

        SLog.d(TAG, "revData举报响应：" + data.toString());
        if (data.getReqBean() != null) {
            if (data.getReqBean() instanceof SxInformReq) {
                SLog.w(TAG, "举报超时了!!!");
                sendEventMsg(EventMsg.EVENT_INFORM_FAILED);
                return;
            } else {
                return;
            }
        }
        if (data.getRespBean() instanceof SxInformResp) {
            sendEventMsg(EventMsg.EVENT_INFORM_SUCCESS);
        }
    }

}
