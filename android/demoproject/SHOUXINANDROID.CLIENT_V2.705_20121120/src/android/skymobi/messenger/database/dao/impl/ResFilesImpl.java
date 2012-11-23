
package android.skymobi.messenger.database.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.skymobi.messenger.bean.ResFile;
import android.skymobi.messenger.database.dao.ResFilesDAO;
import android.skymobi.messenger.exception.MessengerException;
import android.skymobi.messenger.provider.SocialMessenger.FilesColumns;
import android.text.TextUtils;

/**
 * @ClassName: ResFileImpl
 * @author Sivan.LV
 * @date 2012-3-30 下午1:17:48
 */
public class ResFilesImpl extends BaseImpl implements ResFilesDAO {

    public ResFilesImpl(Context context) {
        super(context);

    }

    @Override
    public long addFile(ResFile file) {
        if (null == file)
            return -1;
        ContentValues values = new ContentValues();
        values.put(FilesColumns.VERSION, file.getVersion());
        values.put(FilesColumns.PATH, file.getPath());
        values.put(FilesColumns.URL, file.getUrl());
        values.put(FilesColumns.SIZE, file.getSize());
        values.put(FilesColumns.LENGTH, file.getLength());
        values.put(FilesColumns.FORMAT, file.getFormat());
        return insert(FilesColumns.TABLE_NAME, null, values);
    }

    @Override
    public boolean deleteFile(ResFile file) {
        return deleteFile(file.getId());
    }

    @Override
    public boolean deleteFile(long id) {
        int rows = delete(FilesColumns.TABLE_NAME, "_id=?", new String[] {
                id + ""
        });
        return rows > 0 ? true : false;
    }

    @Override
    public long updateFile(ResFile file) {
        if (null == file || file.getId() <= 0)
            throw new MessengerException("参数不能为空且id必须大于0");
        ContentValues values = new ContentValues();
        if (0 != file.getVersion()) {
            values.put(FilesColumns.VERSION, file.getVersion());
        }

        if (!TextUtils.isEmpty(file.getPath())) {
            values.put(FilesColumns.PATH, file.getPath());
        }

        if (!TextUtils.isEmpty(file.getUrl())) {
            values.put(FilesColumns.URL, file.getUrl());
        }

        if (0 != file.getSize()) {
            values.put(FilesColumns.SIZE, file.getSize());
        }

        if (0 != file.getLength()) {
            values.put(FilesColumns.LENGTH, file.getLength());
        }

        if (!TextUtils.isEmpty(file.getFormat())) {
            values.put(FilesColumns.FORMAT, file.getUrl());
        }
        return update(FilesColumns.TABLE_NAME, values, "_id=" + file.getId(), null);
    }

    @Override
    public ResFile getFile(long id) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(FilesColumns._ID).append(" as id,")
                .append(FilesColumns.VERSION).append(" as version,")
                .append(FilesColumns.PATH).append(" as path,")
                .append(FilesColumns.URL).append(" as url,")
                .append(FilesColumns.SIZE).append(" as size,")
                .append(FilesColumns.LENGTH).append(" as length,")
                .append(FilesColumns.FORMAT).append(" as format")
                .append(" from ")
                .append(FilesColumns.TABLE_NAME).append(" where _id=").append(id);
        return queryForObject(ResFile.class, sql.toString());
    }

    @Override
    public long updateOrAdd(ResFile file) {
        if (file == null || TextUtils.isEmpty(file.getUrl()))
            return 0;
        // 原来有对应的file id 则更新，返回原来的id,否则添加一条记录，返回该记录的id
        if (file.getId() > 0 && updateFile(file) > 0) {
            return file.getId();
        } else {
            return addFile(file);
        }
    }

}
