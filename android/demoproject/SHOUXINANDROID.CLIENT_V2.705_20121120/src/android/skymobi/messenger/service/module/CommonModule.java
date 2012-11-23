
package android.skymobi.messenger.service.module;

import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.network.module.CommonNetModule;
import android.skymobi.messenger.service.CoreService;
import android.skymobi.messenger.service.CoreServiceMSG;
import android.skymobi.messenger.utils.CommonPreferences;
import android.skymobi.messenger.utils.Constants;
import android.skymobi.messenger.utils.SettingsPreferences;
import android.skymobi.messenger.utils.StringUtil;
import android.text.TextUtils;

import com.skymobi.android.sx.codec.beans.clientbean.NetBindResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetForgetPwdResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetConfigurationResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetLoginResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetResponse;
import com.skymobi.android.sx.codec.beans.common.ConfigInfo;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

/**
 * @ClassName: CommonModule
 * @Description: 公共组件
 * @author Sean.Xie
 * @date 2012-2-13 下午2:13:33
 */
public class CommonModule extends BaseModule {

    private CommonNetModule commonNetModule = null;
    private static final String TAG = CommonModule.class.getSimpleName();

    public CommonModule(CoreService service) {
        super(service);
        commonNetModule = netWorkMgr.getCommonModule();
    }

    /*
     * 注册
     * @param username
     * @param pwd public void register(final String username, final String pwd)
     * { mThreadPool.execute(new Runnable() {
     * @Override public void run() { UserInfo userInfo = syncRegister(username,
     * pwd); service.notifyObservers(userInfo.msgCode, userInfo); } }); }
     */

    /*
     * private synchronized UserInfo syncRegister(String username, String pwd) {
     * UserInfo info = new UserInfo(); info.msgCode = CoreServiceMSG.MSG_FAILED;
     * if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) { info.msgCode
     * = CoreServiceMSG.MSG_LOGIN_ACCOUNT_ERROR; return info; } else if
     * (TextUtils.isDigitsOnly(username) && username.length() == 11) {
     * info.msgCode = CoreServiceMSG.MSG_LOGIN_ACCOUNT_ERROR; return info; }
     * else if (!(pwd.trim().length() > 5 && pwd.trim().length() < 13)) {
     * info.msgCode = CoreServiceMSG.MSG_LOGIN_ACCOUNT_ERROR; return info; }
     * NetRegResponse respReg = commonNetModule.register(username, pwd); if
     * (respReg.isNetError() || respReg.isFailed()) { if
     * (respReg.getResultCode() == -1) { respReg.setResult(Constants.NET_ERROR,
     * respReg.getResultHint()); } info.resultHint = respReg.getResultHint() +
     * "\r\n[" + Constants.ERROR_TIP + ":0x" +
     * StringUtil.autoFixZero(respReg.getResultCode()) + "]"; return info; }
     * else { info.skyid = respReg.getSkyId(); info.name =
     * respReg.getUsername(); info.nickname = respReg.getNickname(); info.pwd =
     * respReg.getPasswd(); } // 新增激活后发送短信 String to =
     * respReg.getRecvsmsmobile(); String content = respReg.getSmscontent(); if
     * (null != to && null != content) { // 发送激活短信
     * service.getMessageModule().sendActivateSMSMsg(to, content); } // 登陆
     * NetLoginResponse respLogin = commonNetModule.login(info.name, info.pwd);
     * if (respLogin.isNetError() || respLogin.isFailed()) { if
     * (respLogin.getResultCode() == -1) {
     * respLogin.setResult(Constants.NET_ERROR, respLogin.getResultHint()); }
     * info.resultHint = respLogin.getResultHint() + "\r\n[" +
     * Constants.ERROR_TIP + ":0x" +
     * StringUtil.autoFixZero(respLogin.getResultCode()) + "]"; return info; }
     * else { info.token = respLogin.getToken(); info.encryptPasswd =
     * respLogin.getEncryptPasswd(); } info.msgCode =
     * CoreServiceMSG.MSG_SUCCESS; SettingsPreferences.saveSKYID(info.skyid);
     * return info; }
     */

