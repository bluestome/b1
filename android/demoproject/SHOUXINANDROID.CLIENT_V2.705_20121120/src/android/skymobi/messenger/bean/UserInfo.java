
package android.skymobi.messenger.bean;

import java.io.Serializable;

/**
 * @ClassName: UserInfo
 * @Description: 用户激活后返回给用户的基本信息
 * @author Michael.Pan
 * @date 2012-2-8 下午03:16:56
 */
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public UserInfo() {
    }

    public int msgCode; // 其含义定义在类CoreServiceMSG
    public String name; // 用户名（mp****）
    public String pwd; // 明文密码
    public String nickname; // 昵称
    public int skyid;
    public String token; // 授权令牌，用户注销
    public byte[] encryptPasswd; // 用于自动登录的加密密码
    public String resultHint;
    public boolean isFindPassword = false; // 区分是否到修改密码界面

    public String to; // 发送短信的通道号
    public String message; // 发送短信的内容
}
