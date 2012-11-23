
package android.skymobi.messenger.network.module;

import java.util.ArrayList;

import android.skymobi.app.net.event.ISXListener;
import android.skymobi.messenger.utils.Constants;

import com.skymobi.android.sx.codec.beans.clientbean.NetBindResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetForgetPwdResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetGetConfigurationResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetLoginResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetOnlineStatusResponse;
import com.skymobi.android.sx.codec.beans.clientbean.NetResponse;
import com.skymobi.android.sx.codec.beans.common.ConfigInfo;

/**
 * @ClassName: CommonModule
 * @author Sean.Xie
 * @date 2012-3-2 上午11:11:14
 */
public class CommonNetModule extends BaseNetModule {

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param netClient.getBiz()
     */
    public CommonNetModule(ISXListener netClient) {
        super(netClient);
    }

    /**
     * 检查绑定
     * 
     * @return
     */
    public synchronized NetBindResponse getBind() {
        return netClient.getBiz().getBind();
    }

/*    *//**
     * 注册
     * 
     * @return
     *//*
    public synchronized NetRegResponse register() {
        return netClient.getBiz().beforeCheckReg();
    }

    *//**
     * 注册
     * 
     * @param username
     * @param pwd
     * @return
     *//*
    public synchronized NetRegResponse register(String username, String pwd) {
        return netClient.getBiz().register(username, pwd);
    }*/

    /**
     * 登录
     * 
     * @param name
     * @param pwd
     * @return
     */
    public synchronized NetLoginResponse login(String name, String pwd) {
        return netClient.getBiz().login(name, pwd);
    }

    /**
     * 登陆
     * 
     * @param username
     * @param password
     */
    public synchronized NetLoginResponse login(String username, byte[] password) {
        return netClient.getBiz().login(username, password);
    }

    /**
     * 绑定手机
     * 
     * @return
     */
    public synchronized NetBindResponse bindPhone() {
        return netClient.getBiz().bind();
    }

    /**
     * 设置用户昵称
     * 
     * @param skyid
     * @param token
     * @param nickname
     * @return
     */
    public synchronized NetResponse setNickname(int skyid, String token, String nickname) {
        return netClient.getBiz().setUserNickName(skyid, token, nickname);
    }

    /**
     * 找回密码
     * 
     * @param username
     * @param newpwd
     * @return
     */
    public synchronized NetForgetPwdResponse forgetPwd(String username, String newpwd) {
        return netClient.getBiz().forgetPwd(username, newpwd);
    }

    /**
     * 获取离线消息推送
     */
    public synchronized void offLineMsgPushConfirm() {
        netClient.getBiz().offLineMsgPushConfirm(Constants.APPID);
    }
    
    /**
     * 获取服务端邀请信息配置
     * @param configs
     * @return
     */
    public synchronized NetGetConfigurationResponse getConfiguration(ArrayList<ConfigInfo> configs){
        return netClient.getBiz().getConfiguration(configs);
    }

    /**
     * 获取用户在线状态
     * 
     * @param destSkyids 目标用户的skid，多个skyid之间用,(英语键盘的逗号)分割
     * @return
     */
    public synchronized NetOnlineStatusResponse getOnlineStatus(String skyIDs) {
        return netClient.getBiz().getOnlineStatus(skyIDs);
    }
}
