
package android.skymobi.messenger.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 类说明：手信各表字段
 * 
 * @author Sean.xie
 * @date 2012-1-20
 * @version 1.0
 */
public class SocialMessenger {

    public static final int Social_Messenger_CODE = 10000;

    public static final String AUTHORITY = "com.skymobi.android.messenger";

    public static final Uri CONTENT_URI = Uri
            .parse("content://com.skymobi.android.messenger");

    /**
     * Threads 表字段
     * 
     * @author Sean.Xie
     */
    public static class ThreadsColumns implements BaseColumns {

        public static final int THREADS_CODE = Social_Messenger_CODE + 1;

        public static final String TABLE_NAME = "threads";

        private ThreadsColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/threads");

        /**
         * 电话号码
         */
        public static final String PHONES = "phones";
        /**
         * 消息数
         */
        public static final String _COUNT = "_count";
        /**
         * 时间
         */
        public static final String DATE = "date";
        /**
         * 最新消息内容
         */
        public static final String CONTENT = "content";
        /**
         * 消息阅读状态(已读:1,未读:0)
         */
        public static final String READ = "read";
        /**
         * 本地会话ID
         */
        public static final String LOCAL_THREADS_ID = "local_threads_id";

        /**
         * 账号ID
         */
        public static final String ACCOUNT_IDS = "account_ids";

        /*
         * display name
         */
        public static final String DISPLAY_NAME = "displayName";

        public static final String ADDRESS_IDS = "address_ids";

        public static final String STATUS = "status";

    }

    /**
     * Messages 表字段
     * 
     * @author Sean.Xie
     */
    public static class MessagesColumns implements BaseColumns {

        public static final int MESSAGES_CODE = Social_Messenger_CODE + 2;

        public static final String TABLE_NAME = "messages";

        private MessagesColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/messages");

        /**
         * 消息文本
         */
        public static final String CONTENT = "content";
        /**
         * 多媒体
         */
        public static final String MEDIA = "media";
        /**
         * 消息阅读状态(未读:0，已读:1)
         */
        public static final String READ = "read";
        public static final int READ_NO = 0;
        public static final int READ_YES = 1;

        /**
         * 消息类型(文本消息:1, 语音消息:2, 名片消息:3, 短信:4, 推荐好友消息：5)
         */
        public static final String TYPE = "type";
        public static final int TYPE_TEXT = 1;
        public static final int TYPE_VOICE = 2;
        public static final int TYPE_CARD = 3;
        public static final int TYPE_SMS = 4;
        public static final int TYPE_FRD = 5;
        /**
         * 发送:2,接收:1
         */
        public static final String OPT = "opt";
        public static final int OPT_FROM = 1; // 接收
        public static final int OPT_TO = 2; // 发送
        public static final int OPT_SENDING = 4; // 正在发送
        public static final int OPT_SENDSTART = 6; // 开始发送

        /**
         * 状态 (发送成功:0, 发送中:32, 发送失败:64)
         */
        public static final String STATUS = "status";
        public static final int STATUS_SUCCESS = 0; // 发送成功
        public static final int STATUS_SENDING = 32; // 发送中
        public static final int STATUS_FAILED = 64; // 发送失败

        /**
         * 本地短信ID
         */
        public static final String SMS_ID = "sms_id";
        /**
         * 电话号码
         */
        public static final String PHONE = "phone";
        /**
         * 时间
         */
        public static final String DATE = "date";
        /**
         * 本地短信会话ID
         */
        public static final String LOCAL_THREADS_ID = "local_threads_id";
        /**
         * Threads 会话ID
         */
        public static final String THREADS_ID = "threads_id";

        /**
         * 排序ID
         */
        public static final String SEQUENCE_ID = "sequence_id";

    }

    /**
     * 联系人表字段
     * 
     * @author Sean.Xie
     */
    public static class ContactsColumns implements BaseColumns {

        public static final int CONTACTS_CODE = Social_Messenger_CODE + 3;

        public static final String TABLE_NAME = "contacts";

        private ContactsColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/contacts");

