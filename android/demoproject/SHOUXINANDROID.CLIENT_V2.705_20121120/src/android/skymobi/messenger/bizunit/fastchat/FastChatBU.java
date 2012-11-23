
package android.skymobi.messenger.bizunit.fastchat;

import android.os.Handler;
import android.skymobi.app.c2v.RevData;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bean.Message;
import android.skymobi.messenger.bean.ResFile;
import android.skymobi.messenger.bizunit.BaseBU;
import android.skymobi.messenger.provider.SocialMessenger.MessagesColumns;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;

import com.skymobi.android.sx.codec.beans.sis.SxApplyFastTalkResp;
import com.skymobi.android.sx.codec.beans.sis.SxLeaveFastTalkResp;

/**
 * @ClassName: FastChatBU
 * @Description: FastChat BizUinit
 * @author Bluestome.Zhang
 * @date 2012-10-12 上午10:23:20
 */
public class FastChatBU extends BaseBU {

    private static String TAG = FastChatBU.class.getSimpleName();

    /**
     * @param handler
     */
    public FastChatBU(Handler handler) {
        super(handler);
    }

    /**
     * 申请快聊
     * 
     * @param usex
     * @return
     */
    public void applyFastChat(final String usex) {
        SLog.d(TAG, "申请快聊,自己的性别:" + usex);
        sendEventMsg(CoreServiceMSG.MSG_FASTCHAT_APPLY_BEGIN);
        boolean success = client.getNetBiz().applyFastChat(getContext(), usex);
        if (!success) {
            sendEventMsg(CoreServiceMSG.MSG_NET_ERROR);
        }
    }

    /**
     * 离开快聊
     * 
     * @param destSkyid
     * @return
     */
    public void leaveFastChat(final int destSkyid) {
        SLog.d(TAG, "离开快聊,参数:" + destSkyid);
        boolean success = client.getNetBiz().leaveFastChat(getContext(), destSkyid);
        if (!success) {
            // 离开快聊发送失败
            SLog.d(TAG, "离开快聊发送失败");
            sendEventMsg(CoreServiceMSG.MSG_NET_ERROR);
        }
    }

    @Override
    public void revData(RevData data) {
        // 处理请求超时了
        if (data.getReqBean() != null) {
            if (data.getReqBean() instanceof SxApplyFastTalkResp) {
                SLog.w(TAG, "申请快聊接口超时");
                sendEventMsg(CoreServiceMSG.MSG_FASTCHAT_APPLY_REQ_FAIL);
                return;
            } else if (data.getReqBean() instanceof SxLeaveFastTalkResp) {
                SLog.w(TAG, "离开快聊接口超时");
                sendEventMsg(CoreServiceMSG.MSG_FASTCHAT_LEAVE_FAIL);
                return;
            } else {
                return;
            }
        }

        if (data.getRespBean() instanceof SxApplyFastTalkResp) {
            // 申请快聊
            SxApplyFastTalkResp resp = (SxApplyFastTalkResp) data.getRespBean();
            if (null != resp) {
                switch (resp.getResponseCode()) {
                    case 200:
                        if (resp.getNextRespCode() == 616) {
                            // 如果是老会话，则获取目标好友的SKYID
                            SLog.d(TAG, "申请快聊成功,维持原有会话，对方的SKYID为:" + resp.getDestSkyid());
                            MainApp.getFastChatCache().clearAll();
                            MainApp.getFastChatCache().setMatchedSkyid(resp.getDestSkyid());
                            sendEventMsg(CoreServiceMSG.MSG_FASTCHAT_APPLY_SUCCESS);
                        }
                        break;
                    default:
                        // 申请快聊失败
                        SLog.w(TAG, "申请快聊失败");
                        sendEventMsg(CoreServiceMSG.MSG_FASTCHAT_APPLY_REQ_FAIL);
                        break;
                }
            }
        }
        if (data.getRespBean() instanceof SxLeaveFastTalkResp) {
            // 离开快聊
            SxLeaveFastTalkResp resp = (SxLeaveFastTalkResp) data.getRespBean();
            if (null != resp) {
                switch (resp.getResponseCode()) {
                    case SUCCESS:
                        SLog.d(TAG, "离开快聊成功!");
                        sendEventMsg(CoreServiceMSG.MSG_FASTCHAT_LEAVE_SUCCESS);
                        break;
                    default:
                        SLog.d(TAG, "离开快聊失败!");
                        sendEventMsg(CoreServiceMSG.MSG_FASTCHAT_LEAVE_FAIL);
                        break;
                }
            }
        }
    }

    // 发送语音
    public void sendFastChatVoice(final Message msg, final ResFile file,
            final int destSkyid) {
        if (CoreService.getInstance() != null)
            CoreService.getInstance().getFastChatModule()
                    .sendFastChatVoice(msg, file, destSkyid, false);
    }

    // 重新发送语音
    // pos 为重发语音的位置下标（0~N-1）
    public void reSendFastChatVoice(int pos) {
        Message msg = MainApp.getFastChatCache().getChatMsg().get(pos);
        msg.setStatus(MessagesColumns.STATUS_SENDING);
        ResFile file = msg.getResFile();
        int destSkyid = MainApp.getFastChatCache().getMatchedSkyid();
        if (CoreService.getInstance() != null)
            CoreService.getInstance().getFastChatModule()
                    .sendFastChatVoice(msg, file, destSkyid, true);

    }

}
