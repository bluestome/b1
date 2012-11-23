
package android.skymobi.messenger.database.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.skymobi.messenger.bean.Contact;
import android.skymobi.messenger.bean.Stranger;
import android.skymobi.messenger.database.dao.StrangerDAO;
import android.skymobi.messenger.provider.SocialMessenger.StrangerColumns;
import android.skymobi.messenger.utils.PinYinUtil;
import android.skymobi.messenger.utils.StringUtil;
import android.text.TextUtils;
import android.util.Log;

/**
 * 陌生人DAO实现类
 * 
 * @ClassName: StrangerImpl
 * @Description: TODO
 * @author Bluestome.Zhang
 * @date 2012-9-5 下午07:20:20
 */
public class StrangerImpl extends BaseImpl implements StrangerDAO {

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param context
     */
    public StrangerImpl(Context context) {
        super(context);
    }

    @Override
    public long addStranger(Stranger stranger) {
        ContentValues values = new ContentValues();
        values.put(StrangerColumns.SKY_ID, stranger.getSkyid());
        if (!StringUtil.isBlank(stranger.getDisplayname()))
            values.put(StrangerColumns.DISPLAY_NAME, stranger.getDisplayname());
        if (!StringUtil.isBlank(stranger.getNickname()))
            values.put(StrangerColumns.NICKNAME, stranger.getNickname());
        values.put(StrangerColumns.BIRTHDAY, stranger.getBirthday());
        values.put(StrangerColumns.BLACK_LIST, StrangerColumns.BLACK_LIST_NO);
        if (!StringUtil.isBlank(stranger.getHometown()))
            values.put(StrangerColumns.HOMETOWN, stranger.getHometown());
        values.put(StrangerColumns.LAST_UPDATE_TIME, stranger.getLastUpdateTime());
        if (!StringUtil.isBlank(stranger.getNote()))
            values.put(StrangerColumns.NOTE, stranger.getNote());
        if (!StringUtil.isBlank(stranger.getOrganization()))
            values.put(StrangerColumns.ORGANIZATION, stranger.getOrganization());
        if (!StringUtil.isBlank(stranger.getPhotoId()))
            values.put(StrangerColumns.PHOTO_ID, stranger.getPhotoId());
        if (!StringUtil.isBlank(stranger.getPinyin()))
            values.put(StrangerColumns.PINYIN, stranger.getPinyin());
        if (!StringUtil.isBlank(stranger.getSchool()))
            values.put(StrangerColumns.SCHOOL, stranger.getSchool());
        values.put(StrangerColumns.SEX, stranger.getSex());
        if (!StringUtil.isBlank(stranger.getSignature()))
            values.put(StrangerColumns.SIGNATURE, stranger.getSignature());
        if (!StringUtil.isBlank(stranger.getSkyName()))
            values.put(StrangerColumns.SKYNAME, stranger.getSkyName());
        if (!StringUtil.isBlank(stranger.getSortkey()))
            values.put(StrangerColumns.SORTKEY, stranger.getSortkey());
        long id = insert(StrangerColumns.TABLE_NAME, null, values);
        return id;
    }

