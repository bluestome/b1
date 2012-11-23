
package android.skymobi.messenger.bizunit.auth;

import android.os.Handler;
import android.os.Message;
import android.skymobi.app.print;
import android.skymobi.app.c2v.RevData;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.bizunit.BaseBU;
import android.skymobi.messenger.dataaccess.DAManager;
import android.skymobi.messenger.dataaccess.auth.IAuthDA;
import android.skymobi.messenger.dataaccess.bean.SettingUser;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.ui.handler.event.EventMsg;

import com.skymobi.android.sx.codec.beans.ppa.LoginPhoneRequest;
import com.skymobi.android.sx.codec.beans.ppa.LoginPhoneResponse;
import com.skymobi.android.sx.codec.beans.ppa.SetUserInfoResp;

/**
 * 登录用户的对外接口，对接AAA登录认证注册等功能
 */
public class LoginBU extends BaseBU {

    private static final String TAG = LoginBU.class.getSimpleName();

    private IAuthDA authDA = null;

    public LoginBU(Handler handler) {
        super(handler);
        authDA = (IAuthDA) DAManager.get(DAManager.DA_USER_AUTH);
    }

    /**
     * 获取当前登录用户的信息
     */
    public UserInfo getCurrentUserInfo() {
        return authDA.getCurrentUserInfo();
    }

    /**
     * 获取是否已经绑定本地
     */
    public boolean isBindLocal() {
        return authDA.isBindLocal();
    }

    public void login(final String userName, final String pwd) {
        SLog.d(TAG, "登录...");
        sendEventMsg(EventMsg.EVENT_LOGIN_LOGINING);
        boolean isSendSuccess = client.getNetBiz().login(getContext(),
                userName, pwd.getBytes());
        if (!isSendSuccess) {
            sendEventMsg(EventMsg.EVENT_NET_ERROR);
        } else {
            authDA.addLoginTimes();
        }
    }

    public void login(final String userName, final byte[] pwd) {
        SLog.d(TAG, "登录...");
        sendEventMsg(EventMsg.EVENT_LOGIN_LOGINING);
        boolean isSendSuccess = client.getNetBiz().login(getContext(),
                userName, pwd);
        if (!isSendSuccess) {
            sendEventMsg(EventMsg.EVENT_NET_ERROR);
        } else {
            authDA.addLoginTimes();
        }
    }

    /**
     * 激活引导页，保存昵称及性别
     */
    public void saveActivateGuide(final String nickName, final String sex) {
        SLog.d(TAG, "用户资料修改:激活引导页昵称性别保存...");

        UserInfo userInfo = getCurrentUserInfo();
        // 保存至setting配置
        SettingUser settingUser = new SettingUser(userInfo.skyid);
        // 先保存为昵称，该昵称为系统分配 ,mp....
        settingUser.setNickName(userInfo.nickname);
        // 保留用户填写的昵称,等服务端返回修改成功后更新
        settingUser.setReserve0(nickName);
        settingUser.setSex(sex);
        authDA.saveSettingUserInfoToPreferences(settingUser);

        boolean isSuccess = client.getNetBiz().setCurrentUserInfo(
                        getContext(), userInfo.skyid, userInfo.token, nickName,
                        sex);
        if (isSuccess) {
            sendEventMsg(EventMsg.EVENT_ACTIVATE_GUIDE_SUCCESS);
        } else {
            sendEventMsg(EventMsg.EVENT_ACTIVATE_GUIDE_FAIL);
        }

    }

    @Override
    public void revData(final RevData data) {
        print.v(TAG, "Thread id(revData):" + Thread.currentThread().getId());
        print.v(TAG, "data.getReqBean():" + data.getReqBean());
        // 请求超时了
        if (data.getReqBean() != null) {
            if (data.getReqBean() instanceof LoginPhoneRequest) {
                SLog.w(TAG, "登录超时了!!!");
                sendEventMsg(EventMsg.EVENT_LOGIN_FAIL);
                return;
            } else {
                return;
            }
        }
        /** 收到登录响应 */
        if (data.getRespBean() instanceof LoginPhoneResponse) {
            LoginPhoneResponse resp = (LoginPhoneResponse) data.getRespBean();
            switch (resp.getResponseCode()) {
                case SUCCESS:
                    SLog.d(TAG, "登录成功!");
                    UserInfo info = new UserInfo();
                    info.skyid = resp.getSkyId();
                    info.token = resp.getToken();
                    info.encryptPasswd = resp.getEncryptPasswd();
                    // 保存用户信息
                    authDA.saveUserInfoToPreferences(info);
                    // 保持skyid至setting配置文件
                    authDA.saveSettingUserInfoToPreferences(new SettingUser(
                            resp.getSkyId()));
                    // 向service通知878373
                    CoreService.getInstance().notifyObservers(
                            CoreServiceMSG.MSG_LOGIN_SUCCESS,
                            authDA.getCurrentUserInfo());

                    sendEventMsg(EventMsg.EVENT_LOGIN_SUCCESS);

                    break;
                default:
                    SLog.w(
                            TAG,
                            "登录失败!" + resp.getResponseCode() + ","
                                    + resp.getResponseMsg());
                    Message msg = new Message();
                    msg.what = EventMsg.EVENT_LOGIN_FAIL;
                    msg.obj = resp.getResponseMsg();
                    sendEventMsg(msg);
                    break;
            }
        }

        // 收到修改用户资料的响应
        else if (data.getRespBean() instanceof SetUserInfoResp) {
            SetUserInfoResp resp = (SetUserInfoResp) data.getRespBean();
            switch (resp.getResponseCode()) {
                case SUCCESS:
                    SettingUser settingUser = authDA.getSettingUserInfo();
                    settingUser.setNickName(settingUser.getReserve0());
                    settingUser.setReserve0("");
                    authDA.saveSettingUserInfoToPreferences(settingUser);

                    UserInfo localUserInfo = new UserInfo();
                    localUserInfo.skyid = settingUser.getSkyid();
                    localUserInfo.nickname = settingUser.getNickName();
                    authDA.saveUserInfoToPreferences(localUserInfo);

                    SLog.i(TAG, "用户资料修改成功!");
                    break;
                default:
                    SLog.e(TAG, "用户资料修改失败!![" + resp.getResponseMsg() + "]");
                    sendEventMsg(EventMsg.EVENT_SET_USERINFO_FAIL);
                    break;
            }
        }
    }
}