        /**
         * 本地联系人ID
         */
        public static final String LOCAL_CONTACT_ID = "local_contact_id";
        /**
         * 云端联系人ID
         */
        public static final String CLOUD_ID = "cloud_id";
        /**
         * 姓名
         */
        public static final String DISPLAY_NAME = "display_name";
        /**
         * 性别(0未设置 1男 2女 3保密)
         */
        public static final String SEX = "sex";
        public static final int SEX_UNKNOW = 0;
        public static final int SEX_MALE = 1;
        public static final int SEX_FEMALE = 2;
        public static final int SEX_SECRET = 3;

        /**
         * 签名
         */
        public static final String SIGNATURE = "signature";
        /**
         * 生日
         */
        public static final String BIRTHDAY = "birthday";
        /**
         * 单位
         */
        public static final String ORGANIZATION = "organization";
        /**
         * 家乡
         */
        public static final String HOMETOWN = "hometown";
        /**
         * 备注
         */
        public static final String NOTE = "note";
        /**
         * 学校
         */
        public static final String SCHOOL = "school";
        /**
         * 头像
         */
        public static final String PHOTO_ID = "photo_id";
        /**
         * 黑名单(在:1 不在:0)
         */
        public static final String BLACK_LIST = "black_list";
        public static final int BLACK_LIST_YES = 1;
        public static final int BLACK_LIST_NO = 0;

        /**
         * 拼音
         */
        public static final String PINYIN = "pinyin";
        /**
         * 汉字加拼音 用于搜索
         */
        public static final String SORTKEY = "sortkey";

        /**
         * 删除标示
         */
        public static final String DELETED = "deleted";
        public final static int DELETED_YES = 1;
        public final static int DELETED_NO = 0;
        /**
         * 同步状态 1:已同步 0:未同步
         */
        public static final String SYNCED = "synced";
        public final static int SYNC_YES = 1;
        public final static int SYNC_NO = 0;
        /**
         * 是否是手信联系人 (普通用户:0 手信用户:1 推荐用户:2)
         */
        public static final String USER_TYPE = "user_type";
        public final static int USER_TYPE_LOACL = 0;
        public final static int USER_TYPE_SHOUXIN = 1;
        public final static int USER_TYPE_STRANGER = 2;
        public final static int USER_TYPE_LBS_STRANGER = 3;

        public static final String SKYID = "skyid";
        public static final String LAST_UPDATE_TIME = "last_update_time";
    }

    /**
     * 账号表字段
     * 
     * @author Sean.Xie
     */
    public static class AccountsColumns implements BaseColumns {

        public static final int ACCOUNTS_CODE = Social_Messenger_CODE + 4;

        public static final String TABLE_NAME = "accounts";

        private AccountsColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/contacts");

        /**
         * 联系人ID
         */
        public static final String CONTACT_ID = "contact_id";
        /**
         * SKYID
         */
        public static final String SKYID = "skyid";
        /**
         * 手信号
         */
        public static final String SKY_NAME = "sky_name";
        public static final String SKY_NICKNAME = "nickname";
        /**
         * 电话号码
         */
        public static final String PHONE = "phone";

        /**
         * 主账号 (是:1 不是:0)
         */
        public static final String IS_MAIN = "is_main";
    }

    /**
     * 头像表字段
     * 
     * @author Sean.Xie
     */
    public static class PhotosColumns implements BaseColumns {

        public static final int PHOTOS_CODE = Social_Messenger_CODE + 5;

        public static final String TABLE_NAME = "photos";

        private PhotosColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/photos");

        /**
         * 头像路径
         */
        public static final String PATH = "path";

        /**
         * 头像版本
         */
        public static final String VERSION = "version";
    }

    /**
     * @ClassName: FriendsColums
     * @Description: 好友列表字段
     * @author Anson.Yang
     * @date 2012-3-4 下午7:56:25
     */
    public static class FriendsColumns implements BaseColumns {
        public static final int FRIENDS_CODE = Social_Messenger_CODE + 6;
        public static final String TABLE_NAME = "friends";

        private FriendsColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/friends");

        /**
         * skyid
         */
        public static final String SKYID = "skyid";

        /**
         * 联系人表ID
         */
        public static final String CONTACT_ID = "contact_id";

        /**
         * 昵称
         */
        public static final String NICKNAME = "nickname";