    @Override
    public long addStranger(Contact contact, String nickName, String skyName) {
        ContentValues values = new ContentValues();
        values.put(StrangerColumns.SKY_ID, contact.getSkyid());
        if (!StringUtil.isBlank(contact.getDisplayname()))
            values.put(StrangerColumns.DISPLAY_NAME, contact.getDisplayname());
        values.put(StrangerColumns.NICKNAME, nickName);
        values.put(StrangerColumns.BIRTHDAY, contact.getBirthday());
        values.put(StrangerColumns.BLACK_LIST, StrangerColumns.BLACK_LIST_NO);
        if (!StringUtil.isBlank(contact.getHometown()))
            values.put(StrangerColumns.HOMETOWN, contact.getHometown());
        values.put(StrangerColumns.LAST_UPDATE_TIME, contact.getLastUpdateTime());
        if (!StringUtil.isBlank(contact.getNote()))
            values.put(StrangerColumns.NOTE, contact.getNote());
        if (!StringUtil.isBlank(contact.getOrganization()))
            values.put(StrangerColumns.ORGANIZATION, contact.getOrganization());
        if (!StringUtil.isBlank(contact.getPhotoId()))
            values.put(StrangerColumns.PHOTO_ID, contact.getPhotoId());
        if (!StringUtil.isBlank(contact.getPinyin()))
            values.put(StrangerColumns.PINYIN, contact.getPinyin());
        if (!StringUtil.isBlank(contact.getSchool()))
            values.put(StrangerColumns.SCHOOL, contact.getSchool());
        values.put(StrangerColumns.SEX, contact.getSex());
        if (!StringUtil.isBlank(contact.getSignature()))
            values.put(StrangerColumns.SIGNATURE, contact.getSignature());
        values.put(StrangerColumns.SKYNAME, skyName);
        if (!StringUtil.isBlank(contact.getSortkey()))
            values.put(StrangerColumns.SORTKEY, contact.getSortkey());
        long id = insert(StrangerColumns.TABLE_NAME, null, values);
        return id;
    }

