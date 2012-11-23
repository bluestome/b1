
package android.skymobi.messenger.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.bean.UserInfo;
import android.skymobi.messenger.database.dao.DaoFactory;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;

/**
 * @ClassName: CommonPreferences
 * @Description: 通用的配置文件类
 * @author Michael.pan
 * @date 2012-5-28 上午10:46:30
 */

public class CommonPreferences {

    private static final String TAG = CommonPreferences.class.getSimpleName();
    // 文件名
    private static final String MESSENGER_SHARED_PREFERENCES = "messenger_shared_preferences";

    // 介绍页阅读状态
    private static final String DESC_READED_STATUS = "desc_readed_status";

    // 登陆状态
    private static final String LOGIN_STATUS = "login_status";

    // 注销状态
    private static final String LOGOUT_STATUS = "logout_status";
    // IMSI
    private static final String IMSI = "imsi";
    // 用户名
    private static final String USERNAME = "username";
    // 密码
    private static final String PASSWORD = "password";
    // 登录使用的token
    private static final String TOKEN = "token";
    // sky_id
    private static final String SKY_ID = "sky_id";
    // 昵称
    private static final String NICKNAME = "nickname";
    // 最后自动登录时间
    private static final String LAST_LOGIN_TIME = "last_login_time";
    // 最后联系人同步时间
    private static final String LAST_SYNC_TIME = "last_sync_time";

    // 联系人同步
    private static final String CONTACTS_SYNCED = "contacts_synced";
    public static final int CONTACTS_SYNCED_TYPE_YES = 2;
    public static final int CONTACTS_SYNCED_TYPE_NOT = 0;
    public static final int CONTACTS_SYNCED_TYPE_DOING = 1;

    // 同步会话次数
    private static final String SYNC_THREADS_COUNT = "sync_threads_count";
    // 同步联系人次数
    private static final String SYNC_CONTACTS_COUNT = "sync_contacts_count";

    // 同步消息状态（true： 同步过 ， false：没有同步过）
    private static final String SYNC_MESSAGES = "sync_messages_status";

    // 获取推荐列表的时间 long类型表示
    private static final String SYNC_RECOMMEND_FRIENDS = "sync_recommend_friends";

    // 最后一次同步短语的时间（long表示）
    private static final String SYNC_RECOMMEND_MSGS = "sync_recommend_msgs";

    // 推荐短信类型的版本号
    private static final String SYNC_RECOMMEND_MSG_TYPE_VERSION = "sync_recommend_msg_type_version";

    // 推荐短信列表的版本号
    private static final String SYNC_RECOMMEND_MSG_LIST_VERSION = "sync_recommend_msg_list_version";

    // 最后一次触发服务器端进行推荐好友计算的时间
    private static final String RECALC_FIND_FRIENDS = "recalc_find_friends";

    // 最后一次同步联系人的时间
    private static final String CONTACT_LAST_UPDATETIME = "contact_last_updatetime";

    // 最后一次邀请短信的更新时间
    private static final String INVITE_CONFIGURATION_LAST_UPDATETIME = "invite_configuration_last_updatetime";

    // 邀请短信的内容
    private static final String INVITE_CONFIGURATION_CONTENT = "invite_configuration_content";

    // 下次检查的时间间隔
    private static final String CHECK_INTERVAL = "check_Interval";
    // 距离下次更新的次数
    private static final String CHECK_AFTERTIMES = "check_AfterTimes";
    private static final String LAST_DOWNLOADTIME = "last_downloadtime";
    private static final String LOGIN_TIMES = "login_times";

    private static final String FIND_PASSWORD = "find_password";
    private static final String FIND_PASSWORD_SUCCESS = "find_password_success";
    private static final String FIND_PASSWORD_SENDSMS_TIME = "find_password_sendsms_time";
    private static final String FIND_PASSWORD_RESULT = "find_password_result";

    private static final String CHANGE_BIND_SENDSMS_TIME = "change_bind_sendsms_time";

