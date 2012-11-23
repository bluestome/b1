
package android.skymobi.messenger.service;

/**
 * @ClassName: CoreServiceMSG
 * @Description: service 消息类型的定义
 * @author Michael.Pan
 * @date 2012-2-8 上午10:58:46
 */
public class CoreServiceMSG {
    /** 全局消息类型定义 **/
    // Global Message Code Define
    public final static int MSG_GLOBAL_BASE = 0x1000;
    // 失败
    public final static int MSG_FAILED = MSG_GLOBAL_BASE + 500;
    // 成功
    public final static int MSG_SUCCESS = MSG_GLOBAL_BASE + 501;
    // 网络错误
    public final static int MSG_NET_ERROR = MSG_GLOBAL_BASE + 502;
    // 手机网络连接状态改变
    public final static int MSG_NET_STATUE_CHANGE = MSG_GLOBAL_BASE + 503;
    // 手信帐号在异地登录后,被踢下线
    public final static int MSG_TICKET_OUT = MSG_GLOBAL_BASE + 504;

    /** 同步消息类型定义 **/
    // Sync Message Code Define
    public final static int MSG_SYNC_BASE = 0x2000;
    // 会话同步开始
    public final static int MSG_THREADS_SYNC_BEGIN = MSG_SYNC_BASE + 1;
    // 同步消息的进度
    public final static int MSG_THREADS_SYNC_PROGRESS = MSG_SYNC_BASE + 2;
    // 会话同步结束
    public final static int MSG_THREADS_SYNC_END = MSG_SYNC_BASE + 3;
    // 消息同步开始
    public final static int MSG_MESSAGES_SYNC_BEGIN = MSG_SYNC_BASE + 4;
    // 消息同步结束
    public final static int MSG_MESSAGES_SYNC_END = MSG_SYNC_BASE + 5;
    // 同步消息的进度
    public final static int MSG_MESSAGES_SYNC_PROGRESS = MSG_SYNC_BASE + 6;
    // 批量删除会话
    public final static int MSG_THREADS_CLEARALL = MSG_SYNC_BASE + 7;
    public final static int MSG_THREADS_CLEARALL_BEGIN = MSG_SYNC_BASE + 8;
    public final static int MSG_THREADS_CLEARALL_END = MSG_SYNC_BASE + 9;
    // 删除会话结束(更新未读条数)
    public final static int MSG_THREADS_DELETE_END = MSG_SYNC_BASE + 10;

    /** 激活 **/
    public final static int MSG_REGISTER_BASE = 0x3000;
    public final static int MSG_REGISTER_ISBIND = MSG_REGISTER_BASE + 1;
    public final static int MSG_REGISTER_SUCESS = MSG_REGISTER_BASE + 2;
    public final static int MSG_BIND_SUCESS = MSG_REGISTER_BASE + 3;
    public final static int MSG_REBIND_SUCESS = MSG_REGISTER_BASE + 4;
    public final static int MSG_BIND_FAILURE = MSG_REGISTER_BASE + 5;
    public final static int MSG_CHECK_BIND = MSG_REGISTER_BASE + 6;

    /** 登陆 **/
    // Login Message Code Define
    public final static int MSG_LOGIN_BASE = 0x4000;
    public final static int MSG_LOGIN_SUCCESS = MSG_LOGIN_BASE + 100;
    public final static int MSG_LOGIN_FREEZE = MSG_LOGIN_BASE + 101;
    public final static int MSG_LOGIN_PASSWORD_ERROR = MSG_LOGIN_BASE + 102;
    public final static int MSG_LOGIN_USERNAME_NOTEXIST_IMSI_UNBIND = MSG_LOGIN_BASE + 103;
    public final static int MSG_LOGIN_USERNAME_NOTEXIST_IMSI_BIND = MSG_LOGIN_BASE + 104;

    public final static int MSG_LOGIN_USERNAME_BIND_IMSI_NOT_SAME = MSG_LOGIN_BASE + 105;
    public final static int MSG_LOGIN_USERNAME_UNBIND_IMSI_BIND = MSG_LOGIN_BASE + 106;
    public final static int MSG_LOGIN_USERNAME_UNBIND_IMSI_UNBIND = MSG_LOGIN_BASE + 107;
    public final static int MSG_LOGIN_USERNAME_BIND_IMSI_UNBIND = MSG_LOGIN_BASE + 108;

