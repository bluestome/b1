
package android.skymobi.messenger.bizunit.auth;

import android.os.Handler;
import android.skymobi.app.print;
import android.skymobi.app.c2v.RevData;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.bizunit.BaseBU;
import android.skymobi.messenger.dataaccess.DAManager;
import android.skymobi.messenger.dataaccess.auth.IAuthDA;
import android.skymobi.messenger.dataaccess.bean.Channel;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.handler.event.EventMsg;

import com.skymobi.android.sx.codec.beans.ppa.RegisterReq;
import com.skymobi.android.sx.codec.beans.ppa.RegisterResp;
import com.skymobi.android.sx.codec.beans.ppa.RegisterUserInfo;

public class RegisterBU extends BaseBU {

    private static final String TAG = RegisterBU.class.getSimpleName();

    private IAuthDA authDA = null;

    public RegisterBU(Handler handler) {
        super(handler);
        authDA = (IAuthDA) DAManager.get(DAManager.DA_USER_AUTH);
    }

    /**
     * 新用户注册
     */
    public void register() {
        SLog.d(TAG, "新用户注册...");
        sendEventMsg(EventMsg.EVENT_REGISTER_ING);
        boolean isSendSuccess = client.getNetBiz().register(getContext());
        if (!isSendSuccess) {
            sendEventMsg(EventMsg.EVENT_NET_ERROR);
        }
    }

    /**
     * 能不能再次发起绑定
     */
    public boolean isCanSendBind() {
        return authDA.isCanSendBind();
    }

    public void sendBindSMS() {
        SLog.d(TAG, "发送绑定短信...");

        Channel channel = authDA.getBindChannel();
        if (channel == null || channel.getTargetNo() == null
                        || channel.getTargetNo().length == 0) {
            SLog.w(TAG, "未获取到通道号码，无法绑定！");
            return;
        }
        CoreService.getInstance().registerObserverForCommonSMS();
        // 为了兼容老版本,直接调用messageModule
        CoreService
                        .getInstance()
                        .getMessageModule()
                        .sendActivateSMSMsg(channel.getTargetNo(),
                                channel.getContent());

    }

    @Override
    public void revData(final RevData data) {
        print.v(TAG, "Thread id(revData):" + Thread.currentThread().getId());
        // 请求超时了
        if (data.getReqBean() != null) {
            if (data.getReqBean() instanceof RegisterReq) {
                SLog.w(TAG, "注册超时了!!!");
                sendEventMsg(EventMsg.EVENT_TIMEOUT);
                return;
            }
        }
        /** 收到注册响应 */
        if (data.getRespBean() instanceof RegisterResp) {

            RegisterResp resp = (RegisterResp) data.getRespBean();
            SLog.d(TAG, "收到注册响应..." + resp);
            UserInfo info = new UserInfo();
            switch (resp.getResponseCode()) {
                case SUCCESS:
                    SLog.d(TAG, "注册成功!" + resp.toString());
                    RegisterUserInfo userInfo = resp.getRegisterUserInfo();
                    if (userInfo != null) {
                        SLog.d(TAG, "抽取用户基本信息..");
                        info.skyid = userInfo.getSkyId();
                        info.name = userInfo.getUsername();
                        info.nickname = userInfo.getNickname();
                        info.pwd = userInfo.getPasswd();

                        info.encryptPasswd = resp.getEncryptPasswd();
                        // 保存用户信息
                        authDA.saveUserInfoToPreferences(info);

                        SLog.d(TAG, "注册成功后，模拟一条网络消息...");
                        CoreService
                                .getInstance()
                                .getMessageModule()
                                .createRegisterSMS(
                                        "激活成功！\r\n账号[" + info.name + "]密码[" + info.pwd
                                                + "]，请牢记！\r\n为保障您的帐号安全，请尽快前往设置中修改密码。");

                    }
                    // 保存注册成功后下发的通道号，用于登录成功后激活
                    authDA.saveBindChannel(new Channel(new String[] {
                            resp.getRecvsmsmobile(), resp.getRecvsmsmobile2(),
                            resp.getRecvsmsmobile3()
                    }, resp.getSmscontent()));
                    // 向service通知
                    CoreService.getInstance().notifyObservers(
                            CoreServiceMSG.MSG_REGISTER_SUCESS, null);

                    sendEventMsg(EventMsg.EVENT_REGISTER_SUCCESS);

                    break;
                case 160105:
                    SLog.w(TAG,
                            "已经被激活过的手机!" + resp.getResponseMsg());
                    sendEventMsg(EventMsg.EVENT_REGISTER_IS_ACTIVATED);
                    break;
                default:
                    SLog.w(TAG,
                            "注册响应收到未知的编码!" + resp.getResponseCode());
                    sendEventMsg(EventMsg.EVENT_REGISTER_FAIL);
                    break;
            }

        }
    }

}