    // 下载参数
    private static final String APP_VERION = "app_version";
    private static final String APP_FILELEN = "app_filelen";
    private static final String IS_NEW = "is_new";
    private static final String FILE_MD5 = "file_md5";
    private static final String IS_FORCE = "is_force";
    private static final String IS_DOWNLOADING = "is_downloading";

    private static final String IS_FIRST_ADD_BLACK = "is_first_add_black";
    private static final String IS_CHECKED_NEW_FEATURE_RESTORE = "is_checked_new_feature_restore";

    // 注册标识
    private static final String REGISTER_FLAG = "register_flag";

    // 发送激活短信的通道号
    private static final String ACTIVITE_SMS_SEND_TO = "activite_sms_send_to";
    // 发送激活短信的内容
    private static final String ACTIVITE_SMS_SEND_CONTENT = "activite_sms_send_CONTENT";
    // 桌面快捷方式
    private static final String SHORTCUT_SETTING = "shortcut_setting";
    private static final String LAST_LOGIN_NAME = "last_login_name";

    private static final String LAST_DESCVERSION = "last_descversion";
    private static final String FIRST_CHAT_GUIDE = "first_chat_guide";
    private static final String FIRST_CONTACTS_GUIDE = "first_contacts_guide";
    private static final String CONTACTS_GUIDE_VERSION = "contacts_guide_version";
    private static final String FIRST_CONTACTS_GUIDE_VERSION = "first_contacts_guide_version";

    // LCS 最后一次发送时间
    private static final String LCS_LAST_SEND_TIME = "lcs_last_send_time";
    // 快聊是否使用过
    private static final String FASTCHAT_USED = "fastchat_used";
    private static final String FASTCHAT_SENDED = "fastchat_sended";

    /**
     * 设置是否有登陆过行为的状态
     * 
     * @param context
     * @param status
     */
    public static void setLoginedStatus(boolean status) {
        saveData(MESSENGER_SHARED_PREFERENCES, LOGIN_STATUS, status);
    }