    @Override
    public Stranger fetch(int skyid) {
        Stranger stranger = null;
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
                .append(StrangerColumns._ID).append(" as id, ")
                .append(StrangerColumns.SKY_ID).append(" as skyid,")
                .append(StrangerColumns.DISPLAY_NAME).append(" as displayname,")
                .append(StrangerColumns.NICKNAME).append(" as nickname,")
                .append(StrangerColumns.BIRTHDAY).append(" as birthday,")
                .append(StrangerColumns.BLACK_LIST).append(" as blackList,")
                .append(StrangerColumns.HOMETOWN).append(" as hometown,")
                .append(StrangerColumns.LAST_UPDATE_TIME).append(" as lastUpdateTime,")
                .append(StrangerColumns.NOTE).append(" as note,")
                .append(StrangerColumns.ORGANIZATION).append(" as organization,")
                .append(StrangerColumns.PHOTO_ID).append(" as photoId,")
                .append(StrangerColumns.PINYIN).append(" as pinyin,")
                .append(StrangerColumns.SCHOOL).append(" as school,")
                .append(StrangerColumns.SEX).append(" as sex,")
                .append(StrangerColumns.SIGNATURE).append(" as signature,")
                .append(StrangerColumns.SKYNAME).append(" as skyName,")
                .append(StrangerColumns.SORTKEY).append(" as sortkey")
                .append(" from ")
                .append(StrangerColumns.TABLE_NAME)
                .append(" where ")
                .append(StrangerColumns.SKY_ID).append(" = ? ");
        try {
            stranger = queryForObject(Stranger.class, sql.toString(),
                    new String[] {
                        String.valueOf(skyid)
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stranger;
    }

    @Override
    public boolean delete(int skyid) {
        int rows = delete(StrangerColumns.TABLE_NAME, StrangerColumns.SKY_ID + "=" + skyid, null);
        if (rows > 0) {
            return true;
        }
        return false;
    }

    /**
     * 修改陌生人信息
     */
    @Override
    public boolean update(Stranger stranger) {
        ContentValues values = new ContentValues();
        values.put(StrangerColumns.BIRTHDAY, stranger.getBirthday());
        if (!TextUtils.isEmpty(stranger.getDisplayname())) {
            values.put(StrangerColumns.DISPLAY_NAME, stranger.getDisplayname());
            values.put(StrangerColumns.PINYIN, PinYinUtil.getPingYin(stranger.getDisplayname()));
            values.put(StrangerColumns.SORTKEY, PinYinUtil.getSortKey(stranger.getDisplayname()));
        }
        values.put(StrangerColumns.HOMETOWN, stranger.getHometown());
        values.put(StrangerColumns.NOTE, stranger.getNote());
        values.put(StrangerColumns.ORGANIZATION, stranger.getOrganization());
        values.put(StrangerColumns.SCHOOL, stranger.getSchool());
        values.put(StrangerColumns.SEX, stranger.getSex());
        values.put(StrangerColumns.SIGNATURE, stranger.getSignature());

        values.put(StrangerColumns.BLACK_LIST, stranger.getBlackList());
        values.put(StrangerColumns.PHOTO_ID, stranger.getPhotoId());
        values.put(StrangerColumns.LAST_UPDATE_TIME, System.currentTimeMillis());

        values.put(StrangerColumns.NICKNAME, stranger.getNickname());
        values.put(StrangerColumns.SKYNAME, stranger.getSkyName());
        String whereClause = " " + StrangerColumns.SKY_ID + "=" + stranger.getSkyid();
        int result = update(StrangerColumns.TABLE_NAME, values, whereClause, null);
        if (result > 0)
            return true;
        return false;
    }

    @Override
    public boolean update(Contact contact, String nickName, String skyName) {
        ContentValues values = new ContentValues();
        values.put(StrangerColumns.BIRTHDAY, contact.getBirthday());
        if (!TextUtils.isEmpty(contact.getDisplayname())) {
            values.put(StrangerColumns.DISPLAY_NAME, contact.getDisplayname());
            values.put(StrangerColumns.PINYIN, PinYinUtil.getPingYin(contact.getDisplayname()));
            values.put(StrangerColumns.SORTKEY, PinYinUtil.getSortKey(contact.getDisplayname()));
        }
        values.put(StrangerColumns.HOMETOWN, contact.getHometown());
        values.put(StrangerColumns.NOTE, contact.getNote());
        values.put(StrangerColumns.ORGANIZATION, contact.getOrganization());
        values.put(StrangerColumns.SCHOOL, contact.getSchool());
        values.put(StrangerColumns.SEX, contact.getSex());
        values.put(StrangerColumns.SIGNATURE, contact.getSignature());

        values.put(StrangerColumns.BLACK_LIST, contact.getBlackList());
        values.put(StrangerColumns.PHOTO_ID, contact.getPhotoId());
        values.put(StrangerColumns.LAST_UPDATE_TIME, System.currentTimeMillis());

        if (!StringUtil.isBlank(nickName)) {
            values.put(StrangerColumns.NICKNAME, nickName);
        }
        if (!StringUtil.isBlank(skyName)) {
            values.put(StrangerColumns.SKYNAME, skyName);
        }
        String whereClause = " skyid=" + contact.getSkyid();
        int result = update(StrangerColumns.TABLE_NAME, values, whereClause, null);
        if (result > 0)
            return true;
        return false;
    }

    /**
     * 新增
     * 
     * @param table
     * @param nullColumnHack
     * @param values
     * @return rowID
     */
    @Override
    protected long insert(String table, String nullColumnHack, ContentValues values) {
        long rowID = getSQLiteDatabase().insertOrThrow(table, nullColumnHack, values);
        return rowID;
    }

    /**
     * 检查是否已经存在相同的SKYID帐号
     * 
     * @param skyid
     * @return
     */
    @Override
    public boolean checkExistsSkyid(int skyid) {
        boolean isExists = false;
        Cursor cur = null;
        String sql = "select skyid from " + StrangerColumns.TABLE_NAME + " where skyid = ?";
        String[] selectionArgs = {
                String.valueOf(skyid)
        };
        try {
            cur = getSQLiteDatabase().rawQuery(sql, selectionArgs);
            while (cur.moveToNext()) {
                int skyid1 = cur.getInt(0);
                isExists = true;
                break;
            }
        } catch (SQLException ex) {
            Log.e(TAG, "checkExistsSkyid failed! - " + ex.getMessage());
        } finally {
            if (cur != null)
                cur.close();
        }
        return isExists;
    }
}