        /**
         * 头像
         */
        public static final String PHOTO_ID = "photo_id";

        /**
         * 推荐理由
         */
        public static final String RECOMMEND_REASON = "recommend_reason";

        /**
         * 好友类型
         */
        public static final String CONTACT_TYPE = "contact_type";
        public static final int CONTACT_TYPE_YUNYING = 1; // 运营账号
        public static final int CONTACT_TYPE_TUIJIAN = 2; // 推荐联系人
        public static final int CONTACT_TYPE_FROM_HELPER = 3; // 小助手

        /**
         * 推荐详情
         */
        public static final String DETAIL_REASON = "detail_reason";

        /**
         * 第一次打招呼提示语
         */
        public static final String TALK_REASON = "talk_reason";
    }

    /**
     * 流量表字段对象
     * 
     * @ClassName: TrafficColums
     * @Description: 流量表字段
     * @author Anson.Yang
     * @date 2012-3-4 下午7:56:25
     */
    public static class TrafficColumns implements BaseColumns {
        public static final int FRIENDS_CODE = Social_Messenger_CODE + 7;
        public static final String TABLE_NAME = "tbl_traffic";

        private TrafficColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/tbl_traffic");

        public static final String DATE = "date";

        public static final String WIFI = "wifi";

        public static final String WIFI_LATEST = "wifi_latest";

        public static final String MOBILE = "mobile";

        public static final String MOBILE_LATEST = "mobile_latest";

        public static final String APP_MOBILE = "app_mobile";

        public static final String APP_WIFI = "app_wifi";

    }

    public static class FilesColumns implements BaseColumns {

        public static final String TABLE_NAME = "files";

        public static final String VERSION = "version";

        public static final String PATH = "path";

        public static final String URL = "url";

        public static final String SIZE = "size";

        public static final String LENGTH = "length";

        public static final String FORMAT = "format";
    }

    public static class UsersColumns implements BaseColumns {

        public static final String TABLE_NAME = "users";

        public static final String SKYID = "skyid";
        public static final String LAST_UPDATE_TIME = "last_update_time";
        public static final String LAST_FRIEND_TIME = "last_friend_time";

    }

    public static class AddressColumns implements BaseColumns {
        public static final String TABLE_NAME = "address";

        public static final String PHONE = "phone";
        public static final String SKYID = "skyid";
        public static final String ACCOUNTID = "accountId";
    }
    
    public static class StrangerColumns implements BaseColumns{
        public static final String TABLE_NAME = "stranger";
        /**
         * 姓名
         */
        public static final String DISPLAY_NAME = "display_name";
        /**
         * 性别(0未设置 1男 2女 3保密)
         */
        public static final String SEX = "sex";
        public static final int SEX_UNKNOW = 0;
        public static final int SEX_MALE = 1;
        public static final int SEX_FEMALE = 2;
        public static final int SEX_SECRET = 3;

        /**
         * 签名
         */
        public static final String SIGNATURE = "signature";
        /**
         * 生日
         */
        public static final String BIRTHDAY = "birthday";
        /**
         * 单位
         */
        public static final String ORGANIZATION = "organization";
        /**
         * 家乡
         */
        public static final String HOMETOWN = "hometown";
        /**
         * 备注
         */
        public static final String NOTE = "note";
        /**
         * 学校
         */
        public static final String SCHOOL = "school";
        /**
         * 头像
         */
        public static final String PHOTO_ID = "photo_id";
        /**
         * 黑名单(在:1 不在:0)
         */
        public static final String BLACK_LIST = "black_list";
        public static final int BLACK_LIST_YES = 1;
        public static final int BLACK_LIST_NO = 0;

        /**
         * 拼音
         */
        public static final String PINYIN = "pinyin";
        /**
         * 汉字加拼音 用于搜索
         */
        public static final String SORTKEY = "sortkey";
        
        /**
         * 斯凯ID
         */
        public final static String SKY_ID = "skyid";
        /**
         * 斯凯帐号
         */
        public static final String SKYNAME = "sky_name";
        /**
         * 昵称
         */
        public static final String NICKNAME = "nickname";
        
        public static final String LAST_UPDATE_TIME = "last_update_time";
    }
}
