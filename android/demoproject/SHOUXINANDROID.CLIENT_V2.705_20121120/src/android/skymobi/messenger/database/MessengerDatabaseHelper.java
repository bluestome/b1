
package android.skymobi.messenger.database;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.skymobi.messenger.R;
import android.skymobi.messenger.utils.CommonPreferences;

/**
 * 类说明：
 * 
 * @author Sean.xie
 * @date 2012-1-19
 * @version 1.0
 */
public class MessengerDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "SXSQLite";

    private static final String name = "messenger.db";

    private static int version = 1;
    private static MessengerDatabaseHelper databaseHelper = null;
    private static SQLiteDatabase sqLiteDatabase = null;

    private MessengerDatabaseHelper(Context context) {
        super(context, name, null, version);
    }

    public synchronized static MessengerDatabaseHelper getInstance(Context context) {
        if (databaseHelper == null) {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo pinfo = pm.getPackageInfo(context.getPackageName(),
                        PackageManager.GET_CONFIGURATIONS);
                version = pinfo.versionCode;
                if (version == 0) {
                    version = 1;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            databaseHelper = new MessengerDatabaseHelper(context);
        }
        if (sqLiteDatabase == null) {
            try {
                sqLiteDatabase = databaseHelper.getWritableDatabase();
                SLog.e(TAG, "初始化SQLiteDatabase [" + (sqLiteDatabase != null) + "], name [" + name
                        + "], version [" + version + "]");
            } catch (Exception e) {
                // anson.yang @20121012
                SLog.e(TAG, "初始化SQLiteDatabase error");
                e.printStackTrace();
            }
        }

        return databaseHelper;
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return sqLiteDatabase;
    }

    public synchronized void shutdown() {
        if (sqLiteDatabase != null) {
            try {
                sqLiteDatabase.close();
                sqLiteDatabase = null;
            } catch (Exception e) {
            }
        }
        if (databaseHelper != null) {
            try {
                databaseHelper.close();
                databaseHelper = null;
            } catch (Exception e) {
            }
        }
        SLog.i(TAG, "数据库成功关闭");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // THREADS
        db.execSQL("CREATE TABLE IF NOT EXISTS threads ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // id
                + "phones TEXT, " // 电话号码
                + "account_ids TEXT, " // Acconts ID
                + "_count INTEGER, "// 总条数
                + "date INTEGER, " // 日期
                + "content TEXT, "// 内容
                + "displayName TEXT, "// 显示内容
                + "read INTEGER default 0, "// 消息阅读状态(已读:1,未读:0)
                + "local_threads_id INTEGER," // 本地会话ID
                + "address_ids TEXT,"// 会话地址
                + "status INTEGER, " // 发送中:0,发送成功:1 发送失败:2
                + "data1 TEXT,"// 备用字段
                + "data2 TEXT,"// 备用字段
                + "data3 TEXT"// 备用字段
                + ")");

        // MESSAGES
        db.execSQL("CREATE TABLE IF NOT EXISTS messages ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
                + "content TEXT, "// 內容
                + "media TEXT, " // 音頻文件路徑
                + "read INTEGER default 0, "// 消息阅读状态(已读:1,未读:0)
                + "type INTEGER default 4, " // 消息类型(文本消息:1,音频消息:2,图片消息:3,短消息:4)
                + "opt INTEGER, " // 发送:2,接收:1
                + "status INTEGER, " // 发送中:0,发送成功:1 发送失败:2
                + "sms_id INTEGER, "// 本地短信ID
                + "phone TEXT, " // 电话号码
                + "date INTEGER, " // 日期
                + "local_threads_id INTEGER, " // 本地会话ID
                + "threads_id INTEGER, " // Threads ID
                + "sequence_id INTEGER, "// sequence_id
                + "data1 TEXT,"// 备用字段
                + "data2 TEXT,"// 备用字段
                + "data3 TEXT"// 备用字段
                + ")");

        // Contacts
        db.execSQL("CREATE TABLE IF NOT EXISTS contacts ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
                + "local_contact_id INTEGER, " // 本地联系人ID
                + "cloud_id INTEGER, " // 云端联系人ID
                + "display_name TEXT, " // 名称
                + "sex INTEGER default 0, " // 性别(0:未设置 男:1 女:2 保密:3)
                + "signature TEXT, "// 签名
                + "birthday INTEGER, " // 生日
                + "organization TEXT, " // 单位
                + "hometown TEXT, " // 地区
                + "note TEXT, " // 备注
                + "school TEXT, " // 学校
                + "photo_id TEXT, " // 头像
                + "black_list INTEGER default 0, " // 黑名单(在:1 不在:0)
                + "pinyin TEXT, " // 拼音
                + "sortkey TEXT, " // 拼音用于搜索
                + "skyid Text," // skyID
                + "synced INTEGER default 0, " // 同步状态 1:已同步 0:未同步
                + "deleted INTEGER default 0, " // 删除状态 1:已删除 0:未删除
                + "user_type INTEGER default 0," // 普通用户:0 手信用户:1 推荐用户:2
                + "last_update_time INTEGER,"
                + "data1 TEXT,"// 备用字段
                + "data2 TEXT,"// 备用字段
                + "data3 TEXT"// 备用字段
                + ")");

        // ACCOUNTS
        db.execSQL("CREATE TABLE IF NOT EXISTS accounts ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
                + "contact_id INTEGER, "// 联系人ID
                + "nickname TEXT, " // 昵称
                + "skyid INTEGER default -1, " // SKYID(1表示小助手，-1表示没有sky帐号)
                + "sky_name TEXT, " // 手信号
                + "phone TEXT, "// 电话号码
                + "is_main INTEGER  default 0,"// 是否主账号 (是:1 不是:0)
                + "data1 TEXT,"// 备用字段
                + "data2 TEXT,"// 备用字段
                + "data3 TEXT"// 备用字段
                + ")");

        // Files（消息附件，如语音，图片，好友头像等）
        db.execSQL("CREATE TABLE IF NOT EXISTS files ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
                + "version INTEGER, " // 版本
                + "path TEXT," // sdcard路径
                + "url TEXT," // url地址
                + "size INTEGER," // 文件大小
                + "length INTEGER default 0," // 语音长度
                + "format TEXT," // 格式
                + "data1 TEXT,"// 备用字段
                + "data2 TEXT,"// 备用字段
                + "data3 TEXT"// 备用字段
                + ")");

        // Friends
        db.execSQL("CREATE TABLE IF NOT EXISTS friends ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT," // ID
                + "skyid INTEGER,"
                + "contact_id INTEGER," // 联系人表ID
                + "nickname TEXT," // 联系人表ID
                + "photo_id INTEGER," // 联系人表ID
                + "recommend_reason TEXT," // 推荐理由
                + "talk_reason TEXT," // 推荐理由
                + "contact_type INTEGER," // 联系人类型
                + "detail_reason TEXT," // 详细理由
                + "data1 TEXT,"// 备用字段
                + "data2 TEXT,"// 备用字段
                + "data3 TEXT"// 备用字段
                + ")");

        // tbl_traffic 流量表
        db.execSQL("CREATE TABLE IF NOT EXISTS tbl_traffic ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT," // ID
                + "date TEXT," // 日期(YYYY-MM-DD)
                + "wifi INTEGER," // 无线网络流量
                + "wifi_latest INTEGER," // 最后一次WIFI流量值
                + "mobile INTEGER," // 移动网络流量
                + "mobile_latest INTEGER," // 最后一次移动流量值
                + "app_mobile INTEGER," // 应用无线流量
                + "app_wifi INTEGER," // 应用移动流量
                + "data1 TEXT,"// 备用字段
                + "data2 TEXT,"// 备用字段
                + "data3 TEXT"// 备用字段
                + ")");

        // contact version 用户表
        db.execSQL("CREATE TABLE IF NOT EXISTS users ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT," // ID
                + "last_update_time INTEGER," // 最新版本
                // + "nickname TEXT," // 昵称
                // + "school TEXT," // 学校
                + "last_friend_time INTEGER," // 最新找朋友时间
                + "skyid INTEGER" // skyid
                + ")");

        // address 地址
        db.execSQL("CREATE TABLE IF NOT EXISTS address ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT," // ID
                + "phone TEXT," // 最新找朋友时间
                + "skyid INTEGER," // skyid
                + "accountId INTEGER"
                + ")");

        // stranger 陌生人详情
        db.execSQL("CREATE TABLE IF NOT EXISTS stranger ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
                + "display_name TEXT, " // 名称
                + "sex INTEGER default 0, " // 性别(0:未设置 男:1 女:2 保密:3)
                + "signature TEXT, "// 签名
                + "birthday INTEGER, " // 生日
                + "organization TEXT, " // 单位
                + "hometown TEXT, " // 地区
                + "note TEXT, " // 备注
                + "school TEXT, " // 学校
                + "photo_id TEXT, " // 头像
                + "black_list INTEGER default 0, " // 黑名单(在:1 不在:0)
                + "pinyin TEXT, " // 拼音
                + "sortkey TEXT, " // 拼音用于搜索
                + "skyid Text," // skyID
                + "nickname TEXT, " // 昵称
                + "sky_name TEXT, " // 手信号
                + "last_update_time INTEGER"
                + ")");
        // Index
        db.beginTransaction();
        try {
            db.execSQL("Drop Index If Exists MAIN.[account_phone_index]");
            db.execSQL("CREATE  INDEX MAIN.[account_phone_index] On [accounts] ( [phone] ) ");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        // After Insert Messages Row Trigger
        afterInsertMessagesRow(db);
        // After Delete Threads Row Trigger
        StringBuilder afterDeleteThreadsTrigger = new StringBuilder();
        afterDeleteThreadsTrigger
                .append(" Create  Trigger MAIN.[Threads_delete_trigger] AFTER DELETE On [threads] FOR EACH ROW")
                .append(" begin ")
                .append(" delete from messages ")
                .append(" where threads_id=old._id;")
                .append(" end;");
        db.beginTransaction();
        try {
            db.execSQL("Drop Trigger If Exists MAIN.[Threads_delete_trigger]");
            db.execSQL(afterDeleteThreadsTrigger.toString());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        // After Delete Contact Row Trigger
        StringBuilder afterDeleteContactTrigger = new StringBuilder();
        afterDeleteContactTrigger
                .append(" Create  Trigger MAIN.[contacts_delete_trigger] AFTER DELETE On [contacts] FOR EACH ROW")
                .append(" begin ")
                .append(" delete from accounts ")
                .append(" where contact_id=old._id;")
                .append(" delete from files ")
                .append(" where _id=old.photo_id;")
                .append(" end;");
        db.beginTransaction();
        try {
            db.execSQL("Drop Trigger If Exists MAIN.[contacts_delete_trigger]");
            db.execSQL(afterDeleteContactTrigger.toString());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        // 将address的accountId更新 after delete accounts
        StringBuilder afterDeleteAccountsTrigger = new StringBuilder();
        afterDeleteAccountsTrigger
                .append(" Create  Trigger MAIN.[Accounts_delete_trigger] AFTER DELETE On [accounts] FOR EACH ROW")
                .append(" begin ")
                .append(" update address set accountId=0")
                .append(" where accountId=old._id;")
                .append(" end;");
        db.beginTransaction();
        try {
            db.execSQL("Drop Trigger If Exists MAIN.[Accounts_delete_trigger]");
            db.execSQL(afterDeleteAccountsTrigger.toString());
            String testData1 = "Insert  Into contacts (_id,local_contact_id,cloud_id,display_name,sex,signature,birthday,organization,hometown,note,school,photo_id,black_list,pinyin,user_type) Values('1','0','0','手信小助手',2,'这是我的签名!',NULL,NULL,NULL,NULL,NULL,NULL,'0','shouxinxiaozushou','1')";
            db.execSQL(testData1);
            String testData2 = "Insert  Into accounts (_id,contact_id,skyid,sky_name,phone,is_main) Values('1','1','1','手信小助手',NULL,'1')";
            db.execSQL(testData2);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        afterDeleteMessagesRow(db);
        SLog.i(TAG, "数据库表结构初始化完成");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SLog.d(TAG, "onUpgrade !!!! oldVersion  " + oldVersion + "  newVersion:"
                + newVersion);
        upgradeTable(db);
        afterDeleteMessagesRow(db);
        afterInsertMessagesRow(db);
        delShortcut();
        // 重置部分参数，用于处理覆盖安装的问题
        CommonPreferences.setShortCutSetting(false);
    }

    /**
     * 4.0 系统低版本升级调此接口
     * 
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SLog.d(TAG, " onDowngrade ");
        delShortcut();
        // 重置部分参数，用于处理覆盖安装的问题
        CommonPreferences.setShortCutSetting(false);
    }

    /**
     * 升级安装时需要新增的数据库表
     * 
     * @param db
     */
    private void upgradeTable(SQLiteDatabase db) {
        // stranger 陌生人详情
        db.execSQL("CREATE TABLE IF NOT EXISTS stranger ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
                + "display_name TEXT, " // 名称
                + "sex INTEGER default 0, " // 性别(0:未设置 男:1 女:2 保密:3)
                + "signature TEXT, "// 签名
                + "birthday INTEGER, " // 生日
                + "organization TEXT, " // 单位
                + "hometown TEXT, " // 地区
                + "note TEXT, " // 备注
                + "school TEXT, " // 学校
                + "photo_id TEXT, " // 头像
                + "black_list INTEGER default 0, " // 黑名单(在:1 不在:0)
                + "pinyin TEXT, " // 拼音
                + "sortkey TEXT, " // 拼音用于搜索
                + "skyid Text," // skyID
                + "nickname TEXT, " // 昵称
                + "sky_name TEXT, " // 手信号
                + "last_update_time INTEGER"
                + ")");

        // 简单属性表,hzc@20120919
        db.execSQL("CREATE TABLE IF NOT EXISTS sxconfig ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
                + "attrName TEXT, " // 属性名
                + "attrVal TEXT, " // 属性值
                + "version TEXT, "// 版本号
                + "caption TEXT, " // 说明
                + "reserve0 TEXT, " //
                + "reserve1 TEXT, " //
                + "createTime INTEGER"// 创建时间
                + ")");

    }

    /**
     * 删除空会话
     * 
     * @param db
     */
    private void afterDeleteMessagesRow(SQLiteDatabase db) {
        // After Delete Messages Row Trigger
        StringBuilder afterDeleteMessageTrigger = new StringBuilder();
        afterDeleteMessageTrigger
                .append(" Create  Trigger MAIN.[messages_delete_trigger] AFTER DELETE On [messages] FOR EACH ROW")
                .append(" begin ")
                .append(" delete from threads ")
                .append(" where _id=old.threads_id and (select count(_id) from messages where threads_id=old.threads_id)=0;")
                .append(" update threads ")
                .append(" set _count=(select count(_id) from messages where threads_id=old.threads_id)")
                .append(" where _id=old.threads_id;")
                .append(" end;");
        db.beginTransaction();
        try {
            db.execSQL("Drop Trigger If Exists MAIN.[messages_delete_trigger]");
            db.execSQL(afterDeleteMessageTrigger.toString());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }

    private void afterInsertMessagesRow(SQLiteDatabase db) {
        // After Insert Messages Row Trigger
        StringBuilder afterInsertMessageTrigger = new StringBuilder();
        afterInsertMessageTrigger
                .append(" Create  Trigger MAIN.[messages_insert_trigger] AFTER INSERT On [messages] FOR EACH ROW")
                .append(" begin ")
                .append(" update threads ")
                .append(" set _count=(select count(_id) from messages where threads_id=new.threads_id)")
                .append(" where _id=new.threads_id;")
                .append(" delete from messages where threads_id=new.threads_id and (select count(_id) from threads where _id=new.threads_id)=0;")
                .append(" end;");
        db.beginTransaction();
        try {
            db.execSQL("Drop Trigger If Exists MAIN.[messages_insert_trigger]");
            db.execSQL(afterInsertMessageTrigger.toString());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 删除快捷方式
     */
    private void delShortcut() {
        SLog.d(TAG, "\t>>>>>> 删除桌面图标开始 ");
        Context context = MainApp.i().getApplicationContext();
        Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT"); // 快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name)); //
        String appClass = context.getPackageName() + ".LaunchActivity";
        ComponentName comp = new ComponentName(context.getPackageName(), appClass);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                new Intent(Intent.ACTION_MAIN).setComponent(comp));
        context.sendBroadcast(shortcut);
        SLog.d(TAG, "\t>>>>>> 删除桌面图标结束 ");
    }

}