    /**
     * 激活接口 public void activate() { mThreadPool.execute(new Runnable() {
     * 
     * @Override public void run() { UserInfo userInfo = syncActivate();
     *           service.notifyObservers(userInfo.msgCode, userInfo); } }); }
     */

    /* *//**
     * 激活
     * 
     * @return
     */
    /*
     * private synchronized UserInfo syncActivate() { SLog.d(TAG,
     * "user to activate"); UserInfo info = new UserInfo(); info.msgCode =
     * CoreServiceMSG.MSG_FAILED; // TODO 判断是否已经注册成功 // TODO //
     * 新增注册成功标志registerFlag，用于标识当前设备是否激活，如果激活成功，则发送注册成功的短信,否则，还没有激活过，则走激活流程。 //
     * 并且需要保存当前注册成功的用户信息 if (CommonPreferences.getRegisterFlag()) { // TODO
     * 注册成功的逻辑，发送上次保存的激活短信 String to = CommonPreferences.getActiviteSMSTo();
     * String content = CommonPreferences.getActiviteSMSContent(); SLog.d(TAG,
     * "register success,just to send sms to:" + to + "|content:" + content); if
     * (null != to && null != content) { info = CommonPreferences.getUserInfo();
     * service.getMessageModule().sendActivateSMSMsg(to, content); } else { //
     * 获取短信失败,则直接返回,同时将注册标识为未注册 CommonPreferences.setRegisterFlag(false); //
     * 获取短信通道和内容失败 String hit = MainApp.getInstance().getString(
     * android.skymobi.messenger.R.string.register_error_sms); info.resultHint =
     * hit; return info; } } else { SLog.d(TAG, "first register "); // 注册
     * NetRegResponse respRegister = commonNetModule.register(); if
     * (respRegister.isNetError()) { CommonPreferences.setRegisterFlag(false);
     * if (respRegister.getResultCode() == -1) {
     * respRegister.setResult(Constants.NET_ERROR,
     * respRegister.getResultHint()); } info.resultHint =
     * respRegister.getResultHint() + "\r\n[" + Constants.ERROR_TIP + ":0x" +
     * StringUtil.autoFixZero(respRegister.getResultCode()) + "]"; return info;
     * } if (respRegister.isAccountBind()) { if (respRegister.getResultCode() ==
     * -1) { respRegister.setResult(Constants.NET_ERROR,
     * respRegister.getResultHint()); } SLog.d(TAG, "account is bind");
     * CommonPreferences.setRegisterFlag(false); if
     * (respRegister.getResultHint().indexOf("注册绑定失败!") != -1) { info.resultHint
     * = respRegister.getResultHint().replace("注册绑定失败!", "该手机已激活!"); } else {
     * info.resultHint = respRegister.getResultHint(); } info.msgCode =
     * CoreServiceMSG.MSG_REGISTER_ISBIND; return info; } info.skyid =
     * respRegister.getSkyId(); info.name = respRegister.getUsername();
     * info.nickname = respRegister.getNickname(); info.pwd =
     * respRegister.getPasswd(); String to = respRegister.getRecvsmsmobile();
     * String content = respRegister.getSmscontent(); if (null != to && null !=
     * content) { CommonPreferences.setRegisterFlag(true);
     * CommonPreferences.setActiviteSMSTo(to);
     * CommonPreferences.setActiviteSMSContent(content); // 发送激活短信
     * service.getMessageModule().sendActivateSMSMsg(to, content); } else { //
     * 获取短信通道和内容失败 String hit = MainApp.getInstance().getString(
     * android.skymobi.messenger.R.string.register_error_sms); info.resultHint =
     * hit; return info; } } // 登陆 if (info == null || info.name == null ||
     * info.pwd == null) { CommonPreferences.setRegisterFlag(false); return
     * syncActivate(); } NetLoginResponse respLogin =
     * commonNetModule.login(info.name, info.pwd); if (respLogin.isNetError() ||
     * respLogin.isFailed()) { if (respLogin.getResultCode() == -1) {
     * respLogin.setResult(Constants.NET_ERROR, respLogin.getResultHint()); }
     * info.resultHint = respLogin.getResultHint() + "\r\n[" +
     * Constants.ERROR_TIP + ":0x" +
     * StringUtil.autoFixZero(respLogin.getResultCode()) + "]"; return info; }
     * else { info.token = respLogin.getToken(); info.encryptPasswd =
     * respLogin.getEncryptPasswd(); } CommonPreferences.setUserInfo(info);
     * info.msgCode = CoreServiceMSG.MSG_SUCCESS;
     * SettingsPreferences.saveSKYID(info.skyid); return info; }
     */