    public final static int MSG_LOGIN_USERNAME_NOT_MACTH = MSG_LOGIN_BASE + 109;
    public final static int MSG_LOGIN_PASSWORD_NOT_MACTH = MSG_LOGIN_BASE + 110;
    public final static int MSG_LOGIN_USERNAME_IS_PHONENUMBER = MSG_LOGIN_BASE + 111;

    public final static int MSG_LOGIN_ACCOUNT_ERROR = MSG_LOGIN_BASE + 112;
    public final static int MSG_LOGIN_NET_ERROR = MSG_LOGIN_BASE + 113;
    // 用户名为手机号码，且未注册
    public final static int MSG_LOGIN_USERNAME_UNBIND_IS_PHONE = MSG_LOGIN_BASE + 114;
    // 用户名不存在
    public final static int MSG_LOGIN_USERNAME_NOTEXIST = MSG_LOGIN_BASE + 115;

    // 下载
    public final static int MSG_LOGIN_DOWNLOAD = MSG_LOGIN_BASE + 200;
    public final static int MSG_LOGIN_FORCE_DOWNLOAD = MSG_LOGIN_BASE + 201;
    public final static int MSG_LOGIN_DOWNLOAD_ERROR = MSG_LOGIN_BASE + 202;
    // 检查更新
    public final static int MSG_LOGIN_CHECK_FORCE_UPDATE = MSG_LOGIN_BASE + 203;
    public final static int MSG_LOGIN_CHECK_UPDATE = MSG_LOGIN_BASE + 204;
    public final static int MSG_LOGIN_CHECK_NOUPDATE = MSG_LOGIN_BASE + 205;

    public final static int MSG_LOGIN_CHECK_ERROR = MSG_LOGIN_BASE + 206; // 检查失败
    public final static int MSG_LOGIN_CHECK_NET_ERROR = MSG_LOGIN_BASE + 300;
    public final static int MSG_LOGIN_CHECK_DOWNLOAD_ERROR = MSG_LOGIN_BASE + 301;
    public final static int MSG_LOGIN_FORCE_DOWNLOAD_ERROR = MSG_LOGIN_BASE + 302;
    public final static int MSG_LOGIN_DOWNLOAD_NET_ERROR = MSG_LOGIN_BASE + 303;
    public final static int MSG_LOGIN_DOWNLOAD_RETRY = MSG_LOGIN_BASE + 304;

    // 忘记密码
    public final static int MSG_FORGETPWD_NET_ERROR = MSG_LOGIN_BASE + 400;
    public final static int MSG_FORGETPWD_ERROR = MSG_LOGIN_BASE + 401;
    public final static int MSG_FORGETPWD_ISBOUND = MSG_LOGIN_BASE + 402;
    public final static int MSG_FORGETPWD_UNBOUND = MSG_LOGIN_BASE + 403;
    public final static int MSG_FORGETPWD_SEND_SMS_START = MSG_LOGIN_BASE + 404;
    public final static int MSG_SMSMSG_RECEIVE_COMMON = MSG_LOGIN_BASE + 405;
    // 新增接收网络层发送的消息
    public final static int MSG_SMSMSG_RECEIVE_COMMON_NET = MSG_LOGIN_BASE + 406;

