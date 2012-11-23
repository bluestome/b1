
package android.skymobi.messenger.ui.handler.event;

public class EventMsg extends BaseMsg {

    private EventMsg() {
    }

    /** -------------------------注册消息----------------------------- */
    public final static int EVENT_REGISTER_ING = 0; // 开始注册
    public final static int EVENT_REGISTER_FAIL = 1; // 注册失败
    public final static int EVENT_REGISTER_SUCCESS = 2; // 注册成功
    public final static int EVENT_REGISTER_IS_ACTIVATED = 3; // 手机被激活过的

    /** ------------------------激活引导页----------------------------------- */
    public final static int EVENT_ACTIVATE_GUIDE_SUCCESS = 100; // 成功
    public final static int EVENT_ACTIVATE_GUIDE_FAIL = 101; // 失败

    /** ------------------------登录----------------------------------- */
    public final static int EVENT_LOGIN_SUCCESS = 200; // 成功
    public final static int EVENT_LOGIN_FAIL = 201; // 失败
    public final static int EVENT_LOGIN_LOGINING = 202;

    /** ------------------------修改用户资料----------------------------------- */
    public final static int EVENT_SET_USERINFO_SUCCESS = 300; // 成功
    public final static int EVENT_SET_USERINFO_FAIL = 301; // 失败

    /** -----------------------举报消息----------------------------- */
    public final static int EVENT_INFORM_SENDING = 400; // 正在发送
    public final static int EVENT_INFORM_SUCCESS = 401; // 发送成功
    public final static int EVENT_INFORM_FAILED = 402;// 发送失败
    // public final static int EVENT_INFORM_NOTLOGIN = 403; // 未登录

}