    /**
     * 设置用户昵称接口
     */
    public void setNickname(final int skyid, final String token, final String nickname) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                UserInfo userInfo = syncSetNickname(skyid, token, nickname);
                service.notifyObservers(userInfo.msgCode, userInfo);
            }
        });
    }

    /**
     * 设置昵称
     * 
     * @param skyid
     * @param token
     * @param nickname
     * @return
     */
    private synchronized UserInfo syncSetNickname(int skyid, String token, String nickname) {
        UserInfo info = new UserInfo();
        info.msgCode = CoreServiceMSG.MSG_FAILED;
        info.skyid = skyid;
        info.token = token;
        info.nickname = nickname;
        NetResponse resp = commonNetModule.setNickname(skyid, token, nickname);
        if (resp.isNetError() || resp.isFailed()) {
            if (resp.getResultCode() == -1) {
                resp.setResult(
                        Constants.NET_ERROR,
                        resp.getResultHint() + "\r\n[" + Constants.ERROR_TIP + ":0x"
                                + StringUtil.autoFixZero(resp.getResultCode()) + "]");
            }
            info.resultHint = resp.getResultHint();
            return info;
        }
        info.msgCode = CoreServiceMSG.MSG_SUCCESS;
        return info;
    }

    /**
     * 登录接口
     * 
     * @param name
     * @param pwd
     */
    public void login(final String name, final byte[] password, final boolean... isFindPassword) {
        CommonPreferences.saveLoginTimes(CommonPreferences.getLoginTimes() + 1);
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                UserInfo userInfo = syncLogin(name, password, isFindPassword);
                service.notifyObservers(userInfo.msgCode, userInfo);
            }
        });
    }

    /**
     * 登陆
     * 
     * @param name
     * @param pwd
     * @return
     */
    private synchronized UserInfo syncLogin(String name, byte[] pwd, boolean... isFindPassword) {
        UserInfo info = new UserInfo();
        info.msgCode = CoreServiceMSG.MSG_FAILED;
        info.name = name;
        info.encryptPasswd = pwd;
        // 登陆
        NetLoginResponse respLogin = commonNetModule.login(info.name, info.encryptPasswd);

        // 网络连接错误
        if (respLogin.isNetError()) {
            info.msgCode = CoreServiceMSG.MSG_LOGIN_NET_ERROR;
            if (respLogin.getResultCode() == -1) {
                respLogin.setResult(Constants.NET_ERROR, respLogin.getResultHint());
            }
            info.resultHint = respLogin.getResultHint() + "\r\n[" + Constants.ERROR_TIP + ":0x"
                    + StringUtil.autoFixZero(respLogin.getResultCode()) + "]";
            return info;
        }
        // 处理登录时110134错误码，被禁止登录，被冻结的情况
        if (respLogin.getResultCode() == 110113) {
            info.msgCode = CoreServiceMSG.MSG_LOGIN_FREEZE;
            // SLog.d("login", "MSG_LOGIN_FREEZE");
            return info;
        }
        // 账号不存在
        if (!respLogin.isUsernameExists()) {
            NetBindResponse respCheck = commonNetModule.getBind();
            if (respCheck.isNetError() || respCheck.isFailed()) {
                info.msgCode = CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST;
                if (respCheck.getResultCode() == -1) {
                    respCheck.setResult(Constants.NET_ERROR, respCheck.getResultHint());
                }
                info.resultHint = respCheck.getResultHint() + "\r\n[" + Constants.ERROR_TIP + ":0x"
                        + StringUtil.autoFixZero(respLogin.getResultCode()) + "]";
                return info;
            } else {
                /*
                 * 保存绑定的通道号码 hzc@20120920 *
                 */
                saveBindChannelSMS(
                        new String[] {
                                respCheck.getRecvsmsmobile(), respCheck.getRecvsmsmobile2(),
                                respCheck.getRecvsmsmobile3()
                        }, respCheck.getSmscontent());

                if (respCheck.isBound()) {
                    info.msgCode = CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST_IMSI_BIND;
                    info.resultHint = respCheck.getMobile() + Constants.separator
                            + respCheck.getUsername();
                } else {
                    boolean isPhone = StringUtil.isPhoneNumber(name);
                    // TODO #10646 需要判断登录名是否为手机号码，如果是手机号码，则需要另外提示
                    if (isPhone) {
                        // TODO 提示用户使用该手机号码的手机进行注册
                        info.msgCode = CoreServiceMSG.MSG_LOGIN_USERNAME_UNBIND_IS_PHONE;
                    } else {
                        info.msgCode = CoreServiceMSG.MSG_LOGIN_USERNAME_NOTEXIST_IMSI_UNBIND;
                    }
                }
            }
            return info;
        }

        if (!respLogin.isPasswordOk()) {
            info.msgCode = CoreServiceMSG.MSG_LOGIN_PASSWORD_ERROR;
            if (respLogin.getResultCode() == -1) {
                respLogin.setResult(Constants.NET_ERROR, respLogin.getResultHint());
            }
            info.resultHint = respLogin.getResultHint();
            return info;
        }

        info.name = respLogin.getUserName();
        info.encryptPasswd = respLogin.getEncryptPasswd();
        info.token = respLogin.getToken();
        info.skyid = respLogin.getSkyId();
        // 是否需要考虑到登录者没有绑定手机号码的情况?
        UserInfo checkBindPhoneInfo = checkBindPhone(respLogin.getMobile());
        info.msgCode = checkBindPhoneInfo.msgCode;
        info.resultHint = checkBindPhoneInfo.resultHint;
        if (null != isFindPassword && isFindPassword.length > 0) {
            info.isFindPassword = isFindPassword[0];
        }
        if (info.msgCode == CoreServiceMSG.MSG_LOGIN_SUCCESS
                || info.msgCode == CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_NOT_SAME) { // 登陆成功,保存登陆账号
            CommonPreferences.setLastLoginName(name);
        }
        // 保存当前登录用户的手机号码，用于程序中其他部分判断绑定时的依据
        if (!StringUtil.isBlank(respLogin.getMobile())) {
            SettingsPreferences.saveMobile(respLogin.getMobile());
        }
        SettingsPreferences.saveSKYID(info.skyid);
        return info;
    }

    private void saveBindChannelSMS(String[] recvsmsmobile, String content) {
        /*
         * 保存绑定的通道号码 hzc@20120920 *
         */
        if (recvsmsmobile != null && recvsmsmobile.length > 0) {
            String mobiles = "";
            for (String r : recvsmsmobile) {
                if (StringUtils.isNotBlank(r) && (!"null".equalsIgnoreCase(r))) {
                    mobiles += (r + "|");
                }
            }

            CommonPreferences.setActiviteSMSTo(mobiles);
            CommonPreferences.setActiviteSMSContent(content);
            SLog.d(TAG, "保存通道号:[" + mobiles + "]");
        } else {
            SLog.w(TAG, "未下发通道号,无法保存!");
        }

    }

    /**
     * 检查绑定手机状态
     * 
     * @return
     */
    public synchronized UserInfo checkBindPhone(String mobile) {
        SLog.d(TAG, "checkBindPhone !! ");
        UserInfo info = new UserInfo();
        if (TextUtils.isEmpty(MainApp.i().getDeviceInfo().imsi)) {
            info.msgCode = CoreServiceMSG.MSG_LOGIN_SUCCESS;
            return info;
        }
        // 根据当前登录者信息获取登录用户是否绑定手机
        NetBindResponse respBind = commonNetModule.bindPhone();
        if (respBind.isNetError()) {
            info.msgCode = CoreServiceMSG.MSG_LOGIN_NET_ERROR;
            if (respBind.getResultCode() == -1) {
                respBind.setResult(Constants.NET_ERROR, respBind.getResultHint());
            }
            info.resultHint = respBind.getResultHint() + "\r\n[" + Constants.ERROR_TIP + ":0x"
                    + StringUtil.autoFixZero(respBind.getResultCode()) + "]";
            return info;
        }

        // 账号已绑定 并且手机号码不为空
        if (respBind.isBound() && null != mobile && !mobile.equals("")) {
            // 当前用户绑定手机与当前手机IMSI不一致
            if (!respBind.isImsiSame()) {
                // 获取当前手机是否绑定帐号
                NetBindResponse respCheck = commonNetModule.getBind();
                if (respCheck.isNetError() || respCheck.isFailed()) {
                    info.msgCode = CoreServiceMSG.MSG_LOGIN_NET_ERROR;
                    if (respCheck.getResultCode() == -1) {
                        respCheck.setResult(Constants.NET_ERROR, respCheck.getResultHint());
                    }
                    info.resultHint = respCheck.getResultHint() + "\r\n[" + Constants.ERROR_TIP
                            + ":0x" + StringUtil.autoFixZero(respCheck.getResultCode()) + "]";
                    return info;
                }
                // 通道号
                String to = respBind.getRecvsmsmobile() + "|" + respBind.getRecvsmsmobile2() + "|"
                        + respBind.getRecvsmsmobile3();
                // 发送短信内容
                String content = respBind.getSmscontent();
                // hzc@20120920
                saveBindChannelSMS(
                        new String[] {
                                respCheck.getRecvsmsmobile(), respCheck.getRecvsmsmobile2(),
                                respCheck.getRecvsmsmobile3()
                        }, respCheck.getSmscontent());

                if (respCheck.isBound()) {
                    // 当前手机已经绑定帐号
                    info.msgCode = CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_NOT_SAME;
                    // 这里的mobile为登录帐号绑定的手机号码,需要向该帐号发送解绑短信
                    // respCheck.getUsername() 参数表明需要解绑的帐号名
                    if (respCheck.getResultCode() == -1) {
                        respCheck.setResult(Constants.NET_ERROR, respCheck.getResultHint());
                    }
                    info.resultHint = to + Constants.separator + content + Constants.separator
                            + respCheck.getUsername() + Constants.separator + mobile; // 账号名

                } else {
                    info.msgCode = CoreServiceMSG.MSG_LOGIN_USERNAME_BIND_IMSI_UNBIND;
                    if (respCheck.getResultCode() == -1) {
                        respCheck.setResult(Constants.NET_ERROR, respCheck.getResultHint());
                    }
                    info.resultHint = to + Constants.separator + content + Constants.separator
                            + mobile; // 账号名
                }
                return info;
            } else {
                // IMSI相同，表明是同一个手机登录，直接显示成功
                info.msgCode = CoreServiceMSG.MSG_LOGIN_SUCCESS;
                return info;
            }
        } else {
            // 帐号没有绑定 ，检查手机是否绑定帐号
            NetBindResponse respCheck = commonNetModule.getBind();
            if (respCheck.isNetError() || respCheck.isFailed()) {
                info.msgCode = CoreServiceMSG.MSG_LOGIN_NET_ERROR;
                if (respCheck.getResultCode() == -1) {
                    respCheck.setResult(Constants.NET_ERROR, respCheck.getResultHint());
                }
                info.resultHint = respCheck.getResultHint() + "\r\n[" + Constants.ERROR_TIP + ":0x"
                        + StringUtil.autoFixZero(respCheck.getResultCode()) + "]";
                return info;
            }

            String to = respBind.getRecvsmsmobile() + "|" + respBind.getRecvsmsmobile2() + "|"
                    + respBind.getRecvsmsmobile3();
            String content = respBind.getSmscontent();
            // hzc@20120920
            saveBindChannelSMS(
                    new String[] {
                            respBind.getRecvsmsmobile(), respBind.getRecvsmsmobile2(),
                            respBind.getRecvsmsmobile3()
                    }, respBind.getSmscontent());

            if (respCheck.isBound()) {
                // 手机绑定帐号
                info.msgCode = CoreServiceMSG.MSG_LOGIN_USERNAME_UNBIND_IMSI_BIND;
                if (respCheck.getResultCode() == -1) {
                    respCheck.setResult(Constants.NET_ERROR, respCheck.getResultHint());
                }
                info.resultHint = to + Constants.separator + content + Constants.separator
                        + respCheck.getUsername();
            } else {
                info.msgCode = CoreServiceMSG.MSG_LOGIN_USERNAME_UNBIND_IMSI_UNBIND;
                info.resultHint = to + Constants.separator + content;
            }
            return info;
        }
    }

    public void getBind() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                NetBindResponse bindResp = commonNetModule.getBind();
                if (bindResp.isNetError()) {
                    service.notifyObservers(CoreServiceMSG.MSG_FORGETPWD_NET_ERROR, null);
                } else if (bindResp.isSuccess()) {
                    if (bindResp.isBound()) { // 已经绑定手机
                        service.notifyObservers(CoreServiceMSG.MSG_FORGETPWD_ISBOUND, bindResp);
                    } else {
                        service.notifyObservers(CoreServiceMSG.MSG_FORGETPWD_UNBOUND, null);
                    }
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_FORGETPWD_ERROR, bindResp);
                }
            }
        });
    }

    public void forgetPwd(final String username, final String newpwd) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                NetForgetPwdResponse forget = commonNetModule.forgetPwd(username, newpwd);
                if (forget.isNetError()) {
                    service.notifyObservers(CoreServiceMSG.MSG_FORGETPWD_NET_ERROR, null);
                } else if (forget.isSuccess()) {
                    String to = forget.getRecvsmsmobile() + "|" + forget.getRecvsmsmobile2() + "|"
                            + forget.getRecvsmsmobile3();
                    String content = forget.getSmscontent();
                    // hzc@20120920
                    saveBindChannelSMS(StringUtils.split("|"), content);

                    service.getMessageModule().sendActivateSMSMsg(to, content, false);
                    // 发送短信的时间
                    CommonPreferences.saveFindPasswordSendSMSTime(System.currentTimeMillis());
                    service.notifyObservers(CoreServiceMSG.MSG_FORGETPWD_SEND_SMS_START, null);
                } else {
                    service.notifyObservers(CoreServiceMSG.MSG_FORGETPWD_ERROR, forget);
                }
            }
        });
    }

    /**
     * 触发服务器推送离线消息 如果服务端有离线消息，在接收到该请求后，会将离线消息推送到客户端
     */
    public void activeOfflineMsgPush() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (!MainApp.isLoggedIn()) {
                    return;
                }
                commonNetModule.offLineMsgPushConfirm();
            }
        });
    }

    /**
     * 获取邀请配置 类型:[1：短信邀请，2：语音邀请；]
     */
    public void getInviteConfig() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (!MainApp.isLoggedIn()) {
                    return;
                }
                // TODO 获取当前系统的短信类型版本
                ArrayList<ConfigInfo> configs = new ArrayList<ConfigInfo>();
                ConfigInfo info = new ConfigInfo();
                // 短信邀请文案
                info.setConfigType(Constants.INVITE_CONFIGURATION_SMS_TYPE);
                info.setUpdateTime(CommonPreferences
                        .getInviteConfigLastTimeUpdate(Constants.INVITE_CONFIGURATION_SMS_TYPE));
                configs.add(info);
                // 语音邀请文案
                info = new ConfigInfo();
                info.setConfigType(Constants.INVITE_CONFIGURATION_VOICE_TYPE);
                info.setUpdateTime(CommonPreferences
                        .getInviteConfigLastTimeUpdate(Constants.INVITE_CONFIGURATION_VOICE_TYPE));
                configs.add(info);
                NetGetConfigurationResponse response = commonNetModule.getConfiguration(configs);
                if (null != response && response.isSuccess() && response.isHasUpdate()) {
                    ArrayList<ConfigInfo> tConfigs = response.getConfigInfo();
                    for (ConfigInfo ci : tConfigs) {
                        CommonPreferences.saveInviteConfigLastTimeUpdate(ci.getConfigType(),
                                ci.getUpdateTime());
                        CommonPreferences.saveInviteConfigContent(ci.getConfigType(),
                                ci.getConfigContent());
                    }
                }
            }
        });
    }

}