    /** 消息 **/
    public final static int MSG_CHATMSG_BASE = 0xb000;
    // 文字聊天消息
    public final static int MSG_CHATMSG_TEXTMSG_SEND_BEGIN = MSG_CHATMSG_BASE + 1;
    public final static int MSG_CHATMSG_TEXTMSG_SEND_END = MSG_CHATMSG_BASE + 2;
    public final static int MSG_CHATMSG_TEXTMSG_RECEIVE = MSG_CHATMSG_BASE + 3;
    // 手信小助手消息
    public final static int MSG_CHATMSG_MARKETMSG_RECEIVE = MSG_CHATMSG_BASE + 4;
    public final static int MSG_CHATMSG_SYSTEMMSG_RECEIVE = MSG_CHATMSG_BASE + 5;
    public final static int MSG_CHATMSG_FRIENDSMSG_RECEIVE = MSG_CHATMSG_BASE + 6;
    // 获取推荐短信
    public final static int MSG_CHATMSG_GETRECOMMEND_BEGIN = MSG_CHATMSG_BASE + 7;
    public final static int MSG_CHATMSG_GETRECOMMEND_END = MSG_CHATMSG_BASE + 8;
    // 名片消息
    public final static int MSG_CHATMSG_CARDMSG_RECEIVE = MSG_CHATMSG_BASE + 9;
    public final static int MSG_CHATMSG_CARDMSG_SEND_BEGIN = MSG_CHATMSG_BASE + 10;
    public final static int MSG_CHATMSG_CARDMSG_SEND_END = MSG_CHATMSG_BASE + 11;
    // 语音消息
    public final static int MSG_CHATMSG_VOICEMSG_RECEIVE = MSG_CHATMSG_BASE + 12;
    public final static int MSG_CHATMSG_VOICEMSG_SEND_BEGIN = MSG_CHATMSG_BASE + 13;
    public final static int MSG_CHATMSG_VOICEMSG_SEND_END = MSG_CHATMSG_BASE + 14;
    // SMS短信
    public final static int MSG_CHATMSG_SMSMSG_RECEIVE = MSG_CHATMSG_BASE + 15;
    public final static int MSG_CHATMSG_SMSMSG_SEND_BEGIN = MSG_CHATMSG_BASE + 16;
    public final static int MSG_CHATMSG_SMSMSG_SEND_END = MSG_CHATMSG_BASE + 17;
    // 录音面板的音量更新
    public final static int MSG_CHATMSG_RECODE_SOUND_CHANGE = MSG_CHATMSG_BASE + 18;
    // 录音倒计时的时间更新
    public final static int MSG_CHATMSG_SECOND_CHANGE = MSG_CHATMSG_BASE + 19;
    // 流量统计消息类型
    public final static int MSG_TRAFFIC_NOTIFY_MSG = MSG_CHATMSG_BASE + 20;
    // 发送消息，这是一个通用的消息，不区分各种发送类型
    public final static int MSG_CHATMSG_SEND_BEGIN = MSG_CHATMSG_BASE + 21;
    public final static int MSG_CHATMSG_RESEND_BEGIN = MSG_CHATMSG_BASE + 22;

    /** 联系人 **/
    public final static int MSG_CONTACTS_BASE = 0xc000;
    /** 联系人本地同步开始 **/
    public final static int MSG_CONTACTS_SYNC_BEGIN = MSG_CONTACTS_BASE + 9;
    /** 联系人本地同步结束 **/
    public final static int MSG_CONTACTS_SYNC_END = MSG_CONTACTS_BASE + 10;
    /** 联系人同步进度 **/
    public final static int MSG_CONTACTS_SYNC_PROCESS = MSG_CONTACTS_BASE + 11;
    /** 联系人人云端同步并已返回 **/
    public final static int MSG_CONTACTS_ClOUD_SYNC_END = MSG_CONTACTS_BASE + 12;
    public final static int MSG_CONTACTS_ONLINE_STATUS = MSG_CONTACTS_BASE + 13;
    public final static int MSG_CONTACTS_ADD_BLACKLIST = MSG_CONTACTS_BASE + 14;
    public final static int MSG_CONTACTS_DELETE_CONTACT = MSG_CONTACTS_BASE + 15;
    public final static int MSG_CONTACTS_SYNC_STATUS_END = MSG_CONTACTS_BASE + 16;
    public final static int MSG_CONTACTS_ADD_STATUS_SUCCESS = MSG_CONTACTS_BASE + 17;// 添加联系人成功
    public final static int MSG_CONTACTS_SYNC_FAILED = MSG_CONTACTS_BASE + 18; // 同步联系人失败

    public final static int MSG_CONTACTS_DETAIL_SUCCESS = MSG_CONTACTS_BASE + 19;

    /** 编辑联系人 **/
    public final static int MSG_CONTACTS_EDIT_ADD_SUCCESS = MSG_CONTACTS_BASE + 20; // 联系人详情中添加联系人成功
    public final static int MSG_CONTACTS_EDIT_ADD_FAIL = MSG_CONTACTS_BASE + 21; // 联系人详情中添加联系人失败
    public final static int MSG_CONTACTS_EDIT_UPDATE_SUCCESS = MSG_CONTACTS_BASE + 22; // 编辑联系人后提交更新成功
    public final static int MSG_CONTACTS_EDIT_UPDATE_FAIL = MSG_CONTACTS_BASE + 23; // 编辑联系人后提交更新失败

