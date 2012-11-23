
package android.skymobi.messenger.utils;

import android.graphics.Color;
import android.os.Environment;
import android.text.format.DateUtils;

/**
 * @ClassName: Constants
 * @Description: 常量
 * @author Sean.Xie
 * @date 2012-2-16 下午4:05:26
 */
public class Constants {
    public static final String separator = ",";

    /** 终端信息 **/
    public static final int APPID = 2934;
    public static final int ILLEGA = -1;

    /** 手信文件缓存目录 **/
    public static final String SHOU_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/.shouxin/";
    /** .nomedia文件，为了不让系统扫描该目录下的多媒体文件 **/
    public static final String NO_MEDIA = ".nomedia";
    /** 语音目录 **/
    public static final String SOUND_PATH = SHOU_PATH + "sound/";
    /** 头像目录 **/
    public static final String HEAD_PATH = SHOU_PATH + "head/";
    public static final String LARGE_HEAD_PATH = HEAD_PATH + "large/";
    /** 图片目录 **/
    public static final String PIC_PATH = SHOU_PATH + "pic/";
    /** 下载apk目录 **/
    public static final String APK_PATH = SHOU_PATH + "apk/";
    /** crash log文件目录 **/
    public static final String CRASH_PATH = SHOU_PATH + "crash/";
    /** 普通日志目录 **/
    public static final String NORMAL_LOG_PATH = SHOU_PATH + "log/";

    public static final String PRE_APK_NAME = "shouxin";
    public static final String SUF_APK_NAME = ".apk";

    // 语音文件扩展名
    public static final String VOICE_EXT_NAME = "AMR";

    public static final int SETTINGS_ITEM_HEIGHT = 42; // 42dip
    public static final int SETTINGS_ITEM_HEADPHOTO_HEIGHT = 64; // 64dip
    public static final String SETTINGS_COMMITINFO_TYPE = "type";
    public static final int SETTINGS_COMMITINFO_NICKNAME = 1;
    public static final int SETTINGS_COMMITINFO_SCHOOL = 2;
    public static final int SETTINGS_COMMITINFO_CORPORATION = 3;
    public static final int SETTINGS_COMMITINFO_SIGNATURE = 4;
    public static final int SETTINGS_COMMITINFO_FEEDBACK = 5;
    public static final int SETTINGS_LARGE_HEAD_WIDTH = 480;
    public static final int SETTINGS_SMALL_HEAD_WIDTH = 72;

    public static final int START = 0;
    public static final int FRIEND_PAGESIZE = 50;
    public static final int PAGESIZE = 120;

    /* 名片List max size 由名片传输协议定义 */
    public static final int MAX_CARD_LIST = 6;

    /** 找回密码获取flag **/

    /* 输入模式 */
    public static final int INPUT_MODE_TEXT = 1;
    public static final int INPUT_MODE_VOICE = 2;
    public static final int INPUT_MODE_SMILEY = 4;
    public static final int INPUT_MODE_PHRASE = 8;

    /** 最长录音时间 (60秒) **/
    public static final int MAX_RECORD_TIME = 60;
    /** 录音倒计时开始(10秒开始倒计时) **/
    public static final int COUNTDOWN_TIME = 10;

    /** 手信小助手的虚拟skyid **/
    public static final int HELPER_SKY_ID = 1;
    /** 手信小助手的名称 **/
    public static final String HELPER_NAME = "手信小助手";

    /** 判断是否为UUID，长度小于5 **/
    public static final int NOT_UUID_LEN = 5;

    // 自动登录同步联系人时间间隔 （1小时）
    public static final long LAST_SYNC_TIME = DateUtils.HOUR_IN_MILLIS;
    // 同步会话的时间间隔（ 10分钟）
    public static final long LAST_SYNC_THREADS_TIME = 10 * DateUtils.MINUTE_IN_MILLIS;

    // 搜索界面的字体高亮颜色
    public static final int SEARCH_COLOR = Color.rgb(77, 162, 229);

    // 群头像默认photoid
    public static final String DEFAULT_MULTI_HEAD = "multihead";

    // 默认系统进程最大使用内存
    public static final int DEFAULT_MAX_MEMORY = 16; // 16MB

    // 最少100个头像
    public static final int DEFAULT_MIN_HEAD_CACHE_SIZE = 20;
    // 邀请地址片段（用户判断是否是发送邀请短信）
    public static final String INVITE_CONTENT = "http://s.mopo.com/d.do";

    // 同步会话和同步联系人进度值 估值
    // 会话同步开始 0
    public static final int SYNC_PROCESS_THREADS_BEGIN = 0;
    public static final int SYNC_PROCESS_THREADS_END = 5;
    public static final int SYNC_PROCESS_MESSAGES_BEGIN = 6;
    public static final int SYNC_PROCESS_MESSAGES_END = 25;
    public static final int SYNC_PROCESS_CONTACTS_BEGIN = 35;
    public static final int SYNC_PROCESS_CONTACTS_SETP1 = 60;// 对这一段再进行分割
    public static final int SYNC_PROCESS_CONTACTS_SETP2 = 65;
    public static final int SYNC_PROCESS_CONTACTS_SETP3 = 75;
    public static final int SYNC_PROCESS_CONTACTS_SETP4 = 85;
    public static final int SYNC_PROCESS_CONTACTS_SETP5 = 95;
    public static final int SYNC_PROCESS_CONTACTS_SETP6 = 100;

    // 短信邀请类型
    public static final byte INVITE_CONFIGURATION_SMS_TYPE = 1;

    // 语音邀请类型
    public static final byte INVITE_CONFIGURATION_VOICE_TYPE = 2;

    // 错误提示
    public static final String ERROR_TIP = "错误码";

    // 网络错误
    public static final int NET_ERROR = 99999;

    // 用户好友类型
    /** 手机导入 **/
    public static final byte CONTACT_TYPE_PHONELOAD = 0;
    /** 运营账号 **/
    public static final byte CONTACT_TYPE_YUNYING = 1;
    /** 推荐联系人 **/
    public static final byte CONTACT_TYPE_TUIJIAN = 2;
    /** 手工添加 **/
    public static final byte CONTACT_TYPE_MANUAL = 3;
    /** 精确搜索 **/
    public static final byte CONTACT_TYPE_SEARCH = 4;
    /** LBS **/
    public static final byte CONTACT_TYPE_LBS = 5;
    /** 名片添加 **/
    public static final byte CONTACT_TYPE_VCARD = 6;

    /** 检查绑定的间隔时间 */
    public static final int CHECK_BIND_INTERVAL = 1000 * 60 * 30;
    /** 发送绑定短信后，每N秒去检查是否绑定成功的间隔时间 */
    public static final int WAIT_BIND_STATUS_INTERVAL = 1000 * 5;
    /** SDCARD最小容量Capacity */
    public static final int SDCARD_MIN_CAPACITY = 1024 * 1024;

    /** 邀请入口值(未定义) **/
    public static final int INVITE_ENTRANCE_UNKNOW = -1;
    /** 邀请入口值(联系人界面) **/
    public static final int INVITE_ENTRANCE_CONTACTS = 3;
    /** 邀请入口值(语音聊天界面) **/
    public static final int INVITE_ENTRANCE_CHAT_VOICE = 5;
}
