
package android.skymobi.messenger.database.dao.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.skymobi.messenger.R;
import android.skymobi.messenger.database.dao.CitysDAO;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @ClassName: CitysImpl
 * @Description: TODO
 * @author Lv.Lv
 * @date 2012-3-8 下午4:00:03
 */
public class CitysImpl extends BaseImpl implements CitysDAO {

    private static final String PACKAGE_NAME = "android.skymobi.messenger";
    private static final String DB_NAME = "city.db";
    private static final String DB_PATH = "/data" + Environment
            .getDataDirectory().getAbsolutePath()
            + "/" + PACKAGE_NAME + "/databases";

    private final int BUFFER_SIZE = 1024 * 8;

    private SQLiteDatabase mDatabase;

    public CitysImpl(Context context) {
        super(context);
    }

    @Override
    public ArrayList<String> getAllProvinces() {
        ArrayList<String> list = new ArrayList<String>();
        Cursor cur = null;

        if (mDatabase == null)
            openDatabase();

        try {
            String sql = "select root from city group by root order by _id";
            cur = mDatabase.rawQuery(sql, null);
            if (cur.getCount() > 0) {
                cur.moveToFirst();
                do {
                    list.add(cur.getString(0));
                } while (cur.moveToNext());
            }
        } catch (SQLException ex) {
            Log.e(TAG, "fetchcity failed! - " + ex.getMessage());
        }
        if (cur != null)
            cur.close();

        return list;
    }

    @Override
    public ArrayList<String> getCitysInProvince(String provice) {
        ArrayList<String> list = new ArrayList<String>();
        Cursor cur = null;

        if (mDatabase == null)
            openDatabase();

        try {
            String sql = "select parent from city where root=? group by parent order by _id";
            String[] selectionArgs = {
                    provice
            };
            cur = mDatabase.rawQuery(sql, selectionArgs);
            if (cur.getCount() > 0) {
                cur.moveToFirst();
                do {
                    list.add(cur.getString(0));
                } while (cur.moveToNext());
            }
        } catch (SQLException ex) {
            Log.e(TAG, "fetchcity failed! - " + ex.getMessage());
        }
        if (cur != null)
            cur.close();

        return list;
    }

    public void openDatabase() {
        mDatabase = openDatabase(DB_PATH + "/" + DB_NAME);
    }

    @Override
    public void closeDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    @Override
    public String getProvinceName(int id) {
        ArrayList<String> list = getAllProvinces();
        return list == null ? null : list.get(id);
    }

    @Override
    public String getCityName(String province, int id) {
        ArrayList<String> list = getCitysInProvince(province);
        return list == null ? null : list.get(id);
    }

    private SQLiteDatabase openDatabase(String dbfilepath)
    {
        try
        {
            File file = new File(dbfilepath);
            File dir = new File(file.getParent());
            if (!dir.exists())
                dir.mkdir();

            if (!(new File(dbfilepath)).exists()) {
                InputStream is = context.getResources().openRawResource(R.raw.city);
                FileOutputStream fos = new FileOutputStream(dbfilepath);

                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }

                fos.close();
                is.close();
            }

            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
                    dbfilepath, null);
            return database;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