    /** 黑名单 **/
    public final static int MSG_CONTACTS_BLACKLIST_ADD = MSG_CONTACTS_BASE + 160;
    public final static int MSG_CONTACTS_BLACKLIST_REMOVE = MSG_CONTACTS_BASE + 161;
    public final static int MSG_CONTACTS_IS_INCONTACT = MSG_CONTACTS_BASE + 170;
    public final static int MSG_CONTACTS_DELETE_CONTACT_BLACKLIST = MSG_CONTACTS_BASE + 171;

    /** 找朋友 **/
    public final static int MSG_FRIENDS_BASE = 0xd000;
    public final static int MSG_FRIENDS_GET_LIST = MSG_FRIENDS_BASE + 200;
    public final static int MSG_FRIENDS_NO_UPDATE = MSG_FRIENDS_BASE + 201;
    public final static int MSG_FRIENDS_GET_FROMDB_LIST = MSG_FRIENDS_BASE + 203;
    public final static int MSG_FRIENDS_NET_ERROR = MSG_FRIENDS_BASE + 300;
    public final static int MSG_FRIENDS_PARAM_ERROR = MSG_FRIENDS_BASE + 400;
    public final static int MSG_FRIENDS_FAILED = MSG_FRIENDS_BASE + 404;
    public final static int MSG_FRIENDS_DETAIL_SUCCESS = MSG_FRIENDS_BASE + 500;
    public final static int MSG_FRIENDS_DETAIL_FAIL = MSG_FRIENDS_BASE + 501;
    public final static int MSG_SEARCH_FRIEND_SUCCESS = MSG_FRIENDS_BASE + 600;
    public final static int MSG_SEARCH_FRIEND_FAIL = MSG_FRIENDS_BASE + 601;
    public final static int MSG_SEARCH_FRIEND_NOTFOUND = MSG_FRIENDS_BASE + 602;
    public final static int MSG_SEARCH_FRIEND_NET_ERROR = MSG_FRIENDS_BASE + 603;
    // 快聊消息的定义
    public final static int MSG_FASTCHAT_APPLY_BEGIN = MSG_FRIENDS_BASE + 700; // 发送匹配请求开始
    public final static int MSG_FASTCHAT_APPLY_SUCCESS = MSG_FRIENDS_BASE + 701; // 匹配成功，并且将保持对方的skyid
    public final static int MSG_FASTCHAT_APPLY_FAIL = MSG_FRIENDS_BASE + 702; // 匹配失败，没有匹配到人
    public final static int MSG_FASTCHAT_APPLY_REQ_FAIL = MSG_FRIENDS_BASE + 703; // 匹配的请求发送超时等

    public final static int MSG_FASTCHAT_LEAVE_BEGIN = MSG_FRIENDS_BASE + 703; // 离开快聊请求开始
    public final static int MSG_FASTCHAT_LEAVE_SUCCESS = MSG_FRIENDS_BASE + 704; // 离开快聊请求成功发出
    public final static int MSG_FASTCHAT_LEAVE_FAIL = MSG_FRIENDS_BASE + 705; // 离开快聊请求发送失败

    public final static int MSG_FASTCHAT_ALREADY_LEAVE = MSG_FRIENDS_BASE + 706; // 对方已经离开
    public final static int MSG_FASTCHAT_RECEIVEVOICE = MSG_FRIENDS_BASE + 707; // 收到对方的快聊语音
    public final static int MSG_FASTCHAT_SENDVOICE_BEGIN = MSG_FRIENDS_BASE + 708; // 发送快聊语音开始
    public final static int MSG_FASTCHAT_SENDVOICE_END = MSG_FRIENDS_BASE + 709; // 发送快聊语音结束
    public final static int MSG_FASTCHAT_RESENDVOICE_BEGIN = MSG_FRIENDS_BASE + 710; // 重新发送快聊语音开始
    public final static int MSG_FASTCHAT_RESENDVOICE_END = MSG_FRIENDS_BASE + 711; // 重新发送快聊语音结束