    /**
     * 获取是否有登陆过行为的状态
     * 
     * @param context
     * @return
     */
    public static boolean getLoginedStatus() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, LOGIN_STATUS, false);
    }

    /**
     * 保存IMSI
     * 
     * @param imsi
     */
    public static void setIMSI(String imsi) {
        saveData(MESSENGER_SHARED_PREFERENCES, IMSI, imsi);
    }

    /**
     * 获取IMSI
     * 
     * @return
     */
    public static String getIMSI() {
        return (String) getData(MESSENGER_SHARED_PREFERENCES, IMSI, "");
    }

    /**
     * 设置注册标识
     * 
     * @param isRegister
     */
    public static void setRegisterFlag(boolean isRegister) {
        saveData(MESSENGER_SHARED_PREFERENCES, REGISTER_FLAG, isRegister);
    }

    /**
     * 获取注册标识
     * 
     * @return
     */
    public static boolean getRegisterFlag() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, REGISTER_FLAG, false);
    }

    /**
     * 设置激活短信通道号
     * 
     * @param to
     */
    public static void setActiviteSMSTo(String to) {
        saveData(MESSENGER_SHARED_PREFERENCES, ACTIVITE_SMS_SEND_TO, to);
    }

    public static String getActiviteSMSTo() {
        return (String) getData(MESSENGER_SHARED_PREFERENCES, ACTIVITE_SMS_SEND_TO, null);
    }

    /**
     * 设置激活短信的内容
     * 
     * @param content
     */
    public static void setActiviteSMSContent(String content) {
        saveData(MESSENGER_SHARED_PREFERENCES, ACTIVITE_SMS_SEND_CONTENT, content);
    }

    public static String getActiviteSMSContent() {
        return (String) getData(MESSENGER_SHARED_PREFERENCES, ACTIVITE_SMS_SEND_CONTENT, null);
    }

    /**
     * 保存用户信息
     * 
     * @param info
     */
    public static void setUserInfo(UserInfo info) {
        saveData(MESSENGER_SHARED_PREFERENCES, USERNAME, info.name);
        if (info.encryptPasswd != null)
            saveData(MESSENGER_SHARED_PREFERENCES, PASSWORD, Base64.encode(info.encryptPasswd));
        saveData(MESSENGER_SHARED_PREFERENCES, TOKEN, info.token);
        saveData(MESSENGER_SHARED_PREFERENCES, SKY_ID, info.skyid);
        saveData(MESSENGER_SHARED_PREFERENCES, NICKNAME, info.nickname);
    }

    /**
     * 设置最后自动登录时间
     * 
     * @param time
     */
    public static void setLastAutoLoginTime(long time) {
        saveData(MESSENGER_SHARED_PREFERENCES, LAST_LOGIN_TIME, time);
    }

    /**
     * 获取最后自动登录时间
     * 
     * @return
     */
    public static long getLastAutoLoginTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, LAST_LOGIN_TIME, 0l);
    }

    /**
     * 设置最后联系人同步时间
     * 
     * @param time
     */
    public static void setLastSyncTime(long time) {
        saveData(MESSENGER_SHARED_PREFERENCES, LAST_SYNC_TIME, time);
    }

    /**
     * 获取最后联系人同步时间
     * 
     * @return
     */
    public static long getLastSyncTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, LAST_SYNC_TIME, 0l);
    }

    /**
     * 获取用户信息
     * 
     * @return
     */
    public static UserInfo getUserInfo() {
        UserInfo info = new UserInfo();
        try {
            info.name = (String) getData(MESSENGER_SHARED_PREFERENCES, USERNAME, "");
            info.encryptPasswd = Base64.decode((String) getData(MESSENGER_SHARED_PREFERENCES,
                    PASSWORD, ""));
            if (TextUtils.isEmpty(info.name)
                    || (info.encryptPasswd == null || info.encryptPasswd.length == 0)) {
                return null;
            }
            info.token = (String) getData(MESSENGER_SHARED_PREFERENCES, TOKEN, "");
            info.skyid = (Integer) getData(MESSENGER_SHARED_PREFERENCES, SKY_ID, -1);
            info.nickname = (String) getData(MESSENGER_SHARED_PREFERENCES, NICKNAME, "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return info;
    }

    /**
     * 设置联系人同步状态
     * 
     * @param status
     */
    public static void setContactsSyncedStatus(int status) {
        saveData(MESSENGER_SHARED_PREFERENCES, CONTACTS_SYNCED, status);
    }

    public static int getContactsSyncedStatus() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, CONTACTS_SYNCED,
                CONTACTS_SYNCED_TYPE_NOT);
    }

    /**
     * 设置当前是否在注销状态
     * 
     * @param context
     * @param status
     */
    public static void setLogoutedStatus(boolean status) {
        saveData(MESSENGER_SHARED_PREFERENCES, LOGOUT_STATUS, status);
    }

    /**
     * 获取当前是否在注销状态
     * 
     * @param context
     * @return
     */
    public static boolean getLogoutedStatus() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, LOGOUT_STATUS, true);
    }

    /**
     * 获取同步会话次数
     * 
     * @return
     */
    public static int getSyncThreadsCount() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, SYNC_THREADS_COUNT, 0);
    }

    /**
     * 保存同步会话次数
     * 
     * @param count
     */
    public static void saveSyncThreadsCount(int count) {
        saveData(MESSENGER_SHARED_PREFERENCES, SYNC_THREADS_COUNT, count);
    }

    /**
     * 获取同步联系人次数
     * 
     * @return
     */
    public static int getSyncContactsCount() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, SYNC_CONTACTS_COUNT, 0);
    }

    /**
     * 保存同步联系人次数
     * 
     * @param count
     */
    public static void saveSyncContactsCount(int count) {
        saveData(MESSENGER_SHARED_PREFERENCES, SYNC_CONTACTS_COUNT, count);
    }

    /**
     * 保存同步消息状态
     * 
     * @param context
     * @param status
     */
    public static void saveSyncMessagesStatus(boolean status) {
        saveData(MESSENGER_SHARED_PREFERENCES, SYNC_MESSAGES, status);
    }

    /**
     * 获取同步消息状态
     * 
     * @param context
     * @return
     */
    public static boolean getSyncMessagesStatus() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, SYNC_MESSAGES, false);
    }

    /**
     * 保存推荐的时间
     * 
     * @param time
     */
    public static void saveSyncRecommendFriendsTime(long time) {
        saveData(MESSENGER_SHARED_PREFERENCES, SYNC_RECOMMEND_FRIENDS, time);
    }

    /**
     * 获取推荐的时间
     * 
     * @return
     */
    public static long getSyncRecommendFriendsTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, SYNC_RECOMMEND_FRIENDS, 0L);
    }

    /**
     * 保存推荐的时间
     * 
     * @param time
     */
    public static void saveSyncRecommendMsgsTime(long time) {
        saveData(MESSENGER_SHARED_PREFERENCES, SYNC_RECOMMEND_MSGS, time);
    }

    /**
     * 获取推荐的时间
     * 
     * @return
     */
    public static long getSyncRecommendMsgsTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, SYNC_RECOMMEND_MSGS, -1L);
    }

    /**
     * 保存推荐短信类型的版本号
     * 
     * @param time
     */
    public static void saveSyncRecommendMsgTypeTime(long time) {
        saveData(MESSENGER_SHARED_PREFERENCES, SYNC_RECOMMEND_MSG_TYPE_VERSION, time);
    }

    /**
     * 获取推荐短信类型的版本号
     * 
     * @return
     */
    public static long getSyncRecommendMsgTypeTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, SYNC_RECOMMEND_MSG_TYPE_VERSION, 0L);
    }

    /**
     * 保存推荐短信类型下列表的版本号
     * 
     * @param version 版本号
     * @param msgType 消息类型
     */
    public static void saveSyncRecommendMsgListVersion(long version, int msgType) {
        saveData(MESSENGER_SHARED_PREFERENCES, SYNC_RECOMMEND_MSG_LIST_VERSION + "_" + msgType,
                version);
    }

    /**
     * 根据短信类型获取短信列表版本号
     * 
     * @param msgType 消息类型
     * @return
     */
    public static long getSyncRecommendMsgListVersion(int msgType) {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, SYNC_RECOMMEND_MSG_LIST_VERSION + "_"
                + msgType, 0L);
    }

    /**
     * 保存最后一次触发服务器端计算推荐好友的时间
     * 
     * @param time
     */
    public static void setRecalcFindFriendsTime(long time) {
        saveData(MESSENGER_SHARED_PREFERENCES, RECALC_FIND_FRIENDS, time);
    }

    /**
     * 获取最后一次触发服务器端计算推荐好友的时间
     * 
     * @return
     */
    public static long getRecalcFindFriendsTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, RECALC_FIND_FRIENDS, 0L);
    }

    /**
     * @param lastUpdateTime
     */
    public static void saveContactsLastTimeUpdate(long lastUpdateTime) {
        SLog.d(TAG, "!saveContactsLastTimeUpdate !!!!  " + lastUpdateTime);
        saveData(MESSENGER_SHARED_PREFERENCES, CONTACT_LAST_UPDATETIME, lastUpdateTime);
        DaoFactory.getInstance(MainApp.i()).getUsersDAO()
                .saveContactLastUpdateTime(lastUpdateTime);
    }

    public static long getContactsLastTimeUpdate() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, CONTACT_LAST_UPDATETIME, 0L);
    }

    public static void saveCheckInterval(int checkInterval) {
        saveData(MESSENGER_SHARED_PREFERENCES, CHECK_INTERVAL, checkInterval);
    }

    public static int getCheckInterval() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, CHECK_INTERVAL, -1);
    }

    public static void saveCheckAfterTimes(int checkAfterTimes) {
        saveData(MESSENGER_SHARED_PREFERENCES, CHECK_AFTERTIMES, checkAfterTimes);
    }

    public static int getCheckAfterTimes() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, CHECK_AFTERTIMES, -1);
    }

    public static void saveFindPassword(long findPassword) {
        saveData(MESSENGER_SHARED_PREFERENCES, FIND_PASSWORD, findPassword);
    }

    public static long getFindPassword() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, FIND_PASSWORD, -1L);
    }

    public static void saveFindPasswordSuccess(boolean findPasswordSuccess) {
        saveData(MESSENGER_SHARED_PREFERENCES, FIND_PASSWORD_SUCCESS, findPasswordSuccess);
    }

    public static boolean getFindPasswordSuccess() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, FIND_PASSWORD_SUCCESS, false);
    }

    public static void saveFindPasswordResult(boolean findPasswordResult) {
        saveData(MESSENGER_SHARED_PREFERENCES, FIND_PASSWORD_RESULT, findPasswordResult);
    }

    public static boolean getFindPasswordResult() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, FIND_PASSWORD_RESULT, false);
    }

    public static void saveFindPasswordSendSMSTime(long sendSmsTime) {
        saveData(MESSENGER_SHARED_PREFERENCES, FIND_PASSWORD_SENDSMS_TIME, sendSmsTime);
    }

    public static long getFindPasswordSendSMSTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, FIND_PASSWORD_SENDSMS_TIME, -1L);
    }

    public static void saveChangeBindSendSMSTime(long sendSmsTime) {
        saveData(MESSENGER_SHARED_PREFERENCES, CHANGE_BIND_SENDSMS_TIME, sendSmsTime);
    }

    public static long getChangeBindSendSMSTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, CHANGE_BIND_SENDSMS_TIME, -1L);
    }

    public static void saveAppVerion(String appVersion) {
        saveData(MESSENGER_SHARED_PREFERENCES, APP_VERION, appVersion);
    }

    public static String getAppVerion() {
        return (String) getData(MESSENGER_SHARED_PREFERENCES, APP_VERION, "");
    }

    public static void saveAppFilelen(int appFilelen) {
        saveData(MESSENGER_SHARED_PREFERENCES, APP_FILELEN, appFilelen);
    }

    public static int getAppFilelen() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, APP_FILELEN, 0);
    }

    public static void saveIsNew(boolean isNew) {
        saveData(MESSENGER_SHARED_PREFERENCES, IS_NEW, isNew);
    }

    public static boolean getIsNew() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, IS_NEW, false);
    }

    public static void saveFileMd5(String fileMd5) {
        saveData(MESSENGER_SHARED_PREFERENCES, FILE_MD5, fileMd5);
    }

    public static String getFileMd5() {
        return (String) getData(MESSENGER_SHARED_PREFERENCES, FILE_MD5, "");
    }

    public static void saveIsForce(boolean isForce) {
        saveData(MESSENGER_SHARED_PREFERENCES, IS_FORCE, isForce);
    }

    public static boolean getIsForce() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, IS_FORCE, false);
    }

    // public static void saveIsDownloading(boolean isDownloading) {
    // saveData(MESSENGER_SHARED_PREFERENCES, IS_DOWNLOADING, isDownloading);
    // }
    //
    // public static boolean getIsDownloading() {
    // return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, IS_DOWNLOADING,
    // false);
    // }

    public static void saveLastDownloadTime(long lastDownloadTime) {
        saveData(MESSENGER_SHARED_PREFERENCES, LAST_DOWNLOADTIME, lastDownloadTime);
    }

    public static long getLastDownloadTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, LAST_DOWNLOADTIME, -1L);
    }

    public static void saveLoginTimes(int loginTimes) {
        saveData(MESSENGER_SHARED_PREFERENCES, LOGIN_TIMES, loginTimes);
    }

    public static int getLoginTimes() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, LOGIN_TIMES, 0);
    }

    public static void saveIsFirstAddBlack(boolean isFirst) {
        saveData(MESSENGER_SHARED_PREFERENCES, IS_FIRST_ADD_BLACK, isFirst);
    }

    public static boolean getIsFirstAddBlack() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, IS_FIRST_ADD_BLACK, true);
    }

    public static void saveIsCheckedNewFeatureRestore(boolean isChecked) {
        saveData(MESSENGER_SHARED_PREFERENCES, IS_CHECKED_NEW_FEATURE_RESTORE, isChecked);
    }

    public static boolean getIsCheckedNewFeatureRestore() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, IS_CHECKED_NEW_FEATURE_RESTORE,
                false);
    }

    // 获取指定类型的邀请短信最后更新时间
    public static long getInviteConfigLastTimeUpdate(byte type) {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, INVITE_CONFIGURATION_LAST_UPDATETIME
                + "_" + type, 0L);
    }

    /**
     * 保存指定类型的邀请短信最后更新时间
     * 
     * @param lastUpdateTime
     */
    public static void saveInviteConfigLastTimeUpdate(byte type, long lastUpdateTime) {
        System.err.println("!saveInviteConfigLastTimeUpdate !!!!  " + lastUpdateTime);
        saveData(MESSENGER_SHARED_PREFERENCES, INVITE_CONFIGURATION_LAST_UPDATETIME + "_" + type,
                lastUpdateTime);
    }

    // 获取指定类型的邀请短信内容
    public static String getInviteConfigContent(byte type) {
        return (String) getData(MESSENGER_SHARED_PREFERENCES, INVITE_CONFIGURATION_CONTENT + "_"
                + type, null);
    }

    /**
     * 保存指定类型的邀请短信内容
     * 
     * @param lastUpdateTime
     */
    public static void saveInviteConfigContent(byte type, String content) {
        System.err.println("!saveInviteConfigContent !!!!  " + content);
        saveData(MESSENGER_SHARED_PREFERENCES, INVITE_CONFIGURATION_CONTENT + "_" + type, content);
    }

    /**
     * 保存数据
     * 
     * @param context
     * @param fileName
     * @param key
     * @param value
     */
    public static void saveData(String fileName, String key, Object value) {
        SharedPreferences sp = MainApp.i().getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else {
            if (value == null) {
                editor.putString(key, "");
            } else {
                editor.putString(key, String.valueOf(value));
            }
        }
        editor.commit();
    }

    /**
     * 取值
     * 
     * @param context
     * @param fileName
     * @param key
     * @param defValue
     * @return
     */
    public static Object getData(String fileName, String key, Object defValue) {
        SharedPreferences sp = MainApp.i().getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        if (defValue instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defValue);
        } else if (defValue instanceof Integer) {
            return sp.getInt(key, (Integer) defValue);
        } else if (defValue instanceof Long) {
            return sp.getLong(key, (Long) defValue);
        } else if (defValue instanceof Float) {
            return sp.getFloat(key, (Float) defValue);
        } else {
            if (defValue == null) {
                return sp.getString(key, "");
            }
            return sp.getString(key, String.valueOf(defValue));
        }
    }

    /**
     * 清空
     * 
     * @param fileName
     */
    public static void clear(String fileName) {
        SharedPreferences sp = MainApp.i().getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 保存登陆信息
     * 
     * @param info
     */
    public static void saveLoginStatus(UserInfo info) {
        CommonPreferences.setLoginedStatus(true);
        CommonPreferences.setLogoutedStatus(false);
        CommonPreferences.setIMSI(MainApp.i().getDeviceInfo().imsi);
        CommonPreferences.setUserInfo(info);
    }

    /**
     * @return
     */
    public static boolean getShortCutSetting() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, SHORTCUT_SETTING, false);
    }

    public static void setShortCutSetting(boolean flag) {
        saveData(MESSENGER_SHARED_PREFERENCES, SHORTCUT_SETTING, flag);
    }

    /**
     * @return the lastLoginName
     */
    public static String getLastLoginName() {
        return (String) getData(MESSENGER_SHARED_PREFERENCES, LAST_LOGIN_NAME, "");
    }

    public static void setLastLoginName(String lastLoginName) {
        saveData(MESSENGER_SHARED_PREFERENCES, LAST_LOGIN_NAME, lastLoginName);
    }

    // anson.yang 20120920
    public static int getLastDescVerion() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, LAST_DESCVERSION, -1);
    }

    public static void setLastDescVerion(int lastDescVerion) {
        saveData(MESSENGER_SHARED_PREFERENCES, LAST_DESCVERSION, lastDescVerion);
    }

    public static boolean getFirstChatGuide() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, FIRST_CHAT_GUIDE, true);
    }

    public static void setFirstChatGuide(boolean firstChatGuide) {
        saveData(MESSENGER_SHARED_PREFERENCES, FIRST_CHAT_GUIDE, firstChatGuide);
    }

    public static boolean getFirstContactsGuide() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, FIRST_CONTACTS_GUIDE, true);
    }

    public static void setFirstContactsGuide(boolean firstContactsGuide) {
        saveData(MESSENGER_SHARED_PREFERENCES, FIRST_CONTACTS_GUIDE, firstContactsGuide);
    }

    public static int getContactsGuideVersion() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, CONTACTS_GUIDE_VERSION, -1);
    }

    public static void setContactsGuideVersion(int contactsGuideVersion) {
        saveData(MESSENGER_SHARED_PREFERENCES, CONTACTS_GUIDE_VERSION, contactsGuideVersion);
    }

    public static int getFirstChatGuideVersion() {
        return (Integer) getData(MESSENGER_SHARED_PREFERENCES, FIRST_CONTACTS_GUIDE_VERSION, -1);
    }

    public static void setFirstChatGuideVersion(int firstChatGuideVersion) {
        saveData(MESSENGER_SHARED_PREFERENCES, FIRST_CONTACTS_GUIDE_VERSION, firstChatGuideVersion);
    }

    /**
     * 设置LCS 最后一次发送时间 *
     * 
     * @param lastSendTime
     */
    public static void saveLcsLastSendTime(long lastSendTime) {
        saveData(MESSENGER_SHARED_PREFERENCES, LCS_LAST_SEND_TIME, lastSendTime);
    }

    /**
     * 获取最后一次LCS数据发送时间
     * 
     * @return
     */
    public static long getLcsLastSendTime() {
        return (Long) getData(MESSENGER_SHARED_PREFERENCES, LCS_LAST_SEND_TIME, 0L);
    }

    /**
     * 保存快聊使用情况
     * 
     * @param isUsed
     */
    public static void saveFastChatUsed(boolean isUsed) {
        saveData(MESSENGER_SHARED_PREFERENCES, FASTCHAT_USED, isUsed);
    }

    /**
     * 是否使用过快聊
     * 
     * @return
     */
    public static boolean isFastChatUsed() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, FASTCHAT_USED, false);
    }

    /**
     * 保存发送快聊消息状态
     * 
     * @param isSend
     */
    public static void saveFastChatSended(boolean isSend) {
        saveData(MESSENGER_SHARED_PREFERENCES, FASTCHAT_SENDED, isSend);
    }

    /**
     * 是否发生过快聊消息
     * 
     * @return
     */
    public static boolean isFastChatSended() {
        return (Boolean) getData(MESSENGER_SHARED_PREFERENCES, FASTCHAT_SENDED, false);
    }

    /**
     * 清空密码
     */
    public static void clearPassword() {
        saveData(MESSENGER_SHARED_PREFERENCES, PASSWORD, "");
    }
}
