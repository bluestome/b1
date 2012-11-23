
package android.skymobi.messenger.dataaccess.dao.config;

import android.database.Cursor;
import android.skymobi.common.log.SLog;
import android.skymobi.messenger.dataaccess.bean.AttrConfig;
import android.skymobi.messenger.dataaccess.dao.AbsBasicDAO;

import org.apache.commons.lang.StringUtils;

public class ConfigDao extends AbsBasicDAO implements IConfigDao {

    protected final static String TAG = "--DAO--";

    private final String tableName = " sxconfig ";

    @Override
    public boolean saveToConfig(AttrConfig config) {
        if (config == null || StringUtils.isBlank(config.getAttrName()))
            return false;
        Cursor cursor = null;
        try {
            cursor = getSQLite().rawQuery(
                    "SELECT _ID FROM " + tableName + " WHERE attrName=?",
                    new String[] {
                        config.getAttrName()
                    });
            // 如果记录已存在，则更新
            if (cursor != null && cursor.moveToNext()) {
                getSQLite().execSQL(
                        "UPDATE " + tableName + " SET attrVal='" + config.getAttrVal()
                                + "' WHERE attrName='" + config.getAttrName() + "'");
                SLog.d(TAG, "更新配置记录成功!");
            } else {// 否则，插入
                getSQLite().execSQL(
                        "INSERT INTO " + tableName
                                + "(attrName,attrVal,version,createTime)VALUES('"
                                + config.getAttrName() + "','" + config.getAttrVal() + "','"
                                + config.getVersion() + "'," + System.currentTimeMillis() + ")");
                SLog.d(TAG, "添加配置记录成功!");
            }
            return true;
        } catch (Exception e) {
            SLog.e(TAG, "保存配置记录时出现异常:" + e.getMessage());
        } finally {
            closeCursor(cursor);
        }
        return false;

    }

    @Override
    public AttrConfig getAttrConfig(String attrName) {
        Cursor cursor = null;
        AttrConfig config = null;
        try {
            cursor = getSQLite().rawQuery(
                    "SELECT _ID,attrName,attrVal,version,createTime,reserve0,reserve1 FROM "
                            + tableName + " WHERE attrName=?",
                    new String[] {
                        attrName
                    });
            // 如果记录已存在，则更新
            if (cursor.moveToNext()) {
                config = new AttrConfig();
                config.setAttrName(attrName);
                config.setAttrVal(cursor.getString(2));
                config.setVersion(cursor.getString(3));
                config.setCreateTime(cursor.getLong(4));
                config.setReserve0(cursor.getString(5));
                config.setReserve1(cursor.getString(6));
                SLog.d(TAG, "获取配置:" + config.toString());
            }

        } catch (Exception e) {
            SLog.e(TAG, "保存配置记录时出现异常:" + e.getMessage());
        } finally {
            closeCursor(cursor);
        }
        return config;
    }

}
