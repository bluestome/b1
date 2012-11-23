
package android.skymobi.messenger.database;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.MainApp;
import android.util.Log;

import com.skymobi.android.sx.codec.beans.common.MsgType;
import com.skymobi.android.sx.codec.beans.common.RecommendMsg;

import java.util.ArrayList;

/**
 * @ClassName: PhraseDatabaseHelper
 * @Description: TODO
 * @author Michael.Pan
 * @date 2012-3-6 下午01:31:45
 */
public class PhraseDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = PhraseDatabaseHelper.class.getSimpleName();
    private static final String dbname = "phrase.db";
    private static final int dbversion = 1;

    private static PhraseDatabaseHelper sInstance;
    private static SQLiteDatabase sDatabase = null;
    private static Object classLock = PhraseDatabaseHelper.class;

    public static PhraseDatabaseHelper getInstance() {
        synchronized (classLock) {
            if (sInstance == null) {
                sInstance = new PhraseDatabaseHelper();
            }
            return sInstance;
        }
    }

    // 添加短语Title到表中
    public void addMsgTypeList(ArrayList<MsgType> list) {
        if (list == null || list.size() <= 0) {
            return;
        }
        sDatabase.beginTransaction();
        for (MsgType msgType : list) {
            if (msgType.getAction() == 2) {
                // 删除该短信类型
                deleteTitle(msgType.getMsgTypeId());
                deletePhrase(msgType.getMsgTypeId());
            } else {
                // 判断是否存在相同的类型，如果存在相同的类型，则不添加
                if (!isExistsTitleByTypeId(msgType.getMsgTypeId())) {
                    SLog.d(TAG,
                            "\t>>>>>> 添加短信类型[" + msgType.getMsgTypeId() + "|"
                                    + msgType.getMsgTypeName() + "]");
                    insertTitle(msgType.getMsgTypeId(), msgType.getMsgTypeName());
                } else {
                    SLog.d(TAG,
                            "\t>>>>>> 更新短信类型[" + msgType.getMsgTypeId() + "|"
                                    + msgType.getMsgTypeName() + "]");
                    updateTitle(msgType.getMsgTypeId(), msgType.getMsgTypeName());
                }
            }
        }
        sDatabase.setTransactionSuccessful();
        sDatabase.endTransaction();
    }

    /**
     * 判断是否存在相同类型ID的短信类别
     * 
     * @param typeId 短信类别ID
     * @return true 存在| false 不存在
     */
    public boolean isExistsTitleByTypeId(int typeId) {
        boolean isExists = false;
        Cursor cur = null;
        String sql = "select type_id  from title where type_id = ?";
        String[] selectionArgs = {
                String.valueOf(typeId)
        };
        try {
            cur = sDatabase.rawQuery(sql, selectionArgs);
            while (cur.moveToNext()) {
                isExists = true;
                break;
            }
        } catch (SQLException ex) {
            Log.e(TAG, "fetchTitle failed! - " + ex.getMessage());
        }
        if (cur != null)
            cur.close();
        return isExists;
    }

    // 查询所有title
    public ArrayList<MsgType> fetchTitle() {
        ArrayList<MsgType> list = new ArrayList<MsgType>();
        Cursor cur = null;
        String sql = "select  type_id, type_name from title";
        try {
            cur = sDatabase.rawQuery(sql, null);
            while (cur.moveToNext()) {
                MsgType item = new MsgType();
                item.setMsgTypeId(cur.getInt(0));
                item.setMsgTypeName(cur.getString(1));
                list.add(item);
            }
        } catch (SQLException ex) {
            Log.e(TAG, "fetchTitle failed! - " + ex.getMessage());
        }
        if (cur != null)
            cur.close();
        return list;
    }

    // 添加短语到表中
    public void addPhraseList(ArrayList<String> list, int typeID) {
        deletePhrase(typeID);
        sDatabase.beginTransaction();
        for (String text : list) {
            insertPhrase(typeID, text);
        }
        sDatabase.setTransactionSuccessful();
        sDatabase.endTransaction();
    }

    // 添加短语到表中
    public void addPhraseList2(ArrayList<RecommendMsg> list, int typeID) {
        sDatabase.beginTransaction();
        for (RecommendMsg text : list) {
            // 表明当前的短信，已经从服务端删除，客户端是否需要删除可以再考虑
            if (text.getAction() == 2) {
                deleteTextMessage(typeID, text.getMsgId());
            } else {
                // 判断是否存在相同的短信类型下的短信ID
                if (!isExistsPhrase(text.getMsgId())) {
                    SLog.d(TAG,
                            ">>>>>> 添加短信[" + typeID + "|" + text.getMsgId() + "|"
                                    + text.getTextMessage() + "]");
                    insertPhrase(typeID, text.getMsgId(), text.getTextMessage());
                } else {
                    SLog.d(TAG,
                            ">>>>>> 修改短信[" + typeID + "|" + text.getMsgId() + "|"
                                    + text.getTextMessage() + "]");
                    updatePhrase(typeID, text.getMsgId(), text.getTextMessage());
                }
            }
        }
        sDatabase.setTransactionSuccessful();
        sDatabase.endTransaction();
    }

    public ArrayList<String> fetchPhrase(int typeID) {
        ArrayList<String> list = new ArrayList<String>();
        Cursor cur = null;
        String sql = "select type_id, text_msg, text_msg_id from phrase where type_id=? order by text_msg_id desc";
        String[] selectionArgs = {
                typeID + ""
        };
        try {
            cur = sDatabase.rawQuery(sql, selectionArgs);
            while (cur.moveToNext()) {
                list.add(cur.getString(1));
            }
        } catch (SQLException ex) {
            Log.e(TAG, "fetchPhrase failed! - " + ex.getMessage());
        }
        if (cur != null)
            cur.close();
        return list;
    }

    @Override
    public void close() {
        if (sDatabase != null) {
            sDatabase.close();
        }
    }

    private PhraseDatabaseHelper() {
        super(MainApp.i(), dbname, null, dbversion);
        sDatabase = getWritableDatabase();
    }

    // 删除全部title数据
    private void deleteTitle() {
        String sql = "delete from title";
        try {
            sDatabase.execSQL(sql);
        } catch (SQLException ex) {
            Log.e(this.getClass().toString(), "delete failed! - "
                    + ex.getMessage());
        }
    }

    // 删除指定类型ID的推荐短语
    private void deleteTitle(int mstTypeId) {
        String sql = "delete from title where type_id = ?";
        Object[] args = new Object[] {
                mstTypeId
        };
        try {
            sDatabase.execSQL(sql, args);
        } catch (SQLException ex) {
            Log.e(this.getClass().toString(), "delete failed! - "
                    + ex.getMessage());
        }
    }

    // 添加短信类别
    private void insertTitle(int type_id, String type_name) {
        String sql = "insert into title (_id, type_id, type_name) values(null,?,?);";
        Object[] args = new Object[] {
                type_id, type_name
        };
        try {
            sDatabase.execSQL(sql, args);
        } catch (SQLException ex) {
            Log.e(this.getClass().toString(), "insert title failed! - " + ex.getMessage());
        }
    }

    // 更新短信类别
    private void updateTitle(int type_id, String type_name) {
        String sql = "update title set type_name = ? where type_id = ?;";
        Object[] args = new Object[] {
                type_name, type_id
        };
        try {
            sDatabase.execSQL(sql, args);
        } catch (SQLException ex) {
            Log.e(this.getClass().toString(), "update title failed! - " + ex.getMessage());
        }
    }

    // 删除全部title数据
    private void deletePhrase(int type_id) {
        String sql = "delete from phrase where type_id=?";
        Object[] args = new Object[] {
                type_id
        };
        try {
            sDatabase.execSQL(sql, args);
        } catch (SQLException ex) {
            Log.e(this.getClass().toString(), "delete failed! - "
                    + ex.getMessage());
        }
    }

    /**
     * 删除指定短信类型ID和短信ID的记录
     * 
     * @param textId
     */
    private void deleteTextMessage(int typeId, int textId) {
        String sql = "delete from phrase where type_id = ? and text_msg_id=? ";
        Object[] args = new Object[] {
                typeId, textId
        };
        try {
            SLog.d(this.getClass().toString(), "\t>>> 删除 短信类别[" + typeId + "] 短信ID[" + textId + "]");
            sDatabase.execSQL(sql, args);
        } catch (SQLException ex) {
            SLog.e(this.getClass().toString(), "delete failed! - "
                    + ex.getMessage());
        }
    }

    // 加入一个title数据
    private void insertPhrase(int type_id, String text_msg) {
        String sql = "insert into phrase (_id, type_id, text_msg) values(null,?,?);";
        Object[] args = new Object[] {
                type_id, text_msg
        };
        try {
            sDatabase.execSQL(sql, args);
        } catch (SQLException ex) {
            Log.e(this.getClass().toString(), "insert phrase failed! - " + ex.getMessage());
        }
    }

    /**
     * 判断是否存在相同类型和相同短信的数据
     * 
     * @param type_id
     * @param textMsgId
     * @return
     */
    private boolean isExistsPhrase(int textMsgId) {
        boolean isExists = false;
        Cursor cur = null;
        String sql = "select text_msg_id from phrase where text_msg_id=?";
        String[] selectionArgs = {
                String.valueOf(textMsgId)
        };
        try {
            cur = sDatabase.rawQuery(sql, selectionArgs);
            while (cur.moveToNext()) {
                isExists = true;
                break;
            }
        } catch (SQLException ex) {
            SLog.e(TAG, "isExistsPhrase failed! - " + ex.getMessage());
        } finally {
            if (cur != null)
                cur.close();
        }
        return isExists;
    }

    // 往推荐短信表中新增一条指定类型的短信记录
    private void insertPhrase(int type_id, int textMsgId, String text_msg) {
        String sql = "insert into phrase (_id, type_id, text_msg_id,text_msg) values(null,?,?,?);";
        Object[] args = new Object[] {
                type_id, textMsgId, text_msg
        };
        try {
            SLog.d(this.getClass().toString(), "\t>>> 添加 " + type_id + "|" + textMsgId + "|"
                    + text_msg);
            sDatabase.execSQL(sql, args);
        } catch (SQLException ex) {
            SLog.e(this.getClass().toString(), "insert phrase failed! - " + ex.getMessage());
        }
    }

    // 往推荐短信表中新增一条指定类型的短信记录
    private void updatePhrase(int type_id, int textMsgId, String text_msg) {
        String sql = "update phrase set text_msg = ? where type_id = ? and text_msg_id = ?";
        Object[] args = new Object[] {
                text_msg, type_id, textMsgId
        };
        try {
            SLog.d(this.getClass().toString(), "\t>>> 更新 " + type_id + "|" + textMsgId + "|"
                    + text_msg);
            sDatabase.execSQL(sql, args);
        } catch (SQLException ex) {
            SLog.e(this.getClass().toString(), "insert phrase failed! - " + ex.getMessage());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 删除原来的表
        db.execSQL("DROP TABLE IF EXISTS title;");
        db.execSQL("DROP TABLE IF EXISTS phrase;");
        // title(短语的标题)
        db.execSQL("CREATE TABLE IF NOT EXISTS title ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // id
                + "type_id INTEGER, "// msgTypeID
                + "type_name TEXT"// msgTypeName
                + ")");
        // phrase（每个title下的短语）
        db.execSQL("CREATE TABLE IF NOT EXISTS phrase ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // id
                + "type_id INTEGER, "// msgTypeID
                + "text_msg_id INTEGER, "// textMsgId 【新增字段】
                + "text_msg TEXT"// textMessage
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