    /** 设置 **/
    public final static int MSG_SETTINGS_BASE = 0xe000;
    public final static int MSG_SETTINGS_LOGOUT_FAIL = MSG_SETTINGS_BASE + 1;
    public final static int MSG_SETTINGS_LOGOUT_SUCCESS = MSG_SETTINGS_BASE + 2;
    public final static int MSG_SETTINGS_SET_NICKNAME_FAIL = MSG_SETTINGS_BASE + 3;
    public final static int MSG_SETTINGS_SET_NICKNAME_SUCCESS = MSG_SETTINGS_BASE + 4;
    public final static int MSG_SETTINGS_SET_USERINFO_FAIL = MSG_SETTINGS_BASE + 5;
    public final static int MSG_SETTINGS_SET_USERINFO_SUCCESS = MSG_SETTINGS_BASE + 6;
    public final static int MSG_SETTINGS_MODIFYPWD_FAIL = MSG_SETTINGS_BASE + 7;
    public final static int MSG_SETTINGS_MODIFYPWD_SUCCESS = MSG_SETTINGS_BASE + 8;
    public final static int MSG_SETTINGS_FEEDBACK_FAIL = MSG_SETTINGS_BASE + 9;
    public final static int MSG_SETTINGS_FEEDBACK_SUCCESS = MSG_SETTINGS_BASE + 10;
    public final static int MSG_SETTINGS_SET_RECOMMEND_FAIL = MSG_SETTINGS_BASE + 11;
    public final static int MSG_SETTINGS_SET_RECOMMEND_SUCCESS = MSG_SETTINGS_BASE + 12;
    public final static int MSG_SETTINGS_GET_USERINFO_FAIL = MSG_SETTINGS_BASE + 13;
    public final static int MSG_SETTINGS_GET_USERINFO_SUCCESS = MSG_SETTINGS_BASE + 14;
    public final static int MSG_SETTINGS_UPLOAD_HEADPHOTO_FAIL = MSG_SETTINGS_BASE + 15;
    public final static int MSG_SETTINGS_UPLOAD_HEADPHOTO_SUCCESS = MSG_SETTINGS_BASE + 16;
    public final static int MSG_SETTINGS_DOWNLOAD_HEADPHOTO_FAIL = MSG_SETTINGS_BASE + 17;
    public final static int MSG_SETTINGS_DOWNLOAD_HEADPHOTO_SUCCESS = MSG_SETTINGS_BASE + 18;
    public final static int MSG_SETTINGS_GET_RECOMMEND_FAIL = MSG_SETTINGS_BASE + 19;
    public final static int MSG_SETTINGS_GET_RECOMMEND_SUCCESS = MSG_SETTINGS_BASE + 20;
    public final static int MSG_SETTINGS_UPDATE_APP = MSG_SETTINGS_BASE + 30;
    public final static int MSG_SETTINGS_RESERVE = MSG_SETTINGS_BASE + 80;
    public final static int MSG_SETTINGS_SET_USERINFO_ILLEGAL = MSG_SETTINGS_BASE + 81;
    // 获取恢复联系人消息
    public final static int MSG_SETTINGS_GET_RESTORECONTACTS_BEGIN = MSG_SETTINGS_BASE + 90;
    public final static int MSG_SETTINGS_GET_RESTORECONTACTS_END = MSG_SETTINGS_BASE + 91;
    public final static int MSG_SETTINGS_GET_RESTORECONTACTS_FAIL = MSG_SETTINGS_BASE + 92;
    // 恢复联系人消息
    public final static int MSG_SETTINGS_RESTORE_CONTACTS_BEGIN = MSG_SETTINGS_BASE + 93;
    public final static int MSG_SETTINGS_RESTORE_CONTACTS_END = MSG_SETTINGS_BASE + 94;
    public final static int MSG_SETTINGS_RESTORE_CONTACTS_FAIL = MSG_SETTINGS_BASE + 95;
    /** 查找附近的人 **/
    public final static int MSG_NEARUSER_BASE = 0xf000;
    public final static int MSG_NEARUSER_GET = MSG_NEARUSER_BASE + 1;
    public final static int MSG_NEARUSER_GET_ADD = MSG_NEARUSER_BASE + 2;
    // public final static int MSG_NEARUSER_GET_LOCATION_ERR =
    // MSG_NEARUSER_GET_NET_ERR + 1;
    // 语音重试消息
    public static final int MSG_VOICE_RETRY = 0xe000;
    public static final int MSG_VOICE_RETRY_SUCCESS = MSG_VOICE_RETRY + 1;
    public static final int MSG_VOICE_RETRY_FAILED = MSG_VOICE_RETRY_SUCCESS + 1;

}
